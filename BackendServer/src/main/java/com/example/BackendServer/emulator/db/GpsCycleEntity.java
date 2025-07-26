package com.example.BackendServer.emulator.db;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "gps_cycle")
public class GpsCycleEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String mdn;

  @Column
  private String tid;

  @Column
  private String mid;

  @Column
  private String pv;

  @Column
  private String did;

  @Column
  private String gcd;

  @Column(nullable = false)
  private double lat;

  @Column(nullable = false)
  private double lon;

  @Column
  private int ang;

  @Column
  private int spd;

  @Column
  private int sum;

  @Column
  private int bat;  // 여기 추가

  @Column(nullable = false)
  private LocalDateTime time;

  @Column(nullable = false)
  private String token;
}
