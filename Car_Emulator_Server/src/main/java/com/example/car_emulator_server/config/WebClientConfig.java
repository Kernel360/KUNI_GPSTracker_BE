package com.example.car_emulator_server.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Configuration
@RequiredArgsConstructor
public class WebClientConfig {

    @Value("${emulator.target-base-url}")
    private String targetBaseUrl;

    DateTimeFormatter tsFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
            .withZone(ZoneId.systemDefault());

    @Bean
    public WebClient emulatorWebClient() {
        return WebClient.builder()
                .baseUrl(targetBaseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT,        MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.CACHE_CONTROL, "no-cache")
                .defaultHeader(HttpHeaders.ACCEPT_ENCODING, "gzip, deflate")
                .defaultHeader("Key-Version", "1.0")

                .filter((req, next) -> {
                    ClientRequest newReq = ClientRequest.from(req)
                            .header("Timestamp", tsFmt.format(Instant.now()))
                            .header("TUID", UUID.randomUUID().toString())
                            .build();
                    return next.exchange(newReq);
                })
                .build();
    }
}
