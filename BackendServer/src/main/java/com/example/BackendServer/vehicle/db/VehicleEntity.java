package com.example.BackendServer.vehicle.db;

import com.example.BackendServer.device.db.DeviceEntity;
import com.example.BackendServer.gpsRecord.db.GpsRecordEntity;
import com.example.BackendServer.record.db.RecordEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@Entity
@Table(name = "Vehicle")  // DB 테이블 이름과 매핑
public class VehicleEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")  // 기본 키
  private Long id;

  @Column(name = "vehicle_number", nullable = false, length = 20)
  private String vehicleNumber;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private Status status;

  @Column(name = "total_dist", nullable = false)
  private Long totalDist;

  @Enumerated(EnumType.STRING)
  @Column(name = "type", nullable = false)
  private Type type;

  @Column(name = "create_date", nullable = false)
  private LocalDateTime createDate;

  @OneToOne(mappedBy = "vehicle", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @ToString.Exclude
  private DeviceEntity device;

  @OneToMany(mappedBy = "vehicle", cascade = CascadeType.ALL)
  @ToString.Exclude
  @Builder.Default
  private List<RecordEntity> records = new ArrayList<>();

  @OneToMany(mappedBy = "vehicle", cascade = CascadeType.ALL)
  @ToString.Exclude
  @Builder.Default
  private List<GpsRecordEntity> gpsRecords = new ArrayList<>();
  // status enum 정의
  public enum Status {
    ACTIVE, INACTIVE, INSPECTING
  }

  // type enum 정의
  public enum Type {
    MERCEDES, FERRARI, PORSCHE
  }
}
