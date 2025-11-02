package com.readour.chat.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.readour.chat.dto.common.MessageDto;
import com.readour.chat.entity.ChatMessage;
import com.readour.chat.repository.ChatMessageRepository;
import com.readour.common.enums.ErrorCode;
import com.readour.common.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private static final String TOPIC = "chat-messages";

    private final ChatMessageRepository chatMessageRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

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

    private String serialize(MessageDto response) {
        try {
            return objectMapper.writeValueAsString(response);
        } catch (JsonProcessingException e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "메시지 직렬화에 실패했습니다.");
        }
    }
}
