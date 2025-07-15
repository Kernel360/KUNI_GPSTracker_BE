package com.example.BackendServer.driver.service;

import com.example.BackendServer.driver.db.DriverEntity;
import com.example.BackendServer.driver.db.DriverRepository;
import com.example.BackendServer.driver.model.DriverRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DriverService {

  private final DriverRepository driverRepository;

  public DriverEntity create(DriverRequest driverRequest) {
    DriverEntity driver = DriverEntity.builder()
        .name(driverRequest.getName())
        .age(driverRequest.getAge())
        .phone(driverRequest.getPhone())
        .build();
    return driverRepository.save(driver);
  }
}
