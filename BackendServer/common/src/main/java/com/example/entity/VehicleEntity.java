package com.example.entity;

import com.example.global.Class.VehicleStatus;
import com.example.global.Class.VehicleType;
import com.example.entity.DeviceEntity;
import com.example.entity.GpsRecordEntity;
import com.example.entity.RecordEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true) // ✅ toBuilder 추가
@ToString
@Entity
@Table(name = "vehicle")  // DB 테이블 이름과 매핑
public class VehicleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")  // 기본 키
    private Long id;

    @Column(name = "vehicle_number", nullable = false, length = 20,unique = true)
    private String vehicleNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private VehicleStatus status;

    @Column(name = "total_dist", nullable = false)
    private Long totalDist;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private VehicleType type;

    @Column(name = "create_date", nullable = false)
    private LocalDateTime createDate;

    @OneToOne(mappedBy = "vehicle", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    private DeviceEntity device;

    @OneToMany(mappedBy = "vehicle", cascade = CascadeType.ALL)
    @ToString.Exclude
    @Builder.Default
    @JsonIgnore
    private List<RecordEntity> records = new ArrayList<>();

    @OneToMany(mappedBy = "vehicle", cascade = CascadeType.ALL)
    @ToString.Exclude
    @Builder.Default
    @JsonIgnore
    private List<GpsRecordEntity> gpsRecords = new ArrayList<>();

}
