package com.example.BackendServer.emulator.controller;

import com.example.BackendServer.emulator.model.*;
import com.example.BackendServer.emulator.service.EmulatorService;

import io.swagger.v3.oas.annotations.Parameter;
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
    public ResponseEntity<StandardResponse> on(@RequestBody OnOffRequest request, @Parameter(hidden = true) @RequestHeader(value = HttpHeaders.AUTHORIZATION) String authHeader) {
        emulatorService.verifyToken(authHeader);
        return ResponseEntity.ok(emulatorService.handleOn(request));
    }

    @PostMapping("/off")
    public ResponseEntity<StandardResponse> off(@RequestBody OnOffRequest request, @Parameter(hidden = true) @RequestHeader(value = HttpHeaders.AUTHORIZATION) String authHeader) {
        emulatorService.verifyToken(authHeader);
        return ResponseEntity.ok(emulatorService.handleOff(request));
    }

    @PostMapping("/gps")
    public ResponseEntity<StandardResponse> gps(@RequestBody GpsCycleRequest request, @Parameter(hidden = true) @RequestHeader(value = HttpHeaders.AUTHORIZATION) String authHeader) {
        emulatorService.verifyToken(authHeader);
        return ResponseEntity.ok(emulatorService.handleGps(request));
    }
}
