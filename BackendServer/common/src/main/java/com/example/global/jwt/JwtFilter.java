package com.example.global.jwt;

import com.example.entity.TokenEntity;
import com.example.entity.TokenStatus;
import com.example.repository.TokenRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final TokenRepository tokenRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        String token = null;
        String username = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            username = jwtUtil.extractUsername(token);
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // 1) DB에 VALID 상태로 저장돼 있는 토큰인지 확인
            var opt = tokenRepository.findByAccessTokenAndStatus(token, TokenStatus.VALID);
            if (opt.isPresent()) {
                TokenEntity tokenRow = opt.get();

                // 2) 만료 체크 (만료면 자동으로 INVALID로 바꿔줌 - 선택)
                if (tokenRow.getExpiresAt().isBefore(LocalDateTime.now())) {
                    tokenRow.setStatus(TokenStatus.INVALID);
                    tokenRepository.save(tokenRow);
                } else {
                    // 3) JWT 자체 서명/만료 검증
                    if (jwtUtil.validateToken(token)) {
                        String role = jwtUtil.extractRole(token);
                        var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));

                        UsernamePasswordAuthenticationToken authToken =
                                new UsernamePasswordAuthenticationToken(username, null, authorities);
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    }
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
