package com.example.user.service;

import com.example.global.exception.CustomException;
import com.example.global.exception.ErrorCode;
import com.example.global.jwt.JwtUtil;
import com.example.entity.TokenEntity;
import com.example.repository.TokenRepository;
import com.example.entity.TokenStatus;
import com.example.user.db.UserEntity;
import com.example.user.db.UserRepository;
import com.example.user.model.request.*;
import com.example.user.model.response.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.db.UserRole;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final TokenRepository tokenRepository;

    @Override
    public UserDetails loadUserByUsername(String id) throws UsernameNotFoundException {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (user.getRole() == null) {
            throw new CustomException(ErrorCode.INVALID_ROLE);
        }

        return User.builder()
                .username(user.getId())
                .password(user.getPassword())
                .authorities("ROLE_" + user.getRole().name())
                .build();
    }

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

    /**
     * 로그인: 인증 성공 -> JWT 발급 -> 토큰 테이블에 저장
     */
    public LoginResponse login(LoginRequest req) {
        var user = userRepository.findById(req.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_ID_OR_PASSWORD));

        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_ID_OR_PASSWORD);
        }

        String token = jwtUtil.generateToken(user.getId(), user.getRole());

        // (선택) 정책: 새 로그인 시, 기존 VALID 토큰을 모두 INVALID로 변경하고 단일 세션만 유지하고 싶다면 아래 주석 해제
        // tokenRepository.findByLoginIdAndStatus(user.getId(), TokenStatus.VALID)
        //         .forEach(t -> { t.setStatus(TokenStatus.INVALID); tokenRepository.save(t); });

        LocalDateTime now = LocalDateTime.now();
        TokenEntity tokenRow = TokenEntity.builder()
                .loginId(user.getId())
                .accessToken(token)
                .createdAt(now)
                .expiresAt(now.plusDays(1)) // JwtUtil의 만료시간과 일치하도록 맞추세요
                .status(TokenStatus.VALID)
                .build();
        tokenRepository.save(tokenRow);

        return new LoginResponse(token);
    }

    /**
     * 로그아웃: 전달된 토큰을 INVALID로 변경
     */
    public void logout(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return; // 혹은 CustomException 던져도 됨
        }
        String token = authorizationHeader.substring(7);
        tokenRepository.findByAccessTokenAndStatus(token, TokenStatus.VALID)
                .ifPresent(t -> {
                    t.setStatus(TokenStatus.INVALID);
                    tokenRepository.save(t);
                });
    }

    /**
     * 프런트가 준 토큰이 현재 사용 가능한지 검사
     * - DB에 VALID로 존재
     * - expiresAt > now
     * - JWT 자체 검증 통과
     */
    public boolean isTokenActive(String token) {
        return tokenRepository.findByAccessTokenAndStatus(token, TokenStatus.VALID)
                .filter(TokenEntity::isActiveNow)
                .filter(t -> jwtUtil.validateToken(t.getAccessToken()))
                .isPresent();
    }

    public IdCheckResponse checkIdDuplicate(IdCheckRequest req) {
        boolean exists = userRepository.existsById(req.getId());
        return new IdCheckResponse(!exists);
    }
}
