package com.example.BackendServer.emulator.service;

import com.example.BackendServer.emulator.db.GpsCycleEntity;
import com.example.BackendServer.emulator.db.GpsCycleRepository;
import com.example.BackendServer.emulator.db.OnOffRecordEntity;
import com.example.BackendServer.emulator.db.OnOffRecordRepository;
import com.example.BackendServer.emulator.model.*;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Key;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
@Service
@Transactional
public class EmulatorService {

    private final Map<String, String> tokenStore = new ConcurrentHashMap<>();
    private Key jwtKey;

    private final OnOffRecordRepository onOffRecordRepository;
    private final GpsCycleRepository gpsCycleRepository;

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
            Jwts.parserBuilder().setSigningKey(jwtKey).build().parseClaimsJws(token);
        } catch (Exception e) {
            throw new RuntimeException("토큰 유효하지 않음");
        }
    }

    // 시동 ON 저장 (토큰 파라미터 추가)
    public StandardResponse handleOn(OnOffRequest req, String token) {
        System.out.println("[ON] " + req.getMdn() + " at " + req.getOnTime());

        OnOffRecordEntity onEntity = OnOffRecordEntity.builder()
            .mdn(req.getMdn())
            .tid(req.getTid())
            .mid(req.getMid())
            .pv(req.getPv())
            .did(req.getDid())
            .gcd(req.getGcd())
            .lat(req.getLat())
            .lon(req.getLon())
            .ang(req.getAng())
            .spd(req.getSpd())
            .sum(req.getSum())

            .type("ON")
            .timestamp(req.getOnTime())
            .build();

        onOffRecordRepository.save(onEntity);

        return new StandardResponse("000", "Success", req.getMdn());
    }

    // 시동 OFF 저장 (토큰 파라미터 추가)
    public StandardResponse handleOff(OnOffRequest req, String token) {
        System.out.println("[OFF] " + req.getMdn() + " at " + req.getOffTime());

        OnOffRecordEntity offEntity = OnOffRecordEntity.builder()
            .mdn(req.getMdn())
            .tid(req.getTid())
            .mid(req.getMid())
            .pv(req.getPv())
            .did(req.getDid())
            .gcd(req.getGcd())
            .lat(req.getLat())
            .lon(req.getLon())
            .ang(req.getAng())
            .spd(req.getSpd())
            .sum(req.getSum())

            .type("OFF")
            .timestamp(req.getOffTime())
            .build();

        onOffRecordRepository.save(offEntity);

        return new StandardResponse("000", "Success", req.getMdn());
    }

    // GPS 주기 정보 저장 (토큰 파라미터 추가)
    public StandardResponse handleGps(GpsCycleRequest req, String token) {
        System.out.println("[DEBUG] handleGps() called");

        if (req == null) {
            System.out.println("[ERROR] GpsCycleRequest is null");
            return new StandardResponse("999", "Request is null", null);
        }

        System.out.println("[DEBUG] mdn: " + req.getMdn());
        System.out.println("[DEBUG] tid: " + req.getTid());
        System.out.println("[DEBUG] mid: " + req.getMid());
        System.out.println("[DEBUG] pv: " + req.getPv());
        System.out.println("[DEBUG] did: " + req.getDid());

        if (req.getCList() == null) {
            System.out.println("[GPS] " + req.getMdn() + " - cList is null");
            return new StandardResponse("001", "cList is missing", req.getMdn());
        }

        System.out.println("[DEBUG] cList size: " + req.getCList().size());

        for (GpsCycleData gpsData : req.getCList()) {
            System.out.println("[DEBUG] gpsData: " + gpsData);
            System.out.println("[DEBUG] gcd: " + gpsData.getGcd());
            System.out.println("[DEBUG] lat: " + gpsData.getLat());
            System.out.println("[DEBUG] lon: " + gpsData.getLon());
            System.out.println("[DEBUG] ang: " + gpsData.getAng());
            System.out.println("[DEBUG] spd: " + gpsData.getSpd());
            System.out.println("[DEBUG] sum: " + gpsData.getSum());
            System.out.println("[DEBUG] bat: " + gpsData.getBat());
            System.out.println("[DEBUG] sec: " + gpsData.getSec());

            LocalDateTime gpsTime;
            try {
                gpsTime = LocalDateTime.now().minusSeconds(Integer.parseInt(gpsData.getSec()));
            } catch (NumberFormatException e) {
                gpsTime = LocalDateTime.now();
                System.out.println("[ERROR] gpsData.sec parse error: " + gpsData.getSec());
            }

            GpsCycleEntity entity = GpsCycleEntity.builder()
                .mdn(req.getMdn())
                .tid(req.getTid())
                .mid(req.getMid())
                .pv(req.getPv())
                .did(req.getDid())
                .gcd(gpsData.getGcd())
                .lat(gpsData.getLat())
                .lon(gpsData.getLon())
                .ang(gpsData.getAng())
                .spd(gpsData.getSpd())
                .sum(gpsData.getSum())
                .bat(gpsData.getBat()) // 추가 필요
                .time(gpsTime)
                .token(token)
                .build();

            gpsCycleRepository.save(entity);
            System.out.println("[DEBUG] GPS entity saved to DB");
        }

        return new StandardResponse("000", "Success", req.getMdn());
    }

}
