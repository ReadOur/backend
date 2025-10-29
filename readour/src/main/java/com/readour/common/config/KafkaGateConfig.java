package com.readour.common.config;

import java.util.Properties;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.listener.MessageListenerContainer;

@Configuration
public class KafkaGateConfig {

    private static final Logger log = LoggerFactory.getLogger(KafkaGateConfig.class);

    // 단일 AdminClient 빈 (이름도 명시적으로 변경)
    @Bean(name = "kafkaAdminClient")
    public AdminClient kafkaAdminClient(@Value("${spring.kafka.bootstrap-servers}") String bs) {
        Properties p = new Properties();
        p.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bs);
        return AdminClient.create(p);
    }

    @Bean
    public ApplicationRunner kafkaAutoToggle(
            AdminClient kafkaAdminClient,                           // 타입 주입
            ObjectProvider<KafkaListenerEndpointRegistry> registryProvider
    ) {
        return args -> {
            try {
                kafkaAdminClient.listTopics().names().get(2, TimeUnit.SECONDS);
                KafkaListenerEndpointRegistry registry = registryProvider.getIfAvailable();
                if (registry != null) {
                    for (MessageListenerContainer c : registry.getListenerContainers()) c.start();
                    log.info("Kafka OK -> listeners started");
                } else {
                    log.warn("Kafka OK, but no KafkaListenerEndpointRegistry found. (@EnableKafka? @KafkaListener?)");
                }
            } catch (Exception e) {
                KafkaListenerEndpointRegistry registry = registryProvider.getIfAvailable();
                if (registry != null) for (MessageListenerContainer c : registry.getListenerContainers()) c.stop();
                log.warn("Kafka unavailable -> listeners stopped (or none). cause={}", e.toString());
            }
        };
    }
}
