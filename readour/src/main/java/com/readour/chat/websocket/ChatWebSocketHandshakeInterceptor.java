package com.readour.chat.websocket;

import com.readour.chat.repository.ChatRoomMemberRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriTemplate;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ChatWebSocketHandshakeInterceptor implements HandshakeInterceptor {

    private static final Logger log = LoggerFactory.getLogger(ChatWebSocketHandshakeInterceptor.class);
    private static final UriTemplate URI_TEMPLATE = new UriTemplate("/ws/chat/{roomId}");

    private final ChatRoomMemberRepository chatRoomMemberRepository;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        if (!(request instanceof ServletServerHttpRequest servletRequest)) {
            response.setStatusCode(HttpStatus.BAD_REQUEST);
            return false;
        }

        HttpServletRequest httpServletRequest = servletRequest.getServletRequest();
        String requestUri = httpServletRequest.getRequestURI();
        String contextPath = httpServletRequest.getContextPath();
        if (contextPath != null && !contextPath.isBlank() && requestUri.startsWith(contextPath)) {
            requestUri = requestUri.substring(contextPath.length());
        }
        Map<String, String> vars;
        try {
            vars = URI_TEMPLATE.match(requestUri);
        } catch (IllegalArgumentException e) {
            response.setStatusCode(HttpStatus.BAD_REQUEST);
            return false;
        }

        String roomIdValue = vars.get("roomId");
        String userIdHeader = httpServletRequest.getHeader("X-User-Id");
        if (roomIdValue == null || userIdHeader == null) {
            response.setStatusCode(HttpStatus.BAD_REQUEST);
            return false;
        }

        Long roomId;
        Long userId;
        try {
            roomId = Long.valueOf(roomIdValue);
            userId = Long.valueOf(userIdHeader);
        } catch (NumberFormatException e) {
            response.setStatusCode(HttpStatus.BAD_REQUEST);
            return false;
        }

        boolean isMember = chatRoomMemberRepository.findByRoomIdAndUserIdAndIsActiveTrue(roomId, userId).isPresent();
        if (!isMember) {
            response.setStatusCode(HttpStatus.FORBIDDEN);
            return false;
        }

        attributes.put("roomId", roomId);
        attributes.put("userId", userId);
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        if (exception != null) {
            log.debug("WebSocket handshake failed: {}", exception.getMessage());
        }
    }
}
