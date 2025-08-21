package com.example.service;

import com.example.entity.GpsRecordEntity;
import com.example.entity.RecordEntity;
import com.example.entity.VehicleEntity;
import com.example.exception.CustomException;
import com.example.exception.ErrorCode;
import com.example.repository.GpsRecordRepository;
import com.example.repository.RecordRepository;

import com.example.Response.RecordDetailResponse;
import com.example.Response.RecordListResponse;
import com.example.Response.RecordRequest;

import com.example.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecordService {

  private final RecordRepository recordRepository;
  private final VehicleRepository vehicleRepository;
  private final GpsRecordRepository gpsRecordRepository;

  /** 운행일지 생성 */
  @Transactional
  public RecordEntity create(RecordRequest recordRequest) {
    VehicleEntity vehicle = vehicleRepository.findById(recordRequest.getVehicleId())
            .orElseThrow(() -> new CustomException(ErrorCode.VEHICLE_NOT_FOUND)); // 차량이 존재하지 않을 때

    RecordEntity record = RecordEntity.builder()
            .vehicle(vehicle)
            .sumDist(recordRequest.getSumDist())
            .onTime(recordRequest.getOnTime())
            .offTime(recordRequest.getOffTime())
            .build();

    return recordRepository.save(record);
  }

  /** 운행일지 목록 조회 */
  public Page<RecordListResponse> getRecordList(String vehicleNumber,
                                                LocalDateTime startTime,
                                                LocalDateTime endTime,
                                                Pageable pageable) {
    Page<RecordEntity> page = recordRepository.searchRecords(vehicleNumber, startTime, endTime, pageable);
    return page.map(RecordListResponse::from);
  }

  /** 운행일지 상세 조회 */
  public RecordDetailResponse getRecordDetail(Long id) {
    RecordEntity record = recordRepository.findById(id)
            .orElseThrow(() -> new CustomException(ErrorCode.RECORD_NOT_FOUND)); // 기록이 존재하지 않을 때

    List<GpsRecordEntity> gpsList = gpsRecordRepository.findByRecordIdOrderByOTime(id);

    if (gpsList.isEmpty()) {
      throw new CustomException(ErrorCode.GPS_RECORD_NOT_FOUND); // GPS 데이터가 존재하지 않을 때
    }

    Double startLat = null;
    Double startLng = null;
    Double endLat = null;
    Double endLng = null;

    if (!gpsList.isEmpty()) {
      startLat = gpsList.get(0).getLatitude();
      startLng = gpsList.get(0).getLongitude();
      endLat = gpsList.get(gpsList.size() - 1).getLatitude();
      endLng = gpsList.get(gpsList.size() - 1).getLongitude();
    }

    return RecordDetailResponse.from(record, startLat, startLng, endLat, endLng, gpsList);
  }

}
