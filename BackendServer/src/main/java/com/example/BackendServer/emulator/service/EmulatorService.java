package com.example.BackendServer.emulator.service;

import com.example.BackendServer.emulator.model.GpsRequest;
import com.example.BackendServer.emulator.model.OnOffRequest;
import com.example.BackendServer.emulator.model.TokenRequest;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class EmulatorService {
    private final Map<String, String> tokenStore = new ConcurrentHashMap<>();

    public String issueToken(TokenRequest request) {
        if("emulator1".equals(request.getEmulatorId()) && "pass123".equals(request.getSecret())) {
            String token = UUID.randomUUID().toString();
            tokenStore.put(request.getEmulatorId(), token);
            return token;
        } else {
            throw new RuntimeException("인증 실패");
        }
    }

    public void verifyToken(String authHeader) {
        String token = authHeader.replace("Bearer ", "").trim();
        if(!tokenStore.containsValue(token)) {
            throw new RuntimeException("토큰 불일치");
        }
    }

    public void handleOn(OnOffRequest req) {
        System.out.println("[ON] " + req.getEmulatorId() + " at " + req.getTimestamp());
    }
    public void handleOff(OnOffRequest req) {
        System.out.println("[OFF] " + req.getEmulatorId() + " at " + req.getTimestamp());
    }
    public void handleGps(GpsRequest req) {
        System.out.println("[GPS] " + req.getEmulatorId() + " (" + req.getLatitude() + "," + req.getLongitude() + ") at " + req.getTimestamp());
    }
}

