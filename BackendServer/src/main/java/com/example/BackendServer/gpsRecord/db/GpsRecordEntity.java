package com.example.BackendServer.gpsRecord.db;


import com.example.BackendServer.record.db.RecordEntity;
import com.example.BackendServer.vehicle.db.VehicleEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor

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
