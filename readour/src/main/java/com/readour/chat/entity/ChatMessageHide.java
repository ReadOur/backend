package com.readour.chat.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity @Table(name = "chat_message_hide")
@IdClass(ChatMessageHideId.class)
public class ChatMessageHide {
    @Id private Long msgId;
    @Id private Long userId;
    private LocalDateTime hiddenAt;
    private LocalDateTime unhiddenAt;
}
