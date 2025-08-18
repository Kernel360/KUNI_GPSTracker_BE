package com.example.user.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor

public class TokenValidateResponse {
    private String loginId; // 유효하면 로그인 ID, 아니면 null
    private boolean valid;  // 토큰 유효 여부
}
