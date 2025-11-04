package com.readour.chat.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.readour.chat.dto.common.MessageDto;
import com.readour.chat.dto.response.MessageListResponse;
import com.readour.chat.entity.ChatMessage;
import com.readour.chat.entity.ChatMessageHide;
import com.readour.chat.repository.ChatMessageHideRepository;
import com.readour.chat.repository.ChatMessageRepository;
import com.readour.chat.repository.ChatRoomMemberRepository;
import com.readour.common.entity.FileAsset;
import com.readour.common.enums.ErrorCode;
import com.readour.common.exception.CustomException;
import com.readour.common.service.FileAssetService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private static final String TOPIC = "chat-messages";
    private static final int DEFAULT_LIMIT = 50;
    private static final int MAX_LIMIT = 100;

    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final ChatMessageHideRepository chatMessageHideRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final FileAssetService fileAssetService;

    @Transactional
    public MessageDto send(MessageDto dto) {
        if (dto == null) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "메시지 본문이 비어 있습니다.");
        }
        if (dto.getRoomId() == null || dto.getSenderId() == null) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "roomId와 senderId는 필수입니다.");
        }

        LocalDateTime createdAt = dto.getCreatedAt() != null ? dto.getCreatedAt() : LocalDateTime.now();

        ChatMessage entity = dto.toEntity();
        entity.setCreatedAt(createdAt);

        ChatMessage saved = chatMessageRepository.save(entity);
        MessageDto response = MessageDto.fromEntity(saved);

        kafkaTemplate.send(TOPIC, String.valueOf(response.getRoomId()), serialize(response));

        return response;
    }

    @Transactional
    public MessageDto sendFile(Long roomId, Long userId, MultipartFile file) {
        if (roomId == null || userId == null) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "roomId와 userId는 필수입니다.");
        }
        if (file == null || file.isEmpty()) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "업로드할 파일이 없습니다.");
        }

        chatRoomMemberRepository.findByRoomIdAndUserIdAndIsActiveTrue(roomId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.FORBIDDEN, "채팅방에 참여 중이 아닙니다."));

        FileAsset storedFile = fileAssetService.upload(file, userId);

        LocalDateTime createdAt = LocalDateTime.now();

        var bodyNode = objectMapper.createObjectNode();
        bodyNode.put("fileId", storedFile.getFileId());
        bodyNode.put("url", fileAssetService.buildPublicUrl(storedFile));
        bodyNode.put("name", storedFile.getOriginalName());
        bodyNode.put("contentType", storedFile.getMimeType());
        bodyNode.put("size", storedFile.getByteSize());
        bodyNode.put("uploaderId", userId);
        bodyNode.put("downloadUrl", "/files/" + storedFile.getFileId() + "/download");

        MessageDto dto = MessageDto.builder()
                .roomId(roomId)
                .senderId(userId)
                .type("FILE")
                .body(bodyNode)
                .createdAt(createdAt)
                .build();

        ChatMessage entity = dto.toEntity();
        entity.setCreatedAt(createdAt);

        ChatMessage saved = chatMessageRepository.save(entity);
        fileAssetService.linkFile(storedFile.getFileId(), "CHAT_MESSAGE", saved.getId());
        MessageDto response = MessageDto.fromEntity(saved);

        kafkaTemplate.send(TOPIC, String.valueOf(response.getRoomId()), serialize(response));

        return response;
    }

    @Transactional
    public void hideMessage(Long messageId, Long userId) {
        if (messageId == null || userId == null) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "messageId와 userId는 필수입니다.");
        }

        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "메시지를 찾을 수 없습니다."));

        chatRoomMemberRepository.findByRoomIdAndUserIdAndIsActiveTrue(message.getRoomId(), userId)
                .orElseThrow(() -> new CustomException(ErrorCode.FORBIDDEN, "채팅방에 참여 중이 아닙니다."));

        ChatMessageHide hide = chatMessageHideRepository.findByMsgIdAndUserId(messageId, userId)
                .orElseGet(() -> ChatMessageHide.builder()
                        .msgId(messageId)
                        .userId(userId)
                        .build());
        hide.setHiddenAt(LocalDateTime.now());
        hide.setUnhiddenAt(null);
        chatMessageHideRepository.save(hide);
    }

    @Transactional
    public void unhideMessage(Long messageId, Long userId) {
        if (messageId == null || userId == null) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "messageId와 userId는 필수입니다.");
        }

        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "메시지를 찾을 수 없습니다."));

        chatRoomMemberRepository.findByRoomIdAndUserIdAndIsActiveTrue(message.getRoomId(), userId)
                .orElseThrow(() -> new CustomException(ErrorCode.FORBIDDEN, "채팅방에 참여 중이 아닙니다."));

        chatMessageHideRepository.findByMsgIdAndUserId(messageId, userId)
                .ifPresent(hide -> {
                    hide.setUnhiddenAt(LocalDateTime.now());
                    chatMessageHideRepository.save(hide);
                });
    }

    @Transactional(readOnly = true)
    public MessageListResponse getTimeline(Long roomId, Long userId, LocalDateTime before, Integer limit) {
        if (roomId == null) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "roomId는 필수입니다.");
        }
        if (userId == null) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "userId는 필수입니다.");
        }

        chatRoomMemberRepository.findByRoomIdAndUserIdAndIsActiveTrue(roomId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.FORBIDDEN, "채팅방에 참여 중이 아닙니다."));

        int resolvedLimit = limit == null ? DEFAULT_LIMIT : limit;
        if (resolvedLimit <= 0) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "limit는 1 이상이어야 합니다.");
        }
        if (resolvedLimit > MAX_LIMIT) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "limit는 100 이하이어야 합니다.");
        }

        Sort sort = Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"));
        Pageable pageable = PageRequest.of(0, resolvedLimit, sort);

        Slice<ChatMessage> slice = before != null
                ? chatMessageRepository.findByRoomIdAndDeletedAtIsNullAndCreatedAtLessThan(roomId, before, pageable)
                : chatMessageRepository.findByRoomIdAndDeletedAtIsNull(roomId, pageable);

        List<ChatMessage> entities = slice.getContent();
        List<Long> messageIds = entities.stream()
                .map(ChatMessage::getId)
                .collect(Collectors.toList());

        Set<Long> hiddenMessageIds = messageIds.isEmpty() ? Set.of() : chatMessageHideRepository
                .findAllByUserIdAndMsgIdInAndUnhiddenAtIsNull(userId, messageIds)
                .stream()
                .map(ChatMessageHide::getMsgId)
                .collect(Collectors.toCollection(HashSet::new));

        List<MessageDto> items = entities.stream()
                .filter(entity -> !hiddenMessageIds.contains(entity.getId()))
                .map(MessageDto::fromEntity)
                .collect(Collectors.toCollection(ArrayList::new));

        if (!items.isEmpty()) {
            Collections.reverse(items);
        }

        LocalDateTime nextBefore = (slice.hasNext() && !entities.isEmpty())
                ? entities.get(entities.size() - 1).getCreatedAt()
                : null;

        MessageListResponse.Paging paging = MessageListResponse.Paging.builder()
                .nextBefore(nextBefore)
                .build();

        return MessageListResponse.builder()
                .items(items)
                .paging(paging)
                .build();
    }

    private String serialize(MessageDto response) {
        try {
            return objectMapper.writeValueAsString(response);
        } catch (JsonProcessingException e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "메시지 직렬화에 실패했습니다.");
        }
    }
}
