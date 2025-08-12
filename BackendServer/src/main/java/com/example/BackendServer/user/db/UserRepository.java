package com.example.BackendServer.user.db;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findById(String id);  // 로그인 ID로 사용자 조회
    boolean existsById(String id);              // ID 중복 체크
}
