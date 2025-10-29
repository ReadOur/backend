package com.readour.chat.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "chat_message",
        indexes = @Index(name = "idx_room_created_at", columnList = "room_id, created_at"))
public class ChatMessage {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long roomId;
    private Long senderId;
    private String type;

    @Column(columnDefinition = "json")
    private String body;
    private Long replyToMsgId;
    private LocalDateTime createdAt;
    private LocalDateTime deletedAt;

}
