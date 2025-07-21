package com.example.BackendServer.record.controller;

import com.example.BackendServer.record.db.RecordEntity;
import com.example.BackendServer.record.model.RecordListResponse;
import com.example.BackendServer.record.model.RecordDetailResponse;
import com.example.BackendServer.record.model.RecordRequest;
import com.example.BackendServer.record.service.RecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/record")
@RequiredArgsConstructor
public class RecordApiController {

  private final RecordService recordService;

  /** 운행일지 생성 */
  @PostMapping
  public RecordEntity create(@Valid @RequestBody RecordRequest recordRequest) {
    return recordService.create(recordRequest);
  }

  /** 운행일지 목록 조회 */
  @GetMapping
  public Page<RecordListResponse> getRecordList(
          @RequestParam(required = false) String vehicleNumber,
          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
          @PageableDefault(size = 7) Pageable pageable
  ) {
    return recordService.getRecordList(vehicleNumber, startTime, endTime, pageable);
  }

  /** 운행일지 상세 조회 */
  @GetMapping("/{id}")
  public RecordDetailResponse getRecordDetail(@PathVariable Long id) {
    return recordService.getRecordDetail(id);
  }
}
