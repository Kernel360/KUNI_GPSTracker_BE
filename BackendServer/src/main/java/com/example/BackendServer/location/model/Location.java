package com.example.BackendServer.location.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Location {
    @Schema(description = "위도", example = "35.943944")
    private Double latitude;
    @Schema(description = "경도", example = "127.546379")
    private Double longitude;
}