package com.readour.common.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "report",
        uniqueConstraints = @UniqueConstraint(name = "uq_report",
                columnNames = {"reporter_id","target_type","target_id"}))
public class Report {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reportId;

    private Long reporterId;
    private String targetType;
    private Long targetId;
    private String reasonCode;
    private String reasonText;
    private LocalDateTime createdAt;
}
