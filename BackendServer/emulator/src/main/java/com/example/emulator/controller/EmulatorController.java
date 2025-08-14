package com.example.emulator.controller;

import com.example.emulator.model.*;
import com.example.emulator.service.EmulatorService;

import io.swagger.v3.oas.annotations.Operation;
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
    @Operation(summary = "토큰 발급", description = "애뮬레이터용 인증 토큰을 반환합니다.")
    public ResponseEntity<TokenResponse> getToken(@RequestBody TokenRequest request) {
        return ResponseEntity.ok(emulatorService.issueToken(request));
    }

    @PostMapping("/on")
    @Operation(summary = "애뮬레이터 시동 ON", description = "애뮬레이터로부터 on 요청을 받습니다.")
    public ResponseEntity<StandardResponse> on(@RequestBody OnOffRequest request, @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authHeader) {
        emulatorService.verifyToken(authHeader);
        return ResponseEntity.ok(emulatorService.handleOn(request));
    }

    @PostMapping("/off")
    @Operation(summary = "애뮬레이터 시동 OFF", description = "애뮬레이터로부터 off 요청을 받습니다.")
    public ResponseEntity<StandardResponse> off(@RequestBody OnOffRequest request, @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authHeader) {
        emulatorService.verifyToken(authHeader);
        return ResponseEntity.ok(emulatorService.handleOff(request));
    }

    @PostMapping("/gps")
    @Operation(summary = "애뮬레이터 주기 정보", description = "애뮬레이터의 주기정보를 DB에 저장합니다.")
    public ResponseEntity<StandardResponse> gps(@RequestBody GpsCycleRequest request, @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authHeader) {
        emulatorService.verifyToken(authHeader);
        return ResponseEntity.ok(emulatorService.handleGps(request));
    }
}
