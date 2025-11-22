package com.readour.chat.websocket;

import com.readour.chat.repository.ChatRoomMemberRepository;
import com.readour.common.enums.ErrorCode;
import com.readour.common.exception.CustomException;
import com.readour.common.security.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriTemplate;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class ChatWebSocketHandshakeInterceptor implements HandshakeInterceptor {

    private static final Logger log = LoggerFactory.getLogger(ChatWebSocketHandshakeInterceptor.class);
    private static final UriTemplate URI_TEMPLATE = new UriTemplate("/ws/chat/{roomId}");

    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final JwtTokenProvider jwtTokenProvider;

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
        Long userId;
        try {
            userId = extractUserId(httpServletRequest);
        } catch (CustomException ex) {
            response.setStatusCode(mapToStatus(ex.getErrorCode()));
            return false;
        }
        if (roomIdValue == null) {
            response.setStatusCode(HttpStatus.BAD_REQUEST);
            return false;
        }
        Long roomId;
        try {
            roomId = Long.valueOf(roomIdValue);
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

    private Long extractUserId(HttpServletRequest request) {
        String token = resolveToken(request);
        return jwtTokenProvider.getUserId(token);
    }

    private String resolveToken(HttpServletRequest request) {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(authorization) && authorization.startsWith("Bearer ")) {
            String headerToken = authorization.substring(7);
            if (StringUtils.hasText(headerToken)) {
                return headerToken;
            }
        }
        String queryToken = request.getParameter("token");
        if (StringUtils.hasText(queryToken)) {
            return queryToken;
        }
        throw new CustomException(ErrorCode.UNAUTHORIZED, "JWT 토큰이 필요합니다.");
    }

    private HttpStatus mapToStatus(ErrorCode errorCode) {
        return switch (errorCode) {
            case TOKEN_EXPIRED, INVALID_TOKEN, UNAUTHORIZED -> HttpStatus.UNAUTHORIZED;
            default -> HttpStatus.BAD_REQUEST;
        };
    }
}
