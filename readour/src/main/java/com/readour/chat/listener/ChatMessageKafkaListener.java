package com.readour.chat.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.readour.chat.dto.common.MessageDto;
import com.readour.chat.service.ChatMessageSocketSessionRegistry;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChatMessageKafkaListener {

    private static final Logger log = LoggerFactory.getLogger(ChatMessageKafkaListener.class);

    private final ObjectMapper objectMapper;
    private final ChatMessageSocketSessionRegistry sessionRegistry;

    @KafkaListener(topics = "chat-messages", groupId = "readour-chat-stream")
    public void onMessage(String payload) {
        try {
            MessageDto message = objectMapper.readValue(payload, MessageDto.class);
            sessionRegistry.broadcast(message);
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize chat message payload. payload={}", payload, e);
        }
    }
}
