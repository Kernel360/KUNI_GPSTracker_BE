package com.example.car_emulator_server.service;

import com.example.car_emulator_server.model.TokenRequest;
import com.example.car_emulator_server.model.TokenResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenService {

    private final WebClient client;

    @Getter
    private volatile String token;   // 캐싱

    public void getTokenFromServer(String mdn) {
        if (token != null) return;
        TokenRequest req = TokenRequest.builder()
                .mdn(mdn).tid("A001").mid("6").pv("5").did("1").dFWVer("LTE1.2")
                .build();

        TokenResponse res = client.post()
                .uri("/token")
                .bodyValue(req)
                .retrieve()
                .bodyToMono(TokenResponse.class)
                .block();            // 최초 1회만 동기 블록

        token = res.getToken();
    }


}
