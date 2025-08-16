package com.example.user.db;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "auth_token", indexes = {
        @Index(name = "idx_auth_token_login_id", columnList = "login_id"),
        @Index(name = "idx_auth_token_status", columnList = "token_status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "login_id", nullable = false)
    private String loginId;

    @Column(name = "access_token", nullable = false, unique = true, length = 512)
    private String accessToken;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "token_status", nullable = false, length = 16)
    private TokenStatus status;

    @Transient
    public boolean isActiveNow() {
        return status == TokenStatus.VALID && expiresAt.isAfter(LocalDateTime.now());
    }
}


