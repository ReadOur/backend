package com.readour.chat.repository;

import com.readour.chat.entity.ChatSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatScheduleRepository extends JpaRepository<ChatSchedule, Long> {

    Optional<ChatSchedule> findByIdAndRoomId(Long id, Long roomId);
}
