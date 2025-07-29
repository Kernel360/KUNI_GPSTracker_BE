package com.example.BackendServer.record.controller;

import com.example.BackendServer.record.db.RecordEntity;
import com.example.BackendServer.record.model.RecordListResponse;
import com.example.BackendServer.record.model.RecordDetailResponse;
import com.example.BackendServer.record.model.RecordRequest;
import com.example.BackendServer.record.service.RecordService;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/record")
@RequiredArgsConstructor
public class RecordApiController {

  private final RecordService recordService;

  /** 운행일지 생성 */
  @PostMapping
  @Operation(summary = "운행일지 생성", description = "운행일지를 생성합니다.")
  public ResponseEntity<RecordEntity> create(@Valid @RequestBody RecordRequest recordRequest) {
    //TODO : 응답 DTO로 변경
    return ResponseEntity.ok().body(recordService.create(recordRequest));
  }

  /** 운행일지 목록 조회 */
  @GetMapping
  @Operation(summary = "운행일지 목록 조회", description = "운행일지 목록을 조회합니다.")
  public Page<RecordListResponse> getRecordList(
          @RequestParam(required = false) String vehicleNumber,
          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
          @ParameterObject @PageableDefault(size = 7) Pageable pageable
  ) {
    return recordService.getRecordList(vehicleNumber, startTime, endTime, pageable);
  }

  /** 운행일지 상세 조회 */
  @GetMapping("/{id}")
  @Operation(summary = "운행일지 상세 조회", description = "운행일지의 상세 정보를 조회합니다.")
  public RecordDetailResponse getRecordDetail(@PathVariable Long id) {
    return recordService.getRecordDetail(id);
  }
}
