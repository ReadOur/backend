package com.readour.chat.repository;

import com.readour.chat.entity.ChatMessageHide;
import com.readour.chat.entity.ChatMessageHideId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ChatMessageHideRepository extends JpaRepository<ChatMessageHide, ChatMessageHideId> {

    Optional<ChatMessageHide> findByMsgIdAndUserId(Long msgId, Long userId);

    List<ChatMessageHide> findAllByUserIdAndMsgIdInAndUnhiddenAtIsNull(Long userId, Collection<Long> msgIds);
}
