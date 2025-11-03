package com.readour.chat.repository;

import com.readour.chat.entity.ChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    Optional<ChatMessage> findFirstByRoomIdAndDeletedAtIsNullOrderByCreatedAtDesc(Long roomId);

    Optional<ChatMessage> findFirstByRoomIdOrderByIdDesc(Long roomId);

    long countByRoomIdAndDeletedAtIsNull(Long roomId);

    long countByRoomIdAndDeletedAtIsNullAndIdGreaterThan(Long roomId, Long lastReadMsgId);

    Slice<ChatMessage> findByRoomIdAndDeletedAtIsNull(Long roomId, Pageable pageable);

    Slice<ChatMessage> findByRoomIdAndDeletedAtIsNullAndCreatedAtLessThan(Long roomId, LocalDateTime before, Pageable pageable);
}
