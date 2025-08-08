package com.example.BackendServer.user.controller;

import com.example.BackendServer.user.db.UserEntity;
import com.example.BackendServer.user.db.UserRepository;
import com.example.BackendServer.user.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @PostMapping("/sign-up")
    public Map<String, Object> signUp(@RequestBody UserEntity req) {
        if (userRepository.existsById(req.getId())) {
            throw new RuntimeException("ID already exists");
        }
        req.setPassword(passwordEncoder.encode(req.getPassword()));
        userRepository.save(req);
        return Map.of("id", req.getId());
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> req) {
        var user = userRepository.findById(req.get("id"))
            .orElseThrow(() -> new RuntimeException("Invalid ID or password"));

        if (!passwordEncoder.matches(req.get("password"), user.getPassword())) {
            throw new RuntimeException("Invalid ID or password");
        }

        // role 포함해서 토큰 생성
        String token = jwtUtil.generateToken(user.getId(), user.getRole());

        return Map.of("token", token);
    }

    @PostMapping("/id/duplicate")
    public Map<String, Boolean> checkId(@RequestBody Map<String, String> req) {
        boolean exists = userRepository.existsById(req.get("id"));
        return Map.of("isOk", !exists);
    }
}
