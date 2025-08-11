package com.example.BackendServer.user.controller;

import com.example.BackendServer.user.db.UserEntity;
import com.example.BackendServer.user.db.UserRepository;
import com.example.BackendServer.user.jwt.JwtUtil;
import com.example.BackendServer.user.model.request.*;
import com.example.BackendServer.user.model.response.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @PostMapping("/sign-up")
    public SignUpResponse signUp(@RequestBody SignUpRequest req) {
        if (userRepository.existsById(req.getId())) {
            throw new RuntimeException("ID already exists");
        }

        UserEntity user = UserEntity.builder()
            .id(req.getId())
            .password(passwordEncoder.encode(req.getPassword()))
            .email(req.getEmail())
            .role(req.getRole())
            .build();

        userRepository.save(user);
        return new SignUpResponse(user.getId());
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest req) {
        var user = userRepository.findById(req.getId())
            .orElseThrow(() -> new RuntimeException("Invalid ID or password"));

        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid ID or password");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getRole());
        return new LoginResponse(token);
    }

    @PostMapping("/id/duplicate")
    public IdCheckResponse checkId(@RequestBody IdCheckRequest req) {
        boolean exists = userRepository.existsById(req.getId());
        return new IdCheckResponse(!exists);
    }
}
