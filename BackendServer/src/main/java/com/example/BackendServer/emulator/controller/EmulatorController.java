package com.example.BackendServer.emulator.controller;

import com.example.BackendServer.emulator.model.*;
import com.example.BackendServer.emulator.service.EmulatorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/emulator")
@RequiredArgsConstructor
public class EmulatorController {

    private final EmulatorService emulatorService;

    @PostMapping("/token")
    public ResponseEntity<TokenResponse> getToken(@RequestBody TokenRequest request) {
        return ResponseEntity.ok(emulatorService.issueToken(request));
    }

    @PostMapping("/on")
    public ResponseEntity<StandardResponse> on(
        @RequestBody OnOffRequest request,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {

        emulatorService.verifyToken(authHeader);
        String token = authHeader.replace("Bearer ", "").trim();
        return ResponseEntity.ok(emulatorService.handleOn(request, token));
    }

    @PostMapping("/off")
    public ResponseEntity<StandardResponse> off(
        @RequestBody OnOffRequest request,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {

        emulatorService.verifyToken(authHeader);
        String token = authHeader.replace("Bearer ", "").trim();
        return ResponseEntity.ok(emulatorService.handleOff(request, token));
    }

    @PostMapping("/gps")
    public ResponseEntity<StandardResponse> gps(
        @RequestBody GpsCycleRequest request,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {

        emulatorService.verifyToken(authHeader);
        String token = authHeader.replace("Bearer ", "").trim();
        return ResponseEntity.ok(emulatorService.handleGps(request, token));
    }
}
