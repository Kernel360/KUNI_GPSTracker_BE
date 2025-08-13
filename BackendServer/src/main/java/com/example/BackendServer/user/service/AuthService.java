package com.example.BackendServer.user.service;

import com.example.BackendServer.global.exception.CustomException;
import com.example.BackendServer.global.exception.ErrorCode;
import com.example.BackendServer.global.jwt.JwtUtil;
import com.example.BackendServer.user.db.UserEntity;
import com.example.BackendServer.user.db.UserRepository;
import com.example.BackendServer.user.db.UserRole;
import com.example.BackendServer.user.model.request.IdCheckRequest;
import com.example.BackendServer.user.model.request.LoginRequest;
import com.example.BackendServer.user.model.request.SignUpRequest;
import com.example.BackendServer.user.model.response.IdCheckResponse;
import com.example.BackendServer.user.model.response.LoginResponse;
import com.example.BackendServer.user.model.response.SignUpResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public SignUpResponse signUp(SignUpRequest req) {
        if (userRepository.existsById(req.getId())) {
            throw new CustomException(ErrorCode.DUPLICATE_ID);
        }

        UserRole userRole;
        try {
            userRole = UserRole.valueOf(req.getRole().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new CustomException(ErrorCode.INVALID_ROLE);
        }

        UserEntity user = UserEntity.builder()
                .id(req.getId())
                .password(passwordEncoder.encode(req.getPassword()))
                .email(req.getEmail())
                .role(userRole)
                .build();

        userRepository.save(user);
        return new SignUpResponse(user.getId());
    }

    public LoginResponse login(LoginRequest req) {
        var user = userRepository.findById(req.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_ID_OR_PASSWORD));

        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_ID_OR_PASSWORD);
        }

        String token = jwtUtil.generateToken(user.getId(), user.getRole());
        return new LoginResponse(token);
    }

    public IdCheckResponse checkIdDuplicate(IdCheckRequest req) {
        boolean exists = userRepository.existsById(req.getId());
        return new IdCheckResponse(!exists);
    }
}
