package com.example.repository;

import com.example.entity.DeviceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeviceRepository extends JpaRepository<DeviceEntity, Long> {

}
