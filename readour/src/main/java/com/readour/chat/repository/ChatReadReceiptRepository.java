package com.readour.chat.repository;

import com.readour.chat.entity.ChatReadReceipt;
import com.readour.chat.entity.ChatReadReceiptId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatReadReceiptRepository extends JpaRepository<ChatReadReceipt, ChatReadReceiptId> {

    Optional<ChatReadReceipt> findTopByRoomIdAndUserIdOrderByMsgIdDesc(Long roomId, Long userId);
}
