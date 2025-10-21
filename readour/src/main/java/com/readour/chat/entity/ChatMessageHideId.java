package com.readour.chat.entity;

import lombok.*;
import java.io.Serializable;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode
public class ChatMessageHideId implements Serializable {
    private Long msgId;
    private Long userId;
}
