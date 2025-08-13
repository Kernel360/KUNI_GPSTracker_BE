package com.example.entity;

import com.example.entity.GpsRecordEntity;
import com.example.entity.VehicleEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@Entity
@Table(name = "record")
public class RecordEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // 외래 키
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "vehicle_id", nullable = false)
  private VehicleEntity vehicle;


  @OneToMany(mappedBy = "record")
  @ToString.Exclude
  @Builder.Default
  @JsonIgnore
  private List<GpsRecordEntity> gpsRecords = new ArrayList<>();

  @Column(name = "sum_dist", nullable = true)
  private String sumDist;

  @Column(name = "on_time", nullable = false)
  private LocalDateTime onTime;

  @Column(name = "off_time", nullable = true)
  private LocalDateTime offTime;

  // ✅ 필요한 메서드들 추가
  public void setOffTime(LocalDateTime offTime) {
    this.offTime = offTime;
  }

  public LocalDateTime getBaseTime() {
    return this.onTime;
  }
}