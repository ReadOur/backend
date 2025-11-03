package com.readour.chat.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.readour.chat.dto.request.RoomCreateRequest;
import com.readour.chat.dto.response.ChatPageOverviewResponse;
import com.readour.chat.dto.response.PublicRoomListItemResponse;
import com.readour.chat.dto.response.PublicRoomListResponse;
import com.readour.chat.dto.response.RoomCreateResponse;
import com.readour.chat.dto.response.RoomListItemResponse;
import com.readour.chat.dto.response.RoomListPageResponse;
import com.readour.chat.entity.ChatMessage;
import com.readour.chat.entity.ChatRoom;
import com.readour.chat.entity.ChatRoomMember;
import com.readour.chat.enums.ChatRoomScope;
import com.readour.chat.repository.ChatMessageRepository;
import com.readour.chat.repository.ChatRoomMemberRepository;
import com.readour.chat.repository.ChatRoomRepository;
import com.readour.chat.repository.projection.RoomMemberCountProjection;
import com.readour.common.enums.ErrorCode;
import com.readour.common.enums.Role;
import com.readour.common.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 50;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final ChatMessageRepository chatMessageRepository;

    @Transactional(readOnly = true)
    public RoomListPageResponse getMyRooms(Long userId, String query, int page, int size) {
        if (userId == null) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "userId는 필수입니다.");
        }
        if (page < 0) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "page는 0 이상이어야 합니다.");
        }
        if (size <= 0 || size > MAX_PAGE_SIZE) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "size는 1 이상 50 이하이어야 합니다.");
        }

        List<ChatRoomMember> memberships = chatRoomMemberRepository.findAllByUserIdAndIsActiveTrue(userId);
        if (memberships.isEmpty()) {
            return RoomListPageResponse.builder()
                    .items(List.of())
                    .page(RoomListPageResponse.PageInfo.builder()
                            .number(page)
                            .size(size)
                            .hasNext(false)
                            .build())
                    .build();
        }

        Set<Long> roomIds = memberships.stream()
                .map(ChatRoomMember::getRoomId)
                .collect(Collectors.toSet());

        Map<Long, ChatRoom> roomMap = chatRoomRepository.findAllById(roomIds).stream()
                .filter(room -> Boolean.TRUE.equals(room.getIsActive()))
                .collect(Collectors.toMap(ChatRoom::getId, room -> room));

        Comparator<ChatRoomMember> comparator = Comparator
                .comparing((ChatRoomMember m) -> m.getPinnedAt() == null ? 1 : 0)
                .thenComparing(m -> Objects.requireNonNullElse(m.getPinOrder(), Integer.MAX_VALUE))
                .thenComparing(ChatRoomMember::getPinnedAt, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing((ChatRoomMember m) -> getRoomUpdatedAt(roomMap.get(m.getRoomId())),
                        Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(ChatRoomMember::getRoomId);

        List<ChatRoomMember> filtered = memberships.stream()
                .filter(m -> roomMap.containsKey(m.getRoomId()))
                .filter(m -> isRoomMatched(roomMap.get(m.getRoomId()), query))
                .sorted(comparator)
                .toList();

        int total = filtered.size();
        int fromIndex = Math.min(page * size, total);
        int toIndex = Math.min(fromIndex + size, total);

        List<RoomListItemResponse> items = filtered.subList(fromIndex, toIndex).stream()
                .map(member -> buildRoomListItem(member, roomMap.get(member.getRoomId())))
                .collect(Collectors.toCollection(ArrayList::new));

        boolean hasNext = toIndex < total;

        return RoomListPageResponse.builder()
                .items(items)
                .page(RoomListPageResponse.PageInfo.builder()
                        .number(page)
                        .size(size)
                        .hasNext(hasNext)
                        .build())
                .build();
    }

    @Transactional(readOnly = true)
    public PublicRoomListResponse getPublicRooms(Long userId, String query, int page, int size) {
        if (userId == null) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "userId는 필수입니다.");
        }
        if (page < 0) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "page는 0 이상이어야 합니다.");
        }
        if (size <= 0 || size > MAX_PAGE_SIZE) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "size는 1 이상 50 이하이어야 합니다.");
        }

        String normalizedQuery = StringUtils.isBlank(query) ? null : query.trim();
        Sort sort = Sort.by(Sort.Order.asc("name"), Sort.Order.asc("id"));
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<ChatRoom> roomPage = chatRoomRepository.findActivePublicRooms(normalizedQuery, pageable);
        List<ChatRoom> rooms = roomPage.getContent();
        List<Long> roomIds = rooms.stream()
                .map(ChatRoom::getId)
                .toList();

        Map<Long, Long> memberCountMap = new HashMap<>();
        if (!roomIds.isEmpty()) {
            List<RoomMemberCountProjection> counts = chatRoomMemberRepository.countActiveMembersByRoomIds(roomIds);
            counts.forEach(count -> memberCountMap.put(count.getRoomId(), count.getMemberCount()));
        }

        Set<Long> joinedRoomIds = chatRoomMemberRepository.findAllByUserIdAndIsActiveTrue(userId).stream()
                .map(ChatRoomMember::getRoomId)
                .collect(Collectors.toSet());

        List<PublicRoomListItemResponse> items = rooms.stream()
                .map(room -> PublicRoomListItemResponse.builder()
                        .roomId(room.getId())
                        .name(room.getName())
                        .description(room.getDescription())
                        .memberCount(memberCountMap.getOrDefault(room.getId(), 0L))
                        .joined(joinedRoomIds.contains(room.getId()))
                        .updatedAt(room.getUpdatedAt())
                        .build())
                .toList();

        return PublicRoomListResponse.builder()
                .items(items)
                .page(RoomListPageResponse.PageInfo.builder()
                        .number(roomPage.getNumber())
                        .size(roomPage.getSize())
                        .hasNext(roomPage.hasNext())
                        .build())
                .build();
    }

    @Transactional(readOnly = true)
    public ChatPageOverviewResponse getChatPageOverview(Long userId,
                                                        String myQuery,
                                                        Integer myPage,
                                                        Integer mySize,
                                                        String publicQuery,
                                                        Integer publicPage,
                                                        Integer publicSize) {
        if (userId == null) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "userId는 필수입니다.");
        }

        int resolvedMyPage = myPage == null ? DEFAULT_PAGE : myPage;
        int resolvedMySize = mySize == null ? DEFAULT_SIZE : mySize;
        int resolvedPublicPage = publicPage == null ? DEFAULT_PAGE : publicPage;
        int resolvedPublicSize = publicSize == null ? DEFAULT_SIZE : publicSize;

        RoomListPageResponse myRooms = getMyRooms(userId, myQuery, resolvedMyPage, resolvedMySize);
        PublicRoomListResponse publicRooms = getPublicRooms(userId, publicQuery, resolvedPublicPage, resolvedPublicSize);

        return ChatPageOverviewResponse.builder()
                .myRooms(myRooms)
                .publicRooms(publicRooms)
                .build();
    }

    @Transactional
    public RoomCreateResponse createRoom(Long creatorId, RoomCreateRequest request) {
        if (creatorId == null) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "creatorId는 필수입니다.");
        }
        ChatRoomScope scope = request.getScope();
        if (scope == null) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "scope는 필수입니다.");
        }

        LocalDateTime now = LocalDateTime.now();

        ChatRoom room = ChatRoom.builder()
                .scope(scope.name())
                .name(request.getName())
                .description(request.getDescription())
                .createdBy(creatorId)
                .createdAt(now)
                .updatedAt(now)
                .isActive(true)
                .build();

        ChatRoom savedRoom = chatRoomRepository.save(room);

        List<Long> memberIds = request.getMemberIds();
        if (memberIds == null) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "memberIds는 필수입니다.");
        }
        if (memberIds.stream().anyMatch(Objects::isNull)) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "memberIds에는 null을 포함할 수 없습니다.");
        }

        Set<Long> participantIds = new LinkedHashSet<>(memberIds);
        participantIds.add(creatorId);

        if (scope == ChatRoomScope.PRIVATE && participantIds.size() != 2) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "1:1 채팅방은 참여자가 2명이어야 합니다.");
        }

        List<ChatRoomMember> members = participantIds.stream()
                .map(userId -> buildMemberForNewRoom(savedRoom.getId(), userId, creatorId, now))
                .toList();

        chatRoomMemberRepository.saveAll(members);

        return RoomCreateResponse.fromEntity(savedRoom);
    }

    private boolean isRoomMatched(ChatRoom room, String query) {
        if (room == null || StringUtils.isBlank(query)) {
            return room != null;
        }
        return StringUtils.containsIgnoreCase(room.getName(), query)
                || StringUtils.containsIgnoreCase(StringUtils.defaultString(room.getDescription()), query);
    }

    private RoomListItemResponse buildRoomListItem(ChatRoomMember member, ChatRoom room) {
        if (room == null) {
            throw new CustomException(ErrorCode.NOT_FOUND, "존재하지 않는 채팅방입니다.");
        }

        Optional<ChatMessage> lastMessageOpt = chatMessageRepository.findFirstByRoomIdAndDeletedAtIsNullOrderByCreatedAtDesc(room.getId());
        RoomListItemResponse.LastMessage lastMessage = lastMessageOpt
                .map(this::toLastMessageDto)
                .orElse(null);

        long unreadCount = calculateUnreadCount(member);

        return RoomListItemResponse.builder()
                .roomId(room.getId())
                .name(room.getName())
                .lastMsg(lastMessage)
                .unreadCount(unreadCount)
                .updatedAt(room.getUpdatedAt())
                .pinned(member.getPinnedAt() != null)
                .pinOrder(member.getPinOrder())
                .build();
    }

    private RoomListItemResponse.LastMessage toLastMessageDto(ChatMessage message) {
        return RoomListItemResponse.LastMessage.builder()
                .id(message.getId())
                .preview(extractPreview(message))
                .createdAt(message.getCreatedAt())
                .build();
    }

    private long calculateUnreadCount(ChatRoomMember member) {
        Long roomId = member.getRoomId();
        Long lastReadMsgId = member.getLastReadMsgId();

        if (lastReadMsgId == null) {
            return chatMessageRepository.countByRoomIdAndDeletedAtIsNull(roomId);
        }

        return chatMessageRepository.countByRoomIdAndDeletedAtIsNullAndIdGreaterThan(roomId, lastReadMsgId);
    }

    private String extractPreview(ChatMessage message) {
        String body = message.getBody();
        if (StringUtils.isBlank(body)) {
            return message.getType();
        }
        try {
            JsonNode node = OBJECT_MAPPER.readTree(body);
            if (node.hasNonNull("text")) {
                return node.get("text").asText();
            }
        } catch (JsonProcessingException ignored) {
            // body가 JSON 포맷이 아니더라도 미리보기 자체는 제공한다.
        }
        return message.getType();
    }

    private LocalDateTime getRoomUpdatedAt(ChatRoom room) {
        return room == null ? null : room.getUpdatedAt();
    }

    private ChatRoomMember buildMemberForNewRoom(Long roomId, Long userId, Long creatorId, LocalDateTime now) {
        Role role = Objects.equals(userId, creatorId) ? Role.OWNER : Role.MEMBER;
        return ChatRoomMember.builder()
                .roomId(roomId)
                .userId(userId)
                .role(role)
                .joinedAt(now)
                .mutedUntil(null)
                .lastReadMsgId(null)
                .isActive(true)
                .pinnedAt(null)
                .pinOrder(null)
                .kickedAt(null)
                .kickedBy(null)
                .kickReason(null)
                .build();
    }
}
