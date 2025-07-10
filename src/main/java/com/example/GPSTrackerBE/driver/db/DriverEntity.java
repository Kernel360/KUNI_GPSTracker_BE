package com.example.GPSTrackerBE.driver.db;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Table;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@Entity
@Table(name = "Driver")  // DB 테이블명과 일치시킴
public class DriverEntity {

  @Id
  @GeneratedValue(strategy= GenerationType.IDENTITY)
  @Column(name = "driver_id")  // DB 컬럼명과 매핑
  private Long id;

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "age")
  private Integer age;

  @Column(name = "phone")
  private String phone;
}
