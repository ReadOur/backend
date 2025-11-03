package com.readour.chat.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.readour.chat.dto.common.MessageDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
public class ChatMessageSocketSessionRegistry {

    private static final Logger log = LoggerFactory.getLogger(ChatMessageSocketSessionRegistry.class);

    private final Map<Long, Set<WebSocketSession>> sessionsByRoom = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;

    public ChatMessageSocketSessionRegistry(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void register(Long roomId, WebSocketSession session) {
        if (roomId == null || session == null) {
            return;
        }
        sessionsByRoom.computeIfAbsent(roomId, id -> new CopyOnWriteArraySet<>()).add(session);
    }

    public void unregister(Long roomId, WebSocketSession session) {
        if (roomId == null || session == null) {
            return;
        }
        Set<WebSocketSession> sessions = sessionsByRoom.get(roomId);
        if (sessions == null) {
            return;
        }
        sessions.remove(session);
        if (sessions.isEmpty()) {
            sessionsByRoom.remove(roomId);
        }
    }

    public void broadcast(MessageDto message) {
        if (message == null || message.getRoomId() == null) {
            return;
        }

        Set<WebSocketSession> sessions = sessionsByRoom.get(message.getRoomId());
        if (sessions == null || sessions.isEmpty()) {
            return;
        }

        String payload;
        try {
            payload = objectMapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize message for websocket broadcast. roomId={}", message.getRoomId(), e);
            return;
        }

        TextMessage textMessage = new TextMessage(payload);
        for (WebSocketSession session : sessions) {
            if (!session.isOpen()) {
                unregister(message.getRoomId(), session);
                continue;
            }
            try {
                session.sendMessage(textMessage);
            } catch (IOException e) {
                log.debug("Failed to send websocket message. roomId={}, sessionId={}", message.getRoomId(), session.getId(), e);
                closeSilently(session);
                unregister(message.getRoomId(), session);
            }
        }
    }

    private void closeSilently(WebSocketSession session) {
        try {
            session.close();
        } catch (IOException ignored) {
            // ignore close errors
        }
    }
}
