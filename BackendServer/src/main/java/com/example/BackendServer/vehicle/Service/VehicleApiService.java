package com.example.BackendServer.vehicle.Service;

import com.example.BackendServer.vehicle.db.VehicleEntity;
import com.example.BackendServer.vehicle.db.VehicleRepository;
import com.example.BackendServer.vehicle.model.VehicleCreateDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class VehicleApiService {

    private final VehicleRepository vehicleRepository;

    /**
     * 차량 등록 : Repository에 차량 entity를 등록한다.
     *
     * @param dto : vehicleNumber와 vehicleName을 field로 가진다.
     * @return VehicleEntity  : dto를 이용해 Builder로 Entity 생성 후 반환
     */
    public VehicleEntity createVehicle(VehicleCreateDto dto) {

        VehicleEntity entity = VehicleEntity.builder()
                .vehicleNumber(dto.getVehicleNumber())
                .status(VehicleEntity.Status.INACTIVE)
                .totalDist(0L)
                .type(VehicleEntity.Type.valueOf(dto.getVehicleName()))
                .createDate(LocalDateTime.now())
                .build();

        VehicleEntity saved = vehicleRepository.save(entity);
        return saved;
    }
}
