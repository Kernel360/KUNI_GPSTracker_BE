package com.example.BackendServer.emulator.controller;

import com.example.BackendServer.emulator.model.GpsRequest;
import com.example.BackendServer.emulator.model.OnOffRequest;
import com.example.BackendServer.emulator.model.TokenRequest;
import com.example.BackendServer.emulator.model.TokenResponse;
import com.example.BackendServer.emulator.service.EmulatorService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/emulator")
@RequiredArgsConstructor

public class EmulatorController {
    private final EmulatorService service;

    @PostMapping("/token")
    public TokenResponse token(@RequestBody TokenRequest req) {
        return new TokenResponse(service.issueToken(req));
    }

    @PostMapping("/on")
    public String on(@RequestHeader("Authorization") String auth, @RequestBody OnOffRequest req) {
        service.verifyToken(auth);
        service.handleOn(req);
        return "on ok";
    }

    @PostMapping("/off")
    public String off(@RequestHeader("Authorization") String auth, @RequestBody OnOffRequest req) {
        service.verifyToken(auth);
        service.handleOff(req);
        return "off ok";
    }

    @PostMapping("/gps")
    public String gps(@RequestHeader("Authorization") String auth, @RequestBody GpsRequest req) {
        service.verifyToken(auth);
        service.handleGps(req);
        return "gps ok";
    }
}
