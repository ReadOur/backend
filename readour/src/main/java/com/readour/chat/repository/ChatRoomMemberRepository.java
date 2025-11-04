package com.readour.chat.repository;

import com.readour.chat.entity.ChatRoomMember;
import com.readour.chat.entity.ChatRoomMemberId;
import com.readour.chat.repository.projection.RoomMemberCountProjection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatRoomMemberRepository extends JpaRepository<ChatRoomMember, ChatRoomMemberId> {

    Optional<ChatRoomMember> findByRoomIdAndUserId(Long roomId, Long userId);

    Optional<ChatRoomMember> findByRoomIdAndUserIdAndIsActiveTrue(Long roomId, Long userId);

    List<ChatRoomMember> findAllByRoomId(Long roomId);

    List<ChatRoomMember> findAllByRoomIdAndIsActiveTrue(Long roomId);

    List<ChatRoomMember> findAllByUserIdAndIsActiveTrue(Long userId);

    List<ChatRoomMember> findAllByUserIdAndIsActiveTrueAndRoomIdIn(Long userId, Collection<Long> roomIds);

    List<ChatRoomMember> findAllByRoomIdAndUserIdIn(Long roomId, Collection<Long> userIds);

    @Query("select max(m.pinOrder) from ChatRoomMember m where m.userId = :userId and m.isActive = true and m.pinnedAt is not null")
    Integer findMaxPinOrderByUserIdAndIsActiveTrue(@Param("userId") Long userId);

    @Query("select m.roomId as roomId, count(m) as memberCount from ChatRoomMember m where m.roomId in :roomIds and m.isActive = true group by m.roomId")
    List<RoomMemberCountProjection> countActiveMembersByRoomIds(@Param("roomIds") Collection<Long> roomIds);
}
