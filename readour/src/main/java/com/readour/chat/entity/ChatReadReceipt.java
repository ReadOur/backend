package com.readour.chat.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity @Table(name = "chat_read_receipt")
@IdClass(ChatReadReceiptId.class)
public class ChatReadReceipt {
    @Id private Long roomId;
    @Id private Long msgId;
    @Id private Long userId;
    private LocalDateTime readAt;
}
