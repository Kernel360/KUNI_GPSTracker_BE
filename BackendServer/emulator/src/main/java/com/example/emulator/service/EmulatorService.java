package com.example.emulator.service;

import com.example.emulator.model.*;
import com.example.global.exception.CustomException;
import com.example.global.exception.ErrorCode;
// common 모듈의 엔티티와 리포지토리 사용
import com.example.entity.GpsRecordEntity;
import com.example.entity.RecordEntity;
import com.example.entity.VehicleEntity;
import com.example.repository.GpsRecordRepository;
import com.example.repository.RecordRepository;
import com.example.repository.VehicleRepository;
import com.example.repository.DeviceRepository;
import com.example.model.RecordRequest;
import com.example.service.RecordService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.security.Key;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.example.global.Class.VehicleStatus.ACTIVE;
import static com.example.global.Class.VehicleStatus.INACTIVE;
import static com.example.global.exception.ErrorCode.EMPTY_CLIST_ERROR;
import static com.example.global.exception.ErrorCode.INVALID_TOKEN_ERROR;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class EmulatorService {

    private final DeviceRepository deviceRepository;
    private final VehicleRepository vehicleRepository;
    private final RecordRepository recordRepository;
    private final RecordService recordService;
    private final GpsRecordRepository gpsRecordRepository;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    //@Value("${JWT_SECRET}")
    private String secretBase64 = "SFVGjDe/OwyN46p1euKSNQvZrpF14kwEKI9kUJ50BvI=";

    private final Map<String, String> tokenStore = new ConcurrentHashMap<>();
    private Key jwtKey;

    @PostConstruct
    public void init() {
        if (secretBase64 == null || secretBase64.isBlank()) {
            throw new CustomException(ErrorCode.JWT_SECRET_KEY_MISSING);
        }
        jwtKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretBase64));
    }
    // 토큰 발급
    public TokenResponse issueToken(TokenRequest request) {
        String token = Jwts.builder()
            .setSubject(request.getMdn())
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + 4 * 60 * 60 * 1000))
            .signWith(jwtKey)
            .compact();
        tokenStore.put(request.getMdn(), token);
        return new TokenResponse("000", "Success", request.getMdn(), token, "4");
    }

    // 토큰 검증
    public void verifyToken(String authHeader) {
        if(!StringUtils.hasText(authHeader)){
            throw new CustomException(INVALID_TOKEN_ERROR);
        }
        String token = authHeader.replace("Bearer ", "").trim();
        try {
            Jwts.parserBuilder().setSigningKey(jwtKey).build().parseClaimsJws(token);
        } catch (Exception e) {
            throw new CustomException(INVALID_TOKEN_ERROR);
        }
    }

    // 시동 ON 처리 + DB 저장 + Vehicle 상태 ACTIVE
    public StandardResponse handleOn(OnOffRequest req) {
        log.info("✅ [ON] EmulatorService: {}", req);

        VehicleEntity vehicle = getVehicleByMdn(req.getMdn());

        RecordRequest recordReq = RecordRequest.builder()
                .vehicleId(vehicle.getId())
                .onTime(LocalDateTime.parse(req.getOnTime(), formatter))
                .build();

        recordService.create(recordReq);

        vehicle = vehicle.toBuilder()
                .status(ACTIVE)
                .build();
        vehicleRepository.save(vehicle);

        return new StandardResponse("000", "Success", req.getMdn());
    }

    // 시동 OFF 처리 + DB 저장 + Vehicle 상태 INACTIVE
    public StandardResponse handleOff(OnOffRequest req) {
        log.info("🛑 [OFF] EmulatorService: {}", req);

        VehicleEntity vehicle = getVehicleByMdn(req.getMdn());

        RecordEntity activeRecord = recordRepository.findByVehicleIdAndOffTimeIsNull(vehicle.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.RECORD_NOT_FOUND));

        // 1. offTime, sumDist 저장
        activeRecord.setOffTime(LocalDateTime.parse(req.getOffTime(), formatter));
        activeRecord.setSumDist(req.getSum());  // sum 필드 활용
        recordRepository.save(activeRecord);

        // 2. sumDist → totalDist로 변환 및 Vehicle 업데이트
        String sumDistStr = activeRecord.getSumDist();
        Long sumDistLong = 0L;

        if (sumDistStr != null) {
            try {
                sumDistLong = Long.parseLong(sumDistStr);
            } catch (NumberFormatException e) {
                log.warn("sumDist 변환 실패: {}", sumDistStr);
            }
        }

        // 기존 totalDist + 이번 주행 거리 누적
        Long updatedTotalDist = vehicle.getTotalDist() + sumDistLong;

        vehicle = vehicle.toBuilder()
                .status(INACTIVE)
                .totalDist(updatedTotalDist)
                .build();
        vehicleRepository.save(vehicle);


        return new StandardResponse("000", "Success", req.getMdn());
    }


    // GPS 주기 데이터 처리 + DB 저장
    public StandardResponse handleGps(GpsCycleRequest req) {
        log.info("📍 [GPS CYCLE] EmulatorService: {}", req);

        if (req.getCList() == null || req.getCList().isEmpty()) {
            log.warn("[GPS] cList is null or empty for mdn: {}", req.getMdn());
            throw new CustomException(EMPTY_CLIST_ERROR);
        }

        VehicleEntity vehicle = getVehicleByMdn(req.getMdn());

        RecordEntity activeRecord = recordRepository.findByVehicleIdAndOffTimeIsNull(vehicle.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.RECORD_NOT_FOUND));

        List<GpsRecordEntity> entities = req.getCList().stream()
                .map(data -> {
                    LocalDateTime oTime = LocalDateTime.parse(req.getOTime() + "00", formatter).plusSeconds(data.getSec());

                    return GpsRecordEntity.builder()
                            .record(activeRecord)
                            .vehicle(vehicle)
                            .gcd(data.getGcd())
                            .latitude(parseLat(data.getLat()))
                            .longitude(parseLon(data.getLon()))
                            .oTime(oTime)
                            .status(ACTIVE)
                            .totalDist(data.getSum())
                            .build();
                })
                .toList();

        gpsRecordRepository.saveAll(entities);

        return new StandardResponse("000", "Success", req.getMdn());
    }

    private VehicleEntity getVehicleByMdn(String mdn) {
        return vehicleRepository.findByVehicleNumber(mdn)
            .orElseThrow(() -> new CustomException(ErrorCode.VEHICLE_NOT_FOUND));
    }

    /** '앞 2자리.나머지' 형태로 위도 변환 */
    private static double parseLat(String raw) {
        if (raw.contains(".")) return Double.parseDouble(raw);   // 이미 변환돼 있음
        return Double.parseDouble(raw.substring(0, 2) + "." + raw.substring(2));
    }

    /** '앞 3자리.나머지' 형태로 경도 변환 */
    private static double parseLon(String raw) {
        if (raw.contains(".")) return Double.parseDouble(raw);
        return Double.parseDouble(raw.substring(0, 3) + "." + raw.substring(3));
    }
}
