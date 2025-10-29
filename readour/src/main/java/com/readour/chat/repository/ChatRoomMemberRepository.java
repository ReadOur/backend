package com.readour.chat.repository;

import com.readour.chat.entity.ChatRoomMember;
import com.readour.chat.entity.ChatRoomMemberId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatRoomMemberRepository extends JpaRepository<ChatRoomMember, ChatRoomMemberId> {

    Optional<ChatRoomMember> findByRoomIdAndUserId(Long roomId, Long userId);

    Optional<ChatRoomMember> findByRoomIdAndUserIdAndIsActiveTrue(Long roomId, Long userId);

    List<ChatRoomMember> findAllByRoomId(Long roomId);

    List<ChatRoomMember> findAllByRoomIdAndIsActiveTrue(Long roomId);
}
