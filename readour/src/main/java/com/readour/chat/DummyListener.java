package com.readour.chat;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class DummyListener {
    @KafkaListener(topics = "readour-test", groupId = "readour-chat-group")
    public void on(String msg) { /* no-op */ }
}
