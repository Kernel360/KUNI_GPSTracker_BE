package com.example.user.controller;

import com.example.user.model.request.LoginRequest;
import com.example.user.model.request.SignUpRequest;
import com.example.user.model.response.LoginResponse;
import com.example.user.model.response.SignUpResponse;
import com.example.user.service.CustomUserDetailsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class AuthController {

    private final CustomUserDetailsService userService;

    @PostMapping("/sign-up")
    public SignUpResponse signUp(@RequestBody @Valid SignUpRequest req) {
        return userService.signUp(req);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest req) {
        return ResponseEntity.ok(userService.login(req));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String authorization) {
        userService.logout(authorization);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/token/validate")
    public ResponseEntity<Boolean> validate(@RequestHeader("Authorization") String authorization) {
        String token = authorization != null && authorization.startsWith("Bearer ")
                ? authorization.substring(7) : authorization;
        return ResponseEntity.ok(userService.isTokenActive(token));
    }
}
