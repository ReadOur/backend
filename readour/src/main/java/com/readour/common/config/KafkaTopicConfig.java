package com.readour.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.apache.kafka.clients.admin.NewTopic;

@Configuration
public class KafkaTopicConfig {
    @Bean
    public NewTopic chatMessagesTopic() {
        return TopicBuilder.name("chat-messages")
                .partitions(12)   // 6~12로 시작, 필요 시 24까지 확대
                .replicas(1)      // 로컬/개발: 1, 운영 클러스터는 2~3
                .build();
    }
}

