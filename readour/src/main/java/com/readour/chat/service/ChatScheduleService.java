package com.readour.chat.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.readour.chat.dto.common.MessageDto;
import com.readour.chat.dto.request.ChatScheduleCreateRequest;
import com.readour.chat.dto.request.ChatScheduleUpdateRequest;
import com.readour.chat.dto.response.ChatScheduleCreatorResponse;
import com.readour.chat.dto.response.ChatScheduleResponse;
import com.readour.chat.entity.ChatRoom;
import com.readour.chat.entity.ChatRoomMember;
import com.readour.chat.enums.ChatRoomScope;
import com.readour.chat.repository.ChatRoomMemberRepository;
import com.readour.chat.repository.ChatRoomRepository;
import com.readour.common.entity.Calendar;
import com.readour.common.entity.CalendarEvent;
import com.readour.common.entity.User;
import com.readour.common.enums.CalendarScope;
import com.readour.common.enums.ErrorCode;
import com.readour.common.enums.Role;
import com.readour.common.exception.CustomException;
import com.readour.common.repository.CalendarEventRepository;
import com.readour.common.repository.CalendarRepository;
import com.readour.community.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ChatScheduleService {

    private static final String MESSAGE_TYPE_TEXT = "TEXT";

    private final CalendarRepository calendarRepository;
    private final CalendarEventRepository calendarEventRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final ChatMessageService chatMessageService;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public ChatScheduleResponse createSchedule(Long roomId,
                                               Long actorId,
                                               ChatScheduleCreateRequest request) {
        ChatRoom room = findRoom(roomId);
        ChatRoomScope scope = ChatRoomScope.from(room.getScope());
        ChatRoomMember member = ensureActiveMember(roomId, actorId);
        ensureModificationPermission(scope, member);

        LocalDateTime now = LocalDateTime.now();
        Calendar calendar = resolveOrCreateRoomCalendar(room, now);

        CalendarEvent event = CalendarEvent.builder()
                .calendarId(calendar.getCalendarId())
                .title(request.getTitle())
                .description(request.getDescription())
                .location(null)
                .startsAt(request.getStartAt())
                .endsAt(request.getEndAt())
                .allDay(Boolean.FALSE)
                .createdBy(actorId)
                .isDeleted(Boolean.FALSE)
                .createdAt(now)
                .updatedAt(now)
                .build();

        CalendarEvent saved = calendarEventRepository.save(event);

        ChatScheduleResponse response = buildScheduleResponse(saved, roomId);
        publishScheduleMessage(roomId, actorId, "CREATED", response, "일정이 생성되었습니다.");

        return response;
    }

    @Transactional
    public ChatScheduleResponse addParticipant(Long roomId,
                                               Long scheduleId,
                                               Long userId) {
        ensureActiveMember(roomId, userId);

        Calendar calendar = getRoomCalendarOrThrow(roomId);
        CalendarEvent event = calendarEventRepository.findByEventIdAndCalendarIdAndIsDeletedFalse(scheduleId, calendar.getCalendarId())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "일정을 찾을 수 없습니다."));

        LocalDateTime now = LocalDateTime.now();
        Calendar targetCalendar = resolveOrCreatePersonalCalendar(userId, now);

        boolean alreadyRegistered = calendarEventRepository.existsByCalendarIdAndTitleAndStartsAtAndEndsAtAndIsDeletedFalse(
                targetCalendar.getCalendarId(),
                event.getTitle(),
                event.getStartsAt(),
                event.getEndsAt());

        if (!alreadyRegistered) {
            CalendarEvent personalEvent = CalendarEvent.builder()
                    .calendarId(targetCalendar.getCalendarId())
                    .title(event.getTitle())
                    .description(event.getDescription())
                    .location(event.getLocation())
                    .startsAt(event.getStartsAt())
                    .endsAt(event.getEndsAt())
                    .allDay(event.getAllDay())
                    .createdBy(userId)
                    .isDeleted(Boolean.FALSE)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();
            calendarEventRepository.save(personalEvent);
        }

        return buildScheduleResponse(event, roomId);
    }

    @Transactional
    public ChatScheduleResponse updateSchedule(Long roomId,
                                               Long actorId,
                                               Long scheduleId,
                                               ChatScheduleUpdateRequest request) {
        ChatRoom room = findRoom(roomId);
        ChatRoomScope scope = ChatRoomScope.from(room.getScope());
        ChatRoomMember member = ensureActiveMember(roomId, actorId);
        ensureModificationPermission(scope, member);

        Calendar calendar = getRoomCalendarOrThrow(roomId);
        CalendarEvent event = calendarEventRepository.findByEventIdAndCalendarIdAndIsDeletedFalse(scheduleId, calendar.getCalendarId())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "일정을 찾을 수 없습니다."));

        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setStartsAt(request.getStartAt());
        event.setEndsAt(request.getEndAt());
        event.setUpdatedAt(LocalDateTime.now());

        CalendarEvent saved = calendarEventRepository.save(event);

        ChatScheduleResponse response = buildScheduleResponse(saved, roomId);
        publishScheduleMessage(roomId, actorId, "UPDATED", response, "일정이 수정되었습니다.");

        return response;
    }

    @Transactional
    public void deleteSchedule(Long roomId,
                               Long actorId,
                               Long scheduleId) {
        ChatRoom room = findRoom(roomId);
        ChatRoomScope scope = ChatRoomScope.from(room.getScope());
        ChatRoomMember member = ensureActiveMember(roomId, actorId);
        ensureModificationPermission(scope, member);

        Calendar calendar = getRoomCalendarOrThrow(roomId);
        CalendarEvent event = calendarEventRepository.findByEventIdAndCalendarIdAndIsDeletedFalse(scheduleId, calendar.getCalendarId())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "일정을 찾을 수 없습니다."));

        ChatScheduleResponse response = buildScheduleResponse(event, roomId);

        event.setIsDeleted(Boolean.TRUE);
        event.setUpdatedAt(LocalDateTime.now());
        calendarEventRepository.save(event);

        publishScheduleMessage(roomId, actorId, "DELETED", response, "일정이 삭제되었습니다.");
    }

    private ChatScheduleResponse buildScheduleResponse(CalendarEvent event, Long roomId) {
        ChatScheduleCreatorResponse creator = buildCreator(roomId, event.getCreatedBy());
        return ChatScheduleResponse.from(event, roomId, creator);
    }

    private ChatScheduleCreatorResponse buildCreator(Long roomId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "작성자를 찾을 수 없습니다."));

        Role role = chatRoomMemberRepository.findByRoomIdAndUserId(roomId, userId)
                .map(ChatRoomMember::getRole)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "작성자의 채팅방 역할을 찾을 수 없습니다."));

        return ChatScheduleCreatorResponse.builder()
                .id(userId)
                .username(user.getNickname())
                .role(role)
                .build();
    }

    private ChatRoom findRoom(Long roomId) {
        return chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "채팅방을 찾을 수 없습니다."));
    }

    private Calendar resolveOrCreateRoomCalendar(ChatRoom room, LocalDateTime now) {
        return calendarRepository.findByScopeAndRelatedRoomId(CalendarScope.ROOM, room.getId())
                .map(existing -> {
                    existing.setUpdatedAt(now);
                    return calendarRepository.save(existing);
                })
                .orElseGet(() -> calendarRepository.save(Calendar.builder()
                        .ownerUserId(room.getCreatedBy())
                        .scope(CalendarScope.ROOM)
                        .relatedRoomId(room.getId())
                        .name(room.getName())
                        .createdAt(now)
                        .updatedAt(now)
                        .build()));
    }

    private Calendar getRoomCalendarOrThrow(Long roomId) {
        return calendarRepository.findByScopeAndRelatedRoomId(CalendarScope.ROOM, roomId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "채팅방 일정을 찾을 수 없습니다."));
    }

    private Calendar resolveOrCreatePersonalCalendar(Long ownerId, LocalDateTime now) {
        return calendarRepository.findFirstByOwnerUserIdAndScope(ownerId, CalendarScope.USER)
                .map(existing -> {
                    existing.setUpdatedAt(now);
                    return calendarRepository.save(existing);
                })
                .orElseGet(() -> calendarRepository.save(Calendar.builder()
                        .ownerUserId(ownerId)
                        .scope(CalendarScope.USER)
                        .relatedRoomId(null)
                        .name("개인 캘린더")
                        .createdAt(now)
                        .updatedAt(now)
                        .build()));
    }

    private ChatRoomMember ensureActiveMember(Long roomId, Long userId) {
        return chatRoomMemberRepository.findByRoomIdAndUserIdAndIsActiveTrue(roomId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.FORBIDDEN, "채팅방에 참여 중이 아닙니다."));
    }

    private void ensureModificationPermission(ChatRoomScope scope, ChatRoomMember member) {
        if (scope == ChatRoomScope.PRIVATE) {
            return;
        }

        if (!isManagerOrOwner(member)) {
            throw new CustomException(ErrorCode.FORBIDDEN, "일정 기능은 방장 또는 매니저만 사용할 수 있습니다.");
        }
    }

    private boolean isManagerOrOwner(ChatRoomMember member) {
        Role role = member.getRole();
        return role == Role.MANAGER || role == Role.OWNER;
    }

    private void publishScheduleMessage(Long roomId,
                                        Long actorId,
                                        String action,
                                        ChatScheduleResponse schedule,
                                        String text) {
        ObjectNode body = objectMapper.createObjectNode();
        body.put("text", text);
        body.put("action", action);
        body.put("calendarId", schedule.getCalendarId());
        body.put("eventId", schedule.getEventId());
        body.set("schedule", objectMapper.valueToTree(schedule));

        MessageDto message = MessageDto.builder()
                .roomId(roomId)
                .senderId(actorId)
                .type(MESSAGE_TYPE_TEXT)
                .body(body)
                .build();

        chatMessageService.send(message);
    }
}
