package com.readour.common.controller;

import com.readour.common.dto.ApiResponseDto;
import com.readour.common.dto.CalendarEventCreateRequestDto;
import com.readour.common.dto.CalendarEventResponseDto;
import com.readour.common.dto.CalendarEventUpdateRequestDto;
import com.readour.common.dto.ErrorResponseDto;
import com.readour.common.enums.CalendarScope;
import com.readour.common.service.CalendarService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Personal Calendar", description = "개인 캘린더(일정) 관리 API")
@RestController
@RequestMapping("/api/calendar")
@RequiredArgsConstructor
@Validated
public class CalendarController {

    private final CalendarService calendarService;

    @Operation(summary = "캘린더 조회 (월별/주별)",
            description = "사용자의 캘린더 일정을 조회합니다. (viewType=WEEK 또는 MONTH, scope 파라미터 생략 시 USER+ROOM 모두 조회)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping("/events")
    public ResponseEntity<ApiResponseDto<List<CalendarEventResponseDto>>> getEvents(
            @RequestHeader("X-User-Id") Long userId, // TODO: 인증 기능으로 교체
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @Schema(description = "조회 기준 날짜", example = "2025-11-10")
            LocalDate viewDate,
            @RequestParam(defaultValue = "MONTH")
            @Schema(description = "조회 타입", example = "WEEK", allowableValues = {"MONTH", "WEEK"})
            String viewType,
            @RequestParam(required = false)
            @Schema(description = "조회 범위 (USER 또는 ROOM). 생략 시 모두 조회", example = "USER")
            CalendarScope scope
    ) {
        List<CalendarEventResponseDto> events = calendarService.getEvents(userId, viewDate, viewType, scope);

        return ResponseEntity.ok(ApiResponseDto.<List<CalendarEventResponseDto>>builder()
                .status(HttpStatus.OK.value())
                .body(events)
                .message("캘린더 일정 목록 조회 성공")
                .build());
    }

    @Operation(summary = "상세 일정 조회", description = "개인 일정 1개를 상세 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "일정을 찾을 수 없거나 조회 권한이 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @GetMapping("/events/{eventId}")
    public ResponseEntity<ApiResponseDto<CalendarEventResponseDto>> getEventDetail(
            @PathVariable Long eventId,
            @RequestHeader("X-User-Id") Long userId // TODO: 인증 기능으로 교체
    ) {
        CalendarEventResponseDto event = calendarService.getEventDetail(eventId, userId);
        return ResponseEntity.ok(ApiResponseDto.<CalendarEventResponseDto>builder()
                .status(HttpStatus.OK.value())
                .body(event)
                .message("개인 일정 상세 조회 성공")
                .build());
    }

    @Operation(summary = "개인 일정 추가", description = "개인 캘린더에 새 일정을 추가합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "일정 추가 성공"),
            @ApiResponse(responseCode = "400", description = "요청 값 오류 (날짜 오류 등)",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @PostMapping("/events")
    public ResponseEntity<ApiResponseDto<CalendarEventResponseDto>> createEvent(
            @RequestHeader("X-User-Id") Long userId, // TODO: 인증 기능으로 교체
            @Valid @RequestBody CalendarEventCreateRequestDto requestDto
    ) {
        CalendarEventResponseDto event = calendarService.createEvent(userId, requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponseDto.<CalendarEventResponseDto>builder()
                .status(HttpStatus.CREATED.value())
                .body(event)
                .message("개인 일정이 추가되었습니다.")
                .build());
    }

    @Operation(summary = "일정 수정", description = "자신이 생성한 개인 일정이나 OWNER나 MANAGER인 팀의 일정을 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "일정 수정 성공"),
            @ApiResponse(responseCode = "400", description = "요청 값 오류 (날짜 오류 등)"),
            @ApiResponse(responseCode = "404", description = "일정을 찾을 수 없거나 수정 권한이 없음")
    })
    @PutMapping("/events/{eventId}")
    public ResponseEntity<ApiResponseDto<CalendarEventResponseDto>> updateEvent(
            @PathVariable Long eventId,
            @RequestHeader("X-User-Id") Long userId, // TODO: 인증 기능으로 교체
            @Valid @RequestBody CalendarEventUpdateRequestDto requestDto
    ) {
        CalendarEventResponseDto event = calendarService.updateEvent(eventId, userId, requestDto);
        return ResponseEntity.ok(ApiResponseDto.<CalendarEventResponseDto>builder()
                .status(HttpStatus.OK.value())
                .body(event)
                .message("일정이 수정되었습니다.")
                .build());
    }

    @Operation(summary = "일정 삭제", description = "자신이 생성한 개인 일정이나 OWNER나 MANAGER인 팀의 일정을 삭제합니다. (Soft Delete)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "일정 삭제 성공"),
            @ApiResponse(responseCode = "404", description = "일정을 찾을 수 없거나 삭제 권한이 없음")
    })
    @DeleteMapping("/events/{eventId}")
    public ResponseEntity<ApiResponseDto<Void>> deleteEvent(
            @PathVariable Long eventId,
            @RequestHeader("X-User-Id") Long userId // TODO: 인증 기능으로 교체
    ) {
        calendarService.deleteEvent(eventId, userId);
        return ResponseEntity.ok(ApiResponseDto.<Void>builder()
                .status(HttpStatus.OK.value())
                .message("일정이 삭제되었습니다.")
                .build());
    }
}