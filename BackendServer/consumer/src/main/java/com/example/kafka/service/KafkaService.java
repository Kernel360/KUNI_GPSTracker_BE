package com.example.kafka.service;


import com.example.entity.GpsRecordEntity;
import com.example.entity.RecordEntity;
import com.example.entity.VehicleEntity;
import com.example.exception.CustomException;
import com.example.exception.ErrorCode;
import com.example.kafka.model.GpsMsg;
import com.example.kafka.model.OnOffMsg;
import com.example.Response.RecordRequest;
import com.example.repository.GpsRecordRepository;
import com.example.repository.RecordRepository;
import com.example.repository.VehicleRepository;
import com.example.service.RecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static com.example.model.VehicleStatus.ACTIVE;
import static com.example.model.VehicleStatus.INACTIVE;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaService {

    private final VehicleRepository vehicleRepository;
    private final RecordRepository recordRepository;
    private final RecordService recordService;
    private final GpsRecordRepository gpsRecordRepository;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    public void handleGps(GpsMsg gps) {
        VehicleEntity vehicle = getVehicleByMdn(gps.getMdn());

        RecordEntity activeRecord = recordRepository.findTopByVehicleIdAndOffTimeIsNullOrderByOnTimeDesc(vehicle.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.RECORD_NOT_FOUND));

        List<GpsRecordEntity> entities = gps.getCList().stream()
                .map(data -> {
                    LocalDateTime oTime = LocalDateTime.parse(gps.getOTime() + "00", formatter).plusSeconds(data.getSec());

                    return GpsRecordEntity.builder()
                            .record(activeRecord)
                            .vehicle(vehicle)
                            .gcd(data.getGcd())
                            .latitude(parseLat(data.getLat()))
                            .longitude(parseLon(data.getLon()))
                            .oTime(oTime)
                            .status(ACTIVE)
                            .totalDist(data.getSum())
                            .build();
                })
                .toList();

        gpsRecordRepository.saveAll(entities);
    }

    public void handleOn(OnOffMsg on) {
        VehicleEntity vehicle = getVehicleByMdn(on.getMdn());

        RecordRequest recordReq = RecordRequest.builder()
                .vehicleId(vehicle.getId())
                .onTime(LocalDateTime.parse(on.getOnTime(), formatter))
                .build();

        recordService.create(recordReq);

        vehicle = vehicle.toBuilder()
                .status(ACTIVE)
                .build();
        vehicleRepository.save(vehicle);
    }

    public void handleOff(OnOffMsg off) {
        VehicleEntity vehicle = getVehicleByMdn(off.getMdn());

        RecordEntity activeRecord = recordRepository.findTopByVehicleIdAndOffTimeIsNullOrderByOnTimeDesc(vehicle.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.RECORD_NOT_FOUND));

        // 1. offTime, sumDist 저장
        activeRecord.setOffTime(LocalDateTime.parse(off.getOffTime(), formatter));
        activeRecord.setSumDist(off.getSum());  // sum 필드 활용
        recordRepository.save(activeRecord);

        // 2. sumDist → totalDist로 변환 및 Vehicle 업데이트
        String sumDistStr = activeRecord.getSumDist();
        Long sumDistLong = 0L;

        if (sumDistStr != null) {
            try {
                sumDistLong = Long.parseLong(sumDistStr);
            } catch (NumberFormatException e) {
                log.warn("sumDist 변환 실패: {}", sumDistStr);
            }
        }

        // 기존 totalDist + 이번 주행 거리 누적
        Long updatedTotalDist = vehicle.getTotalDist() + sumDistLong;

        vehicle = vehicle.toBuilder()
                .status(INACTIVE)
                .totalDist(updatedTotalDist)
                .build();
        vehicleRepository.save(vehicle);
    }

    private VehicleEntity getVehicleByMdn(String mdn) {
        return vehicleRepository.findByVehicleNumber(mdn)
                .orElseThrow(() -> new CustomException(ErrorCode.VEHICLE_NOT_FOUND));
    }

    /** '앞 2자리.나머지' 형태로 위도 변환 */
    private static double parseLat(String raw) {
        if (raw.contains(".")) return Double.parseDouble(raw);   // 이미 변환돼 있음
        return Double.parseDouble(raw.substring(0, 2) + "." + raw.substring(2));
    }

    /** '앞 3자리.나머지' 형태로 경도 변환 */
    private static double parseLon(String raw) {
        if (raw.contains(".")) return Double.parseDouble(raw);
        return Double.parseDouble(raw.substring(0, 3) + "." + raw.substring(3));
    }
}
