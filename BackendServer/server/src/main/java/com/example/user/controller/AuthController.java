package com.example.user.controller;

import com.example.user.model.request.IdCheckRequest;
import com.example.user.model.request.LoginRequest;
import com.example.user.model.request.SignUpRequest;
import com.example.user.model.response.IdCheckResponse;
import com.example.user.model.response.LoginResponse;
import com.example.user.model.response.SignUpResponse;
import com.example.user.model.response.TokenValidateResponse;
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
    public ResponseEntity<TokenValidateResponse> validateToken(
            @RequestHeader("Authorization") String authorizationHeader) {

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity.ok(new TokenValidateResponse(null, false));
        }

        String token = authorizationHeader.substring(7);
        TokenValidateResponse response = userService.validateTokenDTO(token);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/id/duplicate")
    public IdCheckResponse checkId(@RequestBody IdCheckRequest req) {
        return userService.checkIdDuplicate(req);
    }

}
