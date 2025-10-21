package com.readour.chat.entity;

import lombok.*;
import java.io.Serializable;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode
public class ChatPollVoteId implements Serializable {
    private Long pollMsgId;
    private Long userId;
}
