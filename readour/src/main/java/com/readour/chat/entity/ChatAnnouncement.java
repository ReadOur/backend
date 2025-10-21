package com.readour.chat.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity @Table(name = "chat_announcement")
public class ChatAnnouncement {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long roomId;
    private Long authorId;
    private String title;
    @Lob private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
