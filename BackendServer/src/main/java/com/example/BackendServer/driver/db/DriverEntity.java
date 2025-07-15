package com.example.BackendServer.driver.db;

import jakarta.persistence.*;
import lombok.*;

@Getter
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
