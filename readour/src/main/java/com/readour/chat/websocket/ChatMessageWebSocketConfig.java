package com.readour.chat.websocket;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class ChatMessageWebSocketConfig implements WebSocketConfigurer {

    private final ChatMessageWebSocketHandler chatMessageWebSocketHandler;
    private final ChatWebSocketHandshakeInterceptor chatWebSocketHandshakeInterceptor;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(chatMessageWebSocketHandler, "/ws/chat/{roomId}")
                .addInterceptors(chatWebSocketHandshakeInterceptor)
                .setAllowedOriginPatterns("*");
    }
}
