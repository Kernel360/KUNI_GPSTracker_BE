package com.example.BackendServer.emulator.service;

import com.example.BackendServer.device.db.DeviceRepository;
import com.example.BackendServer.emulator.model.*;
import com.example.BackendServer.global.exception.CustomException;
import com.example.BackendServer.global.exception.ErrorCode;
import com.example.BackendServer.gpsRecord.model.GpsRecordRequest;
import com.example.BackendServer.gpsRecord.service.GpsRecordService;
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
    private final GpsRecordService gpsRecordService;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    // JWT 관련
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
            .setExpiration(new Date(System.currentTimeMillis() + 4 * 60 * 60 * 1000)) // 4시간
            .signWith(jwtKey)
            .compact();
        tokenStore.put(request.getMdn(), token);
        return new TokenResponse("000", "Success", request.getMdn(), token, "4");
    }

    // 토큰 검증
    public void verifyToken(String authHeader) {
        String token = authHeader.replace("Bearer ", "").trim();
        try {
            Jwts.parserBuilder()
                .setSigningKey(jwtKey)
                .build()
                .parseClaimsJws(token);
        } catch (Exception e) {
            throw new CustomException(INVALID_TOKEN_ERROR);
        }
    }

    // 시동 ON 처리 + DB 저장
    public StandardResponse handleOn(OnOffRequest req) {
        log.info("✅ [ON] EmulatorService: {}", req);

        VehicleEntity vehicle = getVehicleByMdn(req.getMdn());

        RecordRequest recordReq = RecordRequest.builder()
            .vehicleId(vehicle.getId())
            .onTime(LocalDateTime.parse(req.getOnTime(), formatter))
            .build();

        recordService.create(recordReq);

        return new StandardResponse("000", "Success", req.getMdn());
    }

    // 시동 OFF 처리 + DB 저장
    public StandardResponse handleOff(OnOffRequest req) {
        log.info("🛑 [OFF] EmulatorService: {}", req);

        VehicleEntity vehicle = getVehicleByMdn(req.getMdn());

        RecordEntity activeRecord = recordRepository.findByVehicleIdAndOffTimeIsNull(vehicle.getId())
            .orElseThrow(() -> new CustomException(ErrorCode.RECORD_NOT_FOUND));

        activeRecord.setOffTime(LocalDateTime.parse(req.getOffTime(), formatter));
        recordRepository.save(activeRecord);

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

        for (GpsCycleData gpsData : req.getCList()) {
            try {
                LocalDateTime oTime = LocalDateTime.parse(req.getOTime() + gpsData.getSec(), formatter);

                GpsRecordRequest gpsReq = GpsRecordRequest.builder()
                    .vehicleId(vehicle.getId())
                    .status("ACTIVE")
                    .latitude(Double.parseDouble(gpsData.getLat()))
                    .longitude(Double.parseDouble(gpsData.getLon()))
                    .oTime(oTime)
                    .gcd(gpsData.getGcd())
                    .totalDist(gpsData.getSum())
                    .build();

                gpsRecordService.create(gpsReq);

            } catch (Exception e) {
                log.error("❌ GPS 데이터 저장 실패: {}", gpsData, e);
            }
        }


        return new StandardResponse("000", "Success", req.getMdn());
    }

    private VehicleEntity getVehicleByMdn(String mdn) {
        return vehicleRepository.findByVehicleNumber(mdn)
            .orElseThrow(() -> new CustomException(ErrorCode.VEHICLE_NOT_FOUND));
    }
}
