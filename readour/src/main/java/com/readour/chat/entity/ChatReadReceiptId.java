package com.readour.chat.entity;

import lombok.*;
import java.io.Serializable;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode
public class ChatReadReceiptId implements Serializable {
    private Long roomId;
    private Long msgId;
    private Long userId;
}
