package com.example.emulator.service;

import com.example.emulator.model.MDT;
import com.example.emulator.model.TokenRequest;
import com.example.emulator.model.TokenResponse;
import com.example.emulator.util.TokenHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenService {

    private final WebClient client;
    private final ConcurrentHashMap<String, TokenHolder> tokens = new ConcurrentHashMap<>();

    public String getToken(String mdn) {
        // compute() = 스레드 안전하게 갱신
        return tokens.compute(mdn, (key, holder) -> {
            if (holder == null || holder.isExpired()) {
                return fetchToken(key);                // REST 호출
            }
            return holder;                             // 그대로 재사용
        }).getToken();
    }

    private TokenHolder fetchToken(String mdn) {

        TokenRequest req = TokenRequest.builder()
                .mdn(mdn)
                .mdt(MDT.decidedMDT())
                .dFWVer("LTE1.2")
                .build();

        TokenResponse res = client.post()
                .uri("/token")
                .bodyValue(req)
                .retrieve()
                .bodyToMono(TokenResponse.class)
                .block();                // 1회 동기 대기

        assert res != null;
        Instant expires = Instant.now().plusSeconds(60L *60*Integer.parseInt(res.getExPeriod()));
        log.info("[{}] 새 토큰 발급, 만료={}", res.getToken(), expires);
        return new TokenHolder(res.getToken(), expires);
    }




}
