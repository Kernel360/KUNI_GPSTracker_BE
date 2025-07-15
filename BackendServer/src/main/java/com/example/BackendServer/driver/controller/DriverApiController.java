package com.example.BackendServer.driver.controller;

import com.example.BackendServer.driver.db.DriverEntity;
import com.example.BackendServer.driver.model.DriverRequest;
import com.example.BackendServer.driver.service.DriverService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/driver")
@RequiredArgsConstructor
public class DriverApiController {

  private final DriverService driverService;

  @PostMapping("")
  public DriverEntity create(@Valid @RequestBody DriverRequest driverRequest) {
    return driverService.create(driverRequest);
  }
}
