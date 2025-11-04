package com.readour.chat.entity;

import lombok.EqualsAndHashCode;

import java.io.Serializable;

@EqualsAndHashCode
public class ChatScheduleParticipantId implements Serializable {
    private Long scheduleId;
    private Long userId;
}
