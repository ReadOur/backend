package com.readour.chat.repository;

import com.readour.chat.entity.ChatAiSession;
import com.readour.chat.enums.ChatAiSessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatAiSessionRepository extends JpaRepository<ChatAiSession, Long> {

    Optional<ChatAiSession> findByRoomIdAndStatus(Long roomId, ChatAiSessionStatus status);

    Optional<ChatAiSession> findTopByRoomIdAndStatusOrderByIdDesc(Long roomId, ChatAiSessionStatus status);

    List<ChatAiSession> findAllByStatus(ChatAiSessionStatus status);
}
