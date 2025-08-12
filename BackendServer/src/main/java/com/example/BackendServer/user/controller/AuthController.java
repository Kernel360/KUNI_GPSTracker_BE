package com.example.BackendServer.user.controller;

import com.example.BackendServer.user.model.request.*;
import com.example.BackendServer.user.model.response.*;
import com.example.BackendServer.user.service.CustomUserDetailsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {

    private final CustomUserDetailsService customUserDetailsService;

    @PostMapping("/sign-up")
    public SignUpResponse signUp(@RequestBody @Valid SignUpRequest req) {
        return customUserDetailsService.signUp(req);
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest req) {
        return customUserDetailsService.login(req);
    }

    @PostMapping("/id/duplicate")
    public IdCheckResponse checkId(@RequestBody IdCheckRequest req) {
        return customUserDetailsService.checkIdDuplicate(req);
    }
}
