package com.example.BackendServer.global.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

import com.example.BackendServer.user.db.UserRole;

@Component
public class JwtUtil {

    private final Key secretKey = Keys.hmacShaKeyFor(
        "very-secret-key-very-secret-key-123456".getBytes()
    );
    private final long expiration = 86400000; // 1일

    // 토큰 생성 시 username과 role을 클레임에 포함
    public String generateToken(String username, UserRole role) {
        return Jwts.builder()
            .setSubject(username)
            .claim("role", role.name())  // enum명 문자열로 저장
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + expiration))
            .signWith(secretKey, SignatureAlgorithm.HS256)
            .compact();
    }

    public String extractUsername(String token) {
        return getClaims(token).getSubject();
    }

    public String extractRole(String token) {
        return (String) getClaims(token).get("role");
    }

    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(secretKey)
            .build()
            .parseClaimsJws(token)
            .getBody();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }
}
