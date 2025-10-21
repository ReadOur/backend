package com.readour.common.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity @Table(name = "calendar_event")
public class CalendarEvent {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long eventId;

    private Long calendarId;
    private String title;
    @Lob private String description;
    private String location;
    private LocalDateTime startsAt;
    private LocalDateTime endsAt;
    private Boolean allDay;
    private Long createdBy;
    private Boolean isDeleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
