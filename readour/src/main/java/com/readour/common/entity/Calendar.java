package com.readour.common.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity @Table(name = "calendar")
public class Calendar {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long calendarId;

    private Long ownerUserId;
    private String scope;
    private Long relatedRoomId;
    private String name;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
