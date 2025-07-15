package com.example.BackendServer.device.db;

import jakarta.persistence.*;
import lombok.*;
import com.example.BackendServer.vehicle.db.VehicleEntity;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@Entity
@Table(name = "Device")
public class DeviceEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // 외래 키
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "vehicle_id", nullable = false)
  private VehicleEntity vehicle;

  @Column(name = "terminal_id", nullable = false)
  private String terminalId;

  @Column(name = "mid", nullable = false)
  private String mid;

  @Column(name = "pv", nullable = false)
  private String pv;

  @Column(name = "did", nullable = false)
  private String did;
}
