package com.example.emulator.service;

import com.example.emulator.model.*;
import com.example.exception.CustomException;
import com.example.exception.ErrorCode;
import com.example.kafka.converter.MsgConverter;
import com.example.kafka.model.GpsMsg;
import com.example.kafka.model.OnOffMsg;
import com.example.kafka.producer.GpsProducer;
import com.example.kafka.producer.OnOffProducer;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.security.Key;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmulatorService {

    private final OnOffProducer onOffProducer;
    private final GpsProducer gpsProducer;
    private final MsgConverter msgConverter;

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
            throw new CustomException(ErrorCode.INVALID_TOKEN_ERROR);
        }
        String token = authHeader.replace("Bearer ", "").trim();
        try {
            Jwts.parserBuilder().setSigningKey(jwtKey).build().parseClaimsJws(token);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.INVALID_TOKEN_ERROR);
        }
    }

    // 시동 ON 처리 + DB 저장 + Vehicle 상태 ACTIVE
    public StandardResponse handleOn(OnOffRequest req) {
        log.info("✅ [ON] EmulatorService: {}", req);
        OnOffMsg msg = msgConverter.OnOffRequestToOnOffMsg(req, "ON");

        try{
            onOffProducer.sendMessage(msg);
        }
        catch (Exception e){
            throw new CustomException(ErrorCode.ONOFF_PRODUCER_ERROR);
        }
        return new StandardResponse("000", "Success", req.getMdn());
    }

    // 시동 OFF 처리 + DB 저장 + Vehicle 상태 INACTIVE
    public StandardResponse handleOff(OnOffRequest req) {
        log.info("🛑 [OFF] EmulatorService: {}", req);

        OnOffMsg msg = msgConverter.OnOffRequestToOnOffMsg(req, "OFF");

        try{
            onOffProducer.sendMessage(msg);
        }
        catch (Exception e){
            throw new CustomException(ErrorCode.ONOFF_PRODUCER_ERROR);
        }
        return new StandardResponse("000", "Success", req.getMdn());
    }


    // GPS 주기 데이터 처리 + DB 저장
    public StandardResponse handleGps(GpsCycleRequest req) {
        log.info("📍 [GPS CYCLE] EmulatorService: {}", req);

        if (req.getCList() == null || req.getCList().isEmpty()) {
            log.warn("[GPS] cList is null or empty for mdn: {}", req.getMdn());
            throw new CustomException(ErrorCode.EMPTY_CLIST_ERROR);
        }

        GpsMsg msg = msgConverter.GpsCycleRequestToGpsMsg(req, "GPS");

        try{
            gpsProducer.sendMessage(msg);
        }
        catch (Exception e){
            throw new CustomException(ErrorCode.GPS_PRODUCER_ERROR);
        }
        return new StandardResponse("000", "Success", req.getMdn());
    }
}
