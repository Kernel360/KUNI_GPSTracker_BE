package com.example.BackendServer.emulator.service;

import com.example.BackendServer.device.db.DeviceRepository;
import com.example.BackendServer.emulator.model.*;
import com.example.BackendServer.global.exception.CustomException;
import com.example.BackendServer.global.exception.ErrorCode;
import com.example.BackendServer.gpsRecord.db.GpsRecordEntity;
import com.example.BackendServer.gpsRecord.db.GpsRecordRepository;
import com.example.BackendServer.record.db.RecordEntity;
import com.example.BackendServer.record.db.RecordRepository;
import com.example.BackendServer.record.model.RecordRequest;
import com.example.BackendServer.record.service.RecordService;
import com.example.BackendServer.vehicle.db.VehicleEntity;
import com.example.BackendServer.vehicle.db.VehicleRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Key;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.example.BackendServer.global.exception.ErrorCode.EMPTY_CLIST_ERROR;
import static com.example.BackendServer.global.exception.ErrorCode.INVALID_TOKEN_ERROR;

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

    private final Map<String, String> tokenStore = new ConcurrentHashMap<>();
    private Key jwtKey;

    @PostConstruct
    public void init() {
        jwtKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);
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
            .status(VehicleEntity.Status.ACTIVE)
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
            .status(VehicleEntity.Status.INACTIVE)
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
                LocalDateTime oTime = LocalDateTime.parse(req.getOTime() + data.getSec(), formatter);
                return GpsRecordEntity.builder()
                    .record(activeRecord)
                    .vehicle(vehicle)
                    .gcd(data.getGcd())
                    .latitude(Double.parseDouble(data.getLat()))
                    .longitude(Double.parseDouble(data.getLon()))
                    .oTime(oTime)
                    .status(GpsRecordEntity.Status.ACTIVE)
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
}
