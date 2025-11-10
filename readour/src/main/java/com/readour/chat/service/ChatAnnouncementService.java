package com.readour.chat.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.readour.chat.dto.common.MessageDto;
import com.readour.chat.dto.event.ChatAnnouncementEvent;
import com.readour.chat.dto.request.ChatAnnouncementCreateRequest;
import com.readour.chat.dto.request.ChatAnnouncementUpdateRequest;
import com.readour.chat.dto.response.ChatAnnouncementAuthorResponse;
import com.readour.chat.dto.response.ChatAnnouncementListResponse;
import com.readour.chat.dto.response.ChatAnnouncementResponse;
import com.readour.chat.dto.response.ChatAnnouncementSummaryResponse;
import com.readour.chat.entity.ChatAnnouncement;
import com.readour.chat.entity.ChatRoomMember;
import com.readour.chat.repository.ChatAnnouncementRepository;
import com.readour.chat.repository.ChatRoomMemberRepository;
import com.readour.common.entity.User;
import com.readour.common.enums.ErrorCode;
import com.readour.common.enums.Role;
import com.readour.common.exception.CustomException;
import com.readour.common.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatAnnouncementService {

    private static final String ANNOUNCEMENT_TOPIC = "chat-announcements";
    private static final String MESSAGE_TYPE_NOTICE = "NOTICE";
    private static final int CONTENT_PREVIEW_LIMIT = 120;

    private final ChatAnnouncementRepository chatAnnouncementRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final ChatMessageService chatMessageService;
    private final UserRepository userRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Transactional
    public ChatAnnouncementResponse createAnnouncement(Long roomId,
                                                       Long actorId,
                                                       ChatAnnouncementCreateRequest request) {
        ensureManagerOrOwner(roomId, actorId);

        LocalDateTime now = LocalDateTime.now();
        ChatAnnouncement announcement = ChatAnnouncement.builder()
                .roomId(roomId)
                .authorId(actorId)
                .title(request.getTitle())
                .content(request.getContent())
                .createdAt(now)
                .updatedAt(now)
                .build();

        ChatAnnouncement saved = chatAnnouncementRepository.save(announcement);

        ChatAnnouncementAuthorResponse author = buildAuthorResponse(roomId, actorId);
        ChatAnnouncementResponse response = ChatAnnouncementResponse.from(saved, author);

        publishAnnouncementMessage(roomId, actorId, "CREATED", response, "공지 추가되었습니다.");
        publishAnnouncementEvent("CREATED", response.getRoomId(), response.getId(), response, actorId);

        return response;
    }

    @Transactional(readOnly = true)
    public ChatAnnouncementListResponse getAnnouncements(Long roomId,
                                                         Long requesterId,
                                                         Integer page,
                                                         Integer size) {
        ensureActiveMember(roomId, requesterId);

        int resolvedPage = page == null || page < 0 ? 0 : page;
        int resolvedSize = size == null || size <= 0 ? 10 : size;

        Pageable pageable = PageRequest.of(resolvedPage, resolvedSize, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<ChatAnnouncement> slice = chatAnnouncementRepository.findAllByRoomId(roomId, pageable);
        List<ChatAnnouncement> announcements = slice.getContent();

        Map<Long, ChatAnnouncementAuthorResponse> authorMap = buildAuthorResponseMap(roomId, announcements);

        List<ChatAnnouncementSummaryResponse> items = announcements.stream()
                .map(announcement -> ChatAnnouncementSummaryResponse.from(
                        announcement,
                        authorMap.get(announcement.getAuthorId())))
                .collect(Collectors.toList());

        ChatAnnouncementListResponse.PageInfo pageInfo = ChatAnnouncementListResponse.PageInfo.builder()
                .page(slice.getNumber())
                .size(slice.getSize())
                .totalPages(slice.getTotalPages())
                .totalElements(slice.getTotalElements())
                .hasNext(slice.hasNext())
                .build();

        return ChatAnnouncementListResponse.builder()
                .items(items)
                .page(pageInfo)
                .build();
    }

    @Transactional(readOnly = true)
    public ChatAnnouncementResponse getAnnouncement(Long roomId,
                                                    Long requesterId,
                                                    Long announcementId) {
        ensureActiveMember(roomId, requesterId);

        ChatAnnouncement announcement = chatAnnouncementRepository.findByIdAndRoomId(announcementId, roomId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "공지를 찾을 수 없습니다."));

        ChatAnnouncementAuthorResponse author = buildAuthorResponse(roomId, announcement.getAuthorId());

        return ChatAnnouncementResponse.from(announcement, author);
    }

    @Transactional
    public ChatAnnouncementResponse updateAnnouncement(Long roomId,
                                                       Long actorId,
                                                       Long announcementId,
                                                       ChatAnnouncementUpdateRequest request) {
        ensureManagerOrOwner(roomId, actorId);

        ChatAnnouncement announcement = chatAnnouncementRepository.findByIdAndRoomId(announcementId, roomId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "공지를 찾을 수 없습니다."));

        announcement.setTitle(request.getTitle());
        announcement.setContent(request.getContent());
        announcement.setUpdatedAt(LocalDateTime.now());

        ChatAnnouncement saved = chatAnnouncementRepository.save(announcement);

        ChatAnnouncementAuthorResponse author = buildAuthorResponse(roomId, saved.getAuthorId());
        ChatAnnouncementResponse response = ChatAnnouncementResponse.from(saved, author);

        publishAnnouncementMessage(roomId, actorId, "UPDATED", response, "공지 수정되었습니다.");
        publishAnnouncementEvent("UPDATED", response.getRoomId(), response.getId(), response, actorId);

        return response;
    }

    @Transactional
    public void deleteAnnouncement(Long roomId,
                                   Long actorId,
                                   Long announcementId) {
        ensureManagerOrOwner(roomId, actorId);

        ChatAnnouncement announcement = chatAnnouncementRepository.findByIdAndRoomId(announcementId, roomId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "공지를 찾을 수 없습니다."));

        ChatAnnouncementAuthorResponse author = buildAuthorResponse(roomId, announcement.getAuthorId());
        ChatAnnouncementResponse response = ChatAnnouncementResponse.from(announcement, author);

        chatAnnouncementRepository.delete(announcement);

        publishAnnouncementMessage(roomId, actorId, "DELETED", response, "공지 삭제되었습니다.");
        publishAnnouncementEvent("DELETED", announcement.getRoomId(), announcement.getId(), null, actorId);
    }

    private ChatRoomMember ensureActiveMember(Long roomId, Long userId) {
        return chatRoomMemberRepository.findByRoomIdAndUserIdAndIsActiveTrue(roomId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.FORBIDDEN, "채팅방에 참여 중이 아닙니다."));
    }

    private ChatRoomMember ensureManagerOrOwner(Long roomId, Long userId) {
        ChatRoomMember member = ensureActiveMember(roomId, userId);
        if (!isManagerOrOwner(member)) {
            throw new CustomException(ErrorCode.FORBIDDEN, "공지 기능은 방장 또는 매니저만 사용할 수 있습니다.");
        }
        return member;
    }

    private boolean isManagerOrOwner(ChatRoomMember member) {
        Role role = member.getRole();
        return role == Role.MANAGER || role == Role.OWNER;
    }

    private ChatAnnouncementAuthorResponse buildAuthorResponse(Long roomId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "작성자를 찾을 수 없습니다."));

        Role role = chatRoomMemberRepository.findByRoomIdAndUserId(roomId, userId)
                .map(ChatRoomMember::getRole)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "작성자의 채팅방 역할을 찾을 수 없습니다."));

        return ChatAnnouncementAuthorResponse.builder()
                .id(userId)
                .username(user.getNickname())
                .role(role)
                .build();
    }

    private Map<Long, ChatAnnouncementAuthorResponse> buildAuthorResponseMap(Long roomId,
                                                                             List<ChatAnnouncement> announcements) {
        if (announcements.isEmpty()) {
            return Collections.emptyMap();
        }

        Set<Long> authorIds = announcements.stream()
                .map(ChatAnnouncement::getAuthorId)
                .collect(Collectors.toSet());

        Map<Long, User> users = userRepository.findAllById(authorIds).stream()
                .collect(Collectors.toMap(User::getId, user -> user, (left, right) -> left));

        Map<Long, Role> roles = chatRoomMemberRepository.findAllByRoomIdAndUserIdIn(roomId, authorIds).stream()
                .collect(Collectors.toMap(ChatRoomMember::getUserId, ChatRoomMember::getRole, (left, right) -> left));

        Map<Long, ChatAnnouncementAuthorResponse> responseMap = new HashMap<>();
        for (Long authorId : authorIds) {
            User user = users.get(authorId);
            Role role = roles.get(authorId);
            if (user == null || role == null) {
                throw new CustomException(ErrorCode.NOT_FOUND, "공지 작성자 정보를 찾을 수 없습니다.");
            }
            responseMap.put(authorId, ChatAnnouncementAuthorResponse.builder()
                    .id(authorId)
                    .username(user.getNickname())
                    .role(role)
                    .build());
        }
        return responseMap;
    }

    private void publishAnnouncementMessage(Long roomId,
                                            Long actorId,
                                            String action,
                                            ChatAnnouncementResponse announcement,
                                            String text) {
        ObjectNode body = objectMapper.createObjectNode();
        body.put("action", action);
        body.put("text", text);

        if (announcement != null) {
            ObjectNode announcementNode = objectMapper.valueToTree(announcement);
            String preview = buildContentPreview(announcement.getContent());
            if (!preview.isEmpty()) {
                announcementNode.put("contentPreview", preview);
            }
            body.set("announcement", announcementNode);
        }

        MessageDto message = MessageDto.builder()
                .roomId(roomId)
                .senderId(actorId)
                .type(MESSAGE_TYPE_NOTICE)
                .body(body)
                .build();

        chatMessageService.send(message);
    }

    private void publishAnnouncementEvent(String action,
                                          Long roomId,
                                          Long announcementId,
                                          ChatAnnouncementResponse response,
                                          Long actorId) {
        ChatAnnouncementEvent event = ChatAnnouncementEvent.builder()
                .action(action)
                .roomId(roomId)
                .actorId(actorId)
                .announcementId(announcementId)
                .announcement(response)
                .build();

        kafkaTemplate.send(ANNOUNCEMENT_TOPIC, String.valueOf(roomId), serialize(event));
    }

    private String serialize(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "공지 이벤트 직렬화에 실패했습니다.");
        }
    }

    private String buildContentPreview(String content) {
        if (content == null || content.isBlank()) {
            return "";
        }

        String collapsed = content.replaceAll("\\s+", " ").trim();
        if (collapsed.length() <= CONTENT_PREVIEW_LIMIT) {
            return collapsed;
        }

        return collapsed.substring(0, CONTENT_PREVIEW_LIMIT) + "...";
    }
}
