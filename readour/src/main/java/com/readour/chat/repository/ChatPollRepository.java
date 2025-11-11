package com.readour.chat.repository;

import com.readour.chat.entity.ChatPoll;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatPollRepository extends JpaRepository<ChatPoll, Long> {

    Optional<ChatPoll> findByIdAndRoomId(Long id, Long roomId);
}
