package com.readour.chat.repository;

import com.readour.chat.entity.ChatAnnouncement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatAnnouncementRepository extends JpaRepository<ChatAnnouncement, Long> {

    Page<ChatAnnouncement> findAllByRoomId(Long roomId, Pageable pageable);

    Optional<ChatAnnouncement> findByIdAndRoomId(Long id, Long roomId);
}
