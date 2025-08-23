package com.example.vehicle.model;

import com.example.model.VehicleType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Schema(description = "차량 생성 요청 DTO")
public class VehicleCreateDto {

    @NotBlank
    @Pattern(regexp = "^[0-9]{2,3}[하허호][0-9]{4}$",
            message = "차량 번호 형식이 올바르지 않습니다. '하/허/호' 형식으로 입력하세요.")
    @Schema(description = "차량 번호", example = "12하3456")
    private String vehicleNumber;

    @Schema(description = "차량 이름", example = "MERCEDES")
    private VehicleType vehicleName;
}
