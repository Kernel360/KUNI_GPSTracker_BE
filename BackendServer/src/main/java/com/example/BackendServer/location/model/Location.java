package com.example.BackendServer.location.model;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Location {
    private Double latitude;
    private Double longitude;
}