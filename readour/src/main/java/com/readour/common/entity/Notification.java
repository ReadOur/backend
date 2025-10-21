package com.readour.common.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity @Table(name = "notification")
public class Notification {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long notificationId;

    private Long userId;
    private String type;
    private String title;
    private String message;
    @Lob private String linkUrl;
    private String targetType;
    private Long targetId;
    private Boolean isRead;
    private LocalDateTime readAt;
    private LocalDateTime createdAt;
}
