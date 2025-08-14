package com.example.entity;

import jakarta.persistence.*;
import lombok.*;
import com.example.entity.VehicleEntity;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@Entity
@Table(name = "device")
public class DeviceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
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
