package com.readour.chat.repository;

import com.readour.chat.entity.ChatAiSessionSummary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatAiSessionSummaryRepository extends JpaRepository<ChatAiSessionSummary, Long> {

    List<ChatAiSessionSummary> findAllBySessionIdOrderByIdAsc(Long sessionId);
}

