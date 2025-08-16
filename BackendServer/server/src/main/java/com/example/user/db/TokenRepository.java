package com.example.user.db;

import com.example.entity.TokenEntity;
import com.example.entity.TokenStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TokenRepository extends JpaRepository<TokenEntity, Long> {

    Optional<TokenEntity> findByAccessTokenAndStatus(String accessToken, TokenStatus status);

    List<TokenEntity> findByLoginIdAndStatus(String loginId, TokenStatus status);
}
