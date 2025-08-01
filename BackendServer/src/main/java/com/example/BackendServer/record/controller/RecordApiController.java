package com.example.BackendServer.record.controller;

import com.example.BackendServer.record.db.RecordEntity;
import com.example.BackendServer.record.model.RecordListResponse;
import com.example.BackendServer.record.model.RecordDetailResponse;
import com.example.BackendServer.record.model.RecordRequest;
import com.example.BackendServer.record.service.RecordService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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

//  /** 운행일지 생성 */
//  @PostMapping
//  @Operation(summary = "운행일지 생성", description = "운행일지를 생성합니다.")
//  public ResponseEntity<RecordEntity> create(@Valid @RequestBody RecordRequest recordRequest) {
//    //TODO : 응답 DTO로 변경
//    return ResponseEntity.ok().body(recordService.create(recordRequest));
//  }

  /** 운행일지 목록 조회 */
  @GetMapping
  @Operation(
          summary = "운행일지 목록 조회",
          description = "차량 번호·기간 조건으로 운행일지를 페이지 단위로 조회합니다.",
          responses = {
                  @ApiResponse(
                          responseCode = "200",
                          description = "조회 성공",
                          content = @Content(
                                  mediaType = "application/json",
                                  schema = @Schema(implementation = RecordListResponse.class), // ← 스키마(아래 참고)
                                  examples = @ExampleObject(name = "success", value = """
{
  "content": [
    {
      "id": 2,
      "vehicleNumber": "37허9534",
      "vehicleName": "MERCEDES",
      "onTime": "2025-07-26T11:23:36",
      "offTime": "2025-07-26T11:27:35",
      "sumDist": "2437"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 7,
    "sort": {
      "empty": true,
      "unsorted": true,
      "sorted": false
    },
    "offset": 0,
    "unpaged": false,
    "paged": true
  },
  "last": true,
  "totalPages": 1,
  "totalElements": 1,
  "size": 7,
  "number": 0,
  "sort": {
    "empty": true,
    "unsorted": true,
    "sorted": false
  },
  "first": true,
  "numberOfElements": 1,
  "empty": false
}
        """)
                          )
                  )
          }
  )
  public Page<RecordListResponse> getRecordList(
          @RequestParam(required = false) @Parameter(description = "차량 번호", example = "12가3456") String vehicleNumber,
          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @Parameter(description = "시작 날짜", example = "2025-07-26T00:00:00") LocalDateTime startTime,
          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @Parameter(description = "종료 날짜", example = "2025-07-27T00:00:00") LocalDateTime endTime,
          @ParameterObject @PageableDefault(size = 7) Pageable pageable
  ) {
    return recordService.getRecordList(vehicleNumber, startTime, endTime, pageable);
  }

  /** 운행일지 상세 조회 */
  @GetMapping("/{id}")
  @Operation(summary = "운행일지 상세 조회", description = "운행일지의 상세 정보를 조회합니다.")
  public RecordDetailResponse getRecordDetail(@PathVariable @Parameter(description = "운행일지 ID", example = "2") Long id) {
    return recordService.getRecordDetail(id);
  }
}
