package com.readour.chat.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "ai_job",
        indexes = {
                @Index(name = "idx_room_started", columnList = "room_id, started_at"),
                @Index(name = "idx_requester_started", columnList = "requester_id, started_at")
        },
        uniqueConstraints = @UniqueConstraint(name = "uq_dedupe_key", columnNames = {"dedupe_key"}))
public class AiJob {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long roomId;
    private Long requesterId;
    private String action;
    private String taskType;
    private String scopeType;
    @JdbcTypeCode(SqlTypes.JSON) @Column(columnDefinition = "json")
    private String scopeParam;
    @JdbcTypeCode(SqlTypes.JSON) @Column(columnDefinition = "json")
    private String options;
    @Lob private String prompt;
    private String status;
    @JdbcTypeCode(SqlTypes.JSON) @Column(columnDefinition = "json")
    private String payload;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    @Lob private String error;
    private String dedupeKey;
    private Long costTokens;
    private Integer latencyMs;
}
