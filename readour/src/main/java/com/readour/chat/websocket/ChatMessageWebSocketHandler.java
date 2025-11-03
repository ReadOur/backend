package com.readour.chat.websocket;

import com.readour.chat.service.ChatMessageSocketSessionRegistry;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
@RequiredArgsConstructor
public class ChatMessageWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(ChatMessageWebSocketHandler.class);

    private final ChatMessageSocketSessionRegistry sessionRegistry;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Long roomId = (Long) session.getAttributes().get("roomId");
        if (roomId == null) {
            closeSession(session, CloseStatus.BAD_DATA);
            return;
        }
        sessionRegistry.register(roomId, session);
        log.debug("WebSocket connected. sessionId={}, roomId={}", session.getId(), roomId);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long roomId = (Long) session.getAttributes().get("roomId");
        sessionRegistry.unregister(roomId, session);
        log.debug("WebSocket disconnected. sessionId={}, roomId={}, status={}", session.getId(), roomId, status);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        // 현재 서버에서는 클라이언트 송신을 REST + Kafka 기반으로 처리하므로 메시지는 무시한다.
        // 필요 시 핑/퐁이나 커맨드 처리를 추가할 수 있다.
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        Long roomId = (Long) session.getAttributes().get("roomId");
        sessionRegistry.unregister(roomId, session);
        closeSession(session, CloseStatus.SERVER_ERROR);
        log.debug("WebSocket transport error. sessionId={}, roomId={}", session.getId(), roomId, exception);
    }

    private void closeSession(WebSocketSession session, CloseStatus status) {
        try {
            session.close(status);
        } catch (Exception ignored) {
            // ignore close errors
        }
    }
}
