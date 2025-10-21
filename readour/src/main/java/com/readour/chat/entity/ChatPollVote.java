package com.readour.chat.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity @Table(name = "chat_poll_vote")
@IdClass(ChatPollVoteId.class)
public class ChatPollVote {
    @Id private Long pollMsgId;
    @Id private Long userId;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private String selectedOptions;
    private LocalDateTime votedAt;
}
