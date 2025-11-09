package com.readour.common.service;

import com.readour.chat.entity.ChatRoomMember;
import com.readour.chat.repository.ChatRoomMemberRepository;
import com.readour.common.dto.CalendarEventCreateRequestDto;
import com.readour.common.dto.CalendarEventResponseDto;
import com.readour.common.dto.CalendarEventUpdateRequestDto;
import com.readour.common.entity.Calendar;
import com.readour.common.entity.CalendarEvent;
import com.readour.common.enums.CalendarScope;
import com.readour.common.enums.ErrorCode;
import com.readour.common.enums.Role;
import com.readour.common.exception.CustomException;
import com.readour.common.repository.CalendarEventRepository;
import com.readour.common.repository.CalendarRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CalendarService {

    private final CalendarRepository calendarRepository;
    private final CalendarEventRepository calendarEventRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;

    /**
     * (SD-22) 캘린더 조회 (월별/주별)
     */
    @Transactional(readOnly = true)
    public List<CalendarEventResponseDto> getEvents(Long userId, LocalDate viewDate, String viewType, CalendarScope scope) {
        // 1. 조회 범위 계산 (기존과 동일)
        LocalDateTime startDate;
        LocalDateTime endDate;
        if ("WEEK".equalsIgnoreCase(viewType)) {
            LocalDate weekStart = viewDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
            LocalDate weekEnd = weekStart.plusDays(6);
            startDate = weekStart.atStartOfDay();
            endDate = weekEnd.atTime(LocalTime.MAX);
        } else { // "MONTH" (기본값)
            LocalDate monthStart = viewDate.withDayOfMonth(1);
            LocalDate monthEnd = viewDate.with(TemporalAdjusters.lastDayOfMonth());
            startDate = monthStart.atStartOfDay();
            endDate = monthEnd.atTime(LocalTime.MAX);
        }

        log.debug("GetEvents userId: {}, Scope: {}, Range: {} ~ {}", userId, scope, startDate, endDate);

        // 2. [신규] 조회할 캘린더 ID 목록 집계
        List<Long> targetCalendarIds = new ArrayList<>();

        // 2-1. USER 스코프 추가 (기존 로직)
        if (scope == null || scope == CalendarScope.USER) {
            findUserCalendar(userId).ifPresent(cal -> targetCalendarIds.add(cal.getCalendarId()));
        }

        // 2-2. ROOM 스코프 추가
        if (scope == null || scope == CalendarScope.ROOM) {
            // 사용자가 속한 모든 활성 채팅방 ID 조회
            List<Long> roomIds = chatRoomMemberRepository.findAllByUserIdAndIsActiveTrue(userId).stream()

                    .map(ChatRoomMember::getRoomId)
                    .toList();

            if (!roomIds.isEmpty()) {
                // 채팅방 ID에 연결된 ROOM 캘린더 ID들 조회
                List<Long> roomCalendarIds = calendarRepository.findByScopeAndRelatedRoomIdIn(CalendarScope.ROOM, roomIds).stream()
                        .map(Calendar::getCalendarId)
                        .toList();
                targetCalendarIds.addAll(roomCalendarIds);
            }
        }

        // 3. 캘린더 ID가 없으면 빈 리스트 반환
        if (targetCalendarIds.isEmpty()) {
            return Collections.emptyList();
        }

        // 4. 통합된 캘린더 ID 목록으로 기간 내 이벤트 조회
        Sort sort = Sort.by(Sort.Direction.ASC, "startsAt");
        List<CalendarEvent> events = calendarEventRepository
                .findAllByCalendarIdInAndIsDeletedFalseAndStartsAtLessThanEqualAndEndsAtGreaterThanEqual(
                        targetCalendarIds, endDate, startDate, sort);

        return events.stream()
                .map(CalendarEventResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 상세 일정 조회
     */
    @Transactional(readOnly = true)
    public CalendarEventResponseDto getEventDetail(Long eventId, Long userId) {
        // 1. 이벤트 ID로 이벤트 조회
        CalendarEvent event = calendarEventRepository.findById(eventId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "일정을 찾을 수 없습니다."));

        // 2. 이벤트의 캘린더(소유 정보) 조회
        Calendar calendar = calendarRepository.findById(event.getCalendarId())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "일정의 캘린더(소유자) 정보를 찾을 수 없습니다."));

        // 3. 통합 권한 검증
        validateEventPermission(calendar, userId);

        // 4. DTO 반환
        return CalendarEventResponseDto.fromEntity(event);
    }

    /**
     * (SD-23) 일정 추가
     */
    @Transactional
    public CalendarEventResponseDto createEvent(Long userId, CalendarEventCreateRequestDto dto) {
        LocalDateTime now = LocalDateTime.now();
        // 1. 개인 캘린더 조회 (없으면 생성)
        Calendar userCalendar = getOrCreateUserCalendar(userId, now);

        if (dto.getStartsAt().isAfter(dto.getEndsAt())) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "종료 시각은 시작 시각보다 빠를 수 없습니다.");
        }

        // 2. 이벤트 엔티티 생성
        CalendarEvent event = CalendarEvent.builder()
                .calendarId(userCalendar.getCalendarId())
                .title(dto.getTitle())
                .description(dto.getDescription())
                .location(dto.getLocation())
                .startsAt(dto.getStartsAt())
                .endsAt(dto.getEndsAt())
                .allDay(dto.getAllDay() != null ? dto.getAllDay() : false)
                .createdBy(userId)
                .isDeleted(false)
                .createdAt(now)
                .updatedAt(now)
                .build();

        CalendarEvent savedEvent = calendarEventRepository.save(event);
        log.info("Personal CalendarEvent created. eventId: {}, userId: {}", savedEvent.getEventId(), userId);

        return CalendarEventResponseDto.fromEntity(savedEvent);
    }

    /**
     * (SD-24) 일정 수정
     */
    @Transactional
    public CalendarEventResponseDto updateEvent(Long eventId, Long userId, CalendarEventUpdateRequestDto dto) {
        // 1. 이벤트 및 캘린더 조회, 통합 권한 검증
        CalendarEvent event = validateEventModificationPermission(eventId, userId);

        // 2. 값 변경 (Dirty checking)
        if (dto.getTitle() != null) event.setTitle(dto.getTitle());
        if (dto.getDescription() != null) event.setDescription(dto.getDescription());
        if (dto.getLocation() != null) event.setLocation(dto.getLocation());
        if (dto.getStartsAt() != null) event.setStartsAt(dto.getStartsAt());
        if (dto.getEndsAt() != null) event.setEndsAt(dto.getEndsAt());
        if (dto.getAllDay() != null) event.setAllDay(dto.getAllDay());

        event.setUpdatedAt(LocalDateTime.now());

        if (event.getStartsAt().isAfter(event.getEndsAt())) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "종료 시각은 시작 시각보다 빠를 수 없습니다.");
        }

        return CalendarEventResponseDto.fromEntity(event);
    }

    /**
     * (SD-25) 일정 삭제
     */
    @Transactional
    public void deleteEvent(Long eventId, Long userId) {
        // 1. 이벤트 및 캘린더 조회, 통합 권한 검증
        CalendarEvent event = validateEventModificationPermission(eventId, userId);

        // 2. Soft Delete
        event.setIsDeleted(true);
        event.setUpdatedAt(LocalDateTime.now());

        log.info("CalendarEvent soft-deleted. eventId: {}, userId: {}", event.getEventId(), userId);
    }


    /**
     * [Helper] 사용자의 개인 캘린더(Scope.USER)를 찾거나, 없으면 생성합니다.
     */
    private Calendar getOrCreateUserCalendar(Long ownerId, LocalDateTime now) {
        return calendarRepository.findFirstByOwnerUserIdAndScope(ownerId, CalendarScope.USER)
                .orElseGet(() -> {
                    log.info("Creating new personal calendar for userId: {}", ownerId);
                    Calendar newCalendar = Calendar.builder()
                            .ownerUserId(ownerId)
                            .scope(CalendarScope.USER)
                            .name("개인 캘린더") // (DC-23)
                            .createdAt(now)
                            .updatedAt(now)
                            .build();
                    return calendarRepository.save(newCalendar);
                });
    }

    /**
     * [HELPER] 사용자의 개인 캘린더(Scope.USER)를 조회합니다. (Read-Only)
     */
    private Optional<Calendar> findUserCalendar(Long ownerId) {
        return calendarRepository.findFirstByOwnerUserIdAndScope(ownerId, CalendarScope.USER);
    }

    /**
     * 통합 권한 검증 로직
     */
    private void validateEventPermission(Calendar calendar, Long userId) {
        boolean hasPermission = false;

        if (calendar.getScope() == CalendarScope.USER) {
            // USER 스코프: 캘린더 소유자(ownerUserId)가 본인(userId)인지 확인
            hasPermission = calendar.getOwnerUserId().equals(userId);

        } else if (calendar.getScope() == CalendarScope.ROOM) {
            // ROOM 스코프: 해당 채팅방(relatedRoomId)의 '활성 멤버'인지 확인
            hasPermission = chatRoomMemberRepository
                    .findByRoomIdAndUserIdAndIsActiveTrue(calendar.getRelatedRoomId(), userId)
                    .isPresent();

        } else if (calendar.getScope() == CalendarScope.GLOBAL) {
            hasPermission = true; // GLOBAL은 모두에게 허용 (정책)
        }

        if (!hasPermission) {
            throw new CustomException(ErrorCode.FORBIDDEN, "해당 일정에 접근할 권한이 없습니다.");
        }
    }

    /**
     * [Helper] (수정/삭제 권한) 캘린더 일정 '수정/삭제' 권한 검증
     * USER: 일정 생성자
     * ROOM: 채팅방 Owner or Manager
     */
    private CalendarEvent validateEventModificationPermission(Long eventId, Long userId) {
        // 1. 이벤트 조회
        CalendarEvent event = calendarEventRepository.findById(eventId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "일정을 찾을 수 없습니다."));

        // 2. 캘린더 조회
        Calendar calendar = calendarRepository.findById(event.getCalendarId())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "일정의 캘린더(소유자) 정보를 찾을 수 없습니다."));

        // 3. 권한 검증
        if (calendar.getScope() == CalendarScope.USER) {
            // USER 스코프: 생성자(createdBy)와 요청자(userId)가 일치해야 함
            if (!event.getCreatedBy().equals(userId)) {
                throw new CustomException(ErrorCode.FORBIDDEN, "개인 일정은 생성자만 수정/삭제할 수 있습니다.");
            }
        } else if (calendar.getScope() == CalendarScope.ROOM) {
            // ROOM 스코프: 채팅방의 'Owner' 또는 'Manager'여야 함
            ChatRoomMember member = chatRoomMemberRepository
                    .findByRoomIdAndUserIdAndIsActiveTrue(calendar.getRelatedRoomId(), userId)
                    .orElseThrow(() -> new CustomException(ErrorCode.FORBIDDEN, "채팅방 멤버가 아니므로 수정/삭제 권한이 없습니다."));

            // [정책 수정] MEMBER는 안되고, OWNER 또는 MANAGER만 가능
            if (member.getRole() != Role.OWNER && member.getRole() != Role.MANAGER) {
                throw new CustomException(ErrorCode.FORBIDDEN, "일정 수정/삭제는 방장 또는 매니저만 가능합니다.");
            }
        } else {
            // GLOBAL 등 기타
            throw new CustomException(ErrorCode.FORBIDDEN, "해당 일정은 수정/삭제할 수 없습니다.");
        }

        // 4. 권한이 있으면 이벤트 반환
        return event;
    }
}