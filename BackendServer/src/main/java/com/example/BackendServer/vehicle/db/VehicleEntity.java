package com.example.BackendServer.vehicle.db;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

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

  // status enum 정의
  public enum Status {
    ACTIVE, INACTIVE, INSPECTING
  }

  // type enum 정의
  public enum Type {
    MERCEDES, FERRARI, PORSCHE
  }
}
