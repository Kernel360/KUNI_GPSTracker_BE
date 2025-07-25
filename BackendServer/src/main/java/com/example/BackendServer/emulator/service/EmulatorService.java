package com.example.BackendServer.emulator.service;

import com.example.BackendServer.emulator.model.*;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct; // ✅ 여기!
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class EmulatorService {
    private final Map<String, String> tokenStore = new ConcurrentHashMap<>();
    private Key jwtKey;

    @PostConstruct
    public void init() {
        jwtKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    }

    public TokenResponse issueToken(TokenRequest request) {
        if ("01234567890".equals(request.getMdn()) && "A001".equals(request.getTid())) {
            String token = Jwts.builder()
                .setSubject(request.getMdn())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 4 * 60 * 60 * 1000)) // 4시간
                .signWith(jwtKey)
                .compact();
            tokenStore.put(request.getMdn(), token);
            return new TokenResponse("000", "Success", request.getMdn(), token, "4");
        } else {
            throw new RuntimeException("인증 실패");
        }
    }

    public void verifyToken(String authHeader) {
        String token = authHeader.replace("Bearer ", "").trim();
        try {
            Jwts.parserBuilder().setSigningKey(jwtKey).build().parseClaimsJws(token);
        } catch (Exception e) {
            throw new RuntimeException("토큰 유효하지 않음");
        }
    }

    public StandardResponse handleOn(OnOffRequest req) {
        System.out.println("[ON] " + req.getMdn() + " at " + req.getOnTime());
        return new StandardResponse("000", "Success", req.getMdn());
    }

    public StandardResponse handleOff(OnOffRequest req) {
        System.out.println("[OFF] " + req.getMdn() + " at " + req.getOffTime());
        return new StandardResponse("000", "Success", req.getMdn());
    }

    public StandardResponse handleGps(GpsCycleRequest req) {
        if (req.getCList() == null) {
            System.out.println("[GPS] " + req.getMdn() + " - cList is null");
            return new StandardResponse("001", "cList is missing", req.getMdn());
        }
        System.out.println("[GPS] " + req.getMdn() + " - count: " + req.getCList().size());
        return new StandardResponse("000", "Success", req.getMdn());
    }

}
