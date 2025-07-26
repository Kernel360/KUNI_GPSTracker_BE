package com.example.BackendServer.emulator.db;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "on_off_record")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OnOffRecordEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String mdn;      // 차량번호
  private String tid;      // 단말기 TID
  private String mid;      // 단말기 MID
  private String pv;       // 프로토콜 버전
  private String did;      // 단말기 ID

  private String gcd;      // GPS 상태 코드
  private double lat;      // 위도
  private double lon;      // 경도
  private int ang;      // 각도
  private int spd;      // 속도
  private int sum;      // 검증값 등

  private String type;     // "ON" 또는 "OFF"
  private LocalDateTime timestamp;  // 시동 시간
}
