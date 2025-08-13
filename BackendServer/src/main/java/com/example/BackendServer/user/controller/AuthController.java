package com.example.BackendServer.user.controller;

import com.example.BackendServer.user.model.request.*;
import com.example.BackendServer.user.model.response.*;
import com.example.BackendServer.user.service.AuthService;
import com.example.BackendServer.user.service.CustomUserDetailsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/sign-up")
    public SignUpResponse signUp(@RequestBody @Valid SignUpRequest req) {
        return authService.signUp(req);
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest req) {
        return authService.login(req);
    }

    @PostMapping("/id/duplicate")
    public IdCheckResponse checkId(@RequestBody IdCheckRequest req) {
        return authService.checkIdDuplicate(req);
    }
}
