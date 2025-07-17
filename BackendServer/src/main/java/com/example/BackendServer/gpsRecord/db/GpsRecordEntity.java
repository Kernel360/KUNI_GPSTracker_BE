package com.example.BackendServer.gpsRecord.db;

import com.example.BackendServer.driver.db.DriverEntity;
import com.example.BackendServer.record.db.RecordEntity;
import com.example.BackendServer.vehicle.db.VehicleEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Builder
@Entity
@Getter
@Table(name = "GpsRecord")
public class GpsRecordEntity {

  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  //@Column(name = "gps_record_id")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "record_id")
  private RecordEntity record;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "vehicle_id", nullable = false)
  private VehicleEntity vehicle;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "driver_id", nullable = false)
  private DriverEntity driver;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private Status status;

  @Column(name = "latitude", nullable = false)
  private Double latitude;

  @Column(name = "longitude", nullable = false)
  private Double longitude;

  @Column(name = "oTime", nullable = false)
  private LocalDateTime oTime;

  @Column(name = "gcd", nullable = false)
  private String gcd;

  @Column(name = "total_dist", nullable = false)
  private String totalDist;

  public enum Status {
    ACTIVE, INACTIVE, INSPECTING
  }
}
