package com.example.vehicle;

import com.example.global.Class.VehicleStatus;
import com.example.global.Class.VehicleType;
import com.example.entity.VehicleEntity;
import com.example.repository.VehicleRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;

@Component
@RequiredArgsConstructor
public class VehicleDataInitializer {

  private final VehicleRepository vehicleRepository;

  private final List<String> vehicleNumbers = List.of(
      "37허9534", "77하9129", "47하7226", "18하9305", "42호3796", "89하8148", "25하2382", "88허2646", "64허1225", "99하5525",
      "26호6540", "50호2768", "94호8121", "19하5914", "70하3860", "20호9563", "79허1985", "72하7318", "69하4844", "47호5239"

  );

  @PostConstruct
  public void initVehicles() {
    if (vehicleRepository.count() > 0) return;  // 이미 데이터가 있다면 초기화하지 않음
    IntStream.range(0, vehicleNumbers.size()).forEach(i -> {
      String number = vehicleNumbers.get(i);

      VehicleType type = switch ((i + 1) % 3) {
        case 1 -> VehicleType.MERCEDES;
        case 2 -> VehicleType.FERRARI;
        default -> VehicleType.PORSCHE;
      };

      VehicleEntity vehicle = VehicleEntity.builder()
          .vehicleNumber(number)
          .type(type)
          .status(VehicleStatus.INACTIVE)
          .totalDist(0L)
          .createDate(LocalDateTime.now())
          .build();

      vehicleRepository.save(vehicle);
    });
  }
}