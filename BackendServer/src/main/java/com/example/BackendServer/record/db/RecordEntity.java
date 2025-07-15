package com.example.BackendServer.record.db;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import com.example.BackendServer.vehicle.db.VehicleEntity;
import com.example.BackendServer.driver.db.DriverEntity;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@Entity
@Table(name = "Record")
public class RecordEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // 외래 키
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "vehicle_id", nullable = false)
  private VehicleEntity vehicle;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "driver_id", nullable = false)
  private DriverEntity driver;

  @Column(name = "sum_dist", nullable = false)
  private String sumDist;

  @Column(name = "on_time", nullable = false)
  private LocalDateTime onTime;

  @Column(name = "off_time", nullable = false)
  private LocalDateTime offTime;
}
