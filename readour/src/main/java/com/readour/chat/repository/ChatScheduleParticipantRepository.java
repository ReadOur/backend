package com.readour.chat.repository;

import com.readour.chat.entity.ChatScheduleParticipant;
import com.readour.chat.entity.ChatScheduleParticipantId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatScheduleParticipantRepository extends JpaRepository<ChatScheduleParticipant, ChatScheduleParticipantId> {

    boolean existsByScheduleIdAndUserId(Long scheduleId, Long userId);

    List<ChatScheduleParticipant> findAllByScheduleId(Long scheduleId);

    void deleteAllByScheduleId(Long scheduleId);
}
