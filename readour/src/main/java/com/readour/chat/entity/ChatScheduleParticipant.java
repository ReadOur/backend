package com.readour.chat.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "chat_schedule_participant")
@IdClass(ChatScheduleParticipantId.class)
public class ChatScheduleParticipant {

    @Id
    private Long scheduleId;

    @Id
    private Long userId;

    private LocalDateTime joinedAt;
}
