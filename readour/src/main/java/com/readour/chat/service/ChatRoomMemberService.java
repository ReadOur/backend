package com.readour.chat.service;

import com.readour.chat.entity.ChatRoom;
import com.readour.chat.entity.ChatRoomMember;
import com.readour.chat.repository.ChatRoomMemberRepository;
import com.readour.chat.repository.ChatRoomRepository;
import com.readour.common.enums.ErrorCode;
import com.readour.common.enums.Role;
import com.readour.common.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatRoomMemberService {

    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final ChatRoomRepository chatRoomRepository;

    /**
     * 해당 채팅방의 멤버 또는 관리자의 경우 방 나가기를 실행할 수 있음
     * @param roomId
     * @param userId
     */
    @Transactional
    public void leaveRoom(Long roomId, Long userId) {
        ChatRoomMember member = chatRoomMemberRepository.findByRoomIdAndUserIdAndIsActiveTrue(roomId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST, "활성 상태의 채팅방 멤버가 아닙니다."));

        if (isOwner(member)) {
            throw new CustomException(ErrorCode.FORBIDDEN, "방장은 일반 나가기를 사용할 수 없습니다." +
                    "방장을 위임한 후에 나가기를 하거나, 방 삭제를 진행하세요.");
        }

        deactivateMember(member);
    }

    /**
     * 해당 채팅방에서 역할이 방장인 경우 방 나가기 대신 방을 폭파시킴. 프론트에서 경고메시지 주기...?
     * @param roomId
     * @param ownerId
     * @param confirmDestroy
     */
    @Transactional
    public void destroyRoom(Long roomId, Long ownerId, Boolean confirmDestroy) {
        if (!Boolean.TRUE.equals(confirmDestroy)) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "방 삭제를 확정(confirmDestroy=true)해야 요청이 처리됩니다.");
        }

        ChatRoomMember owner = chatRoomMemberRepository.findByRoomIdAndUserIdAndIsActiveTrue(roomId, ownerId)
                .orElseThrow(() -> new CustomException(ErrorCode.FORBIDDEN, "방에 속한 활성 멤버가 아니거나 권한이 없습니다."));

        if (!isOwner(owner)) {
            throw new CustomException(ErrorCode.FORBIDDEN, "방장만 방을 삭제할 수 있습니다.");
        }

        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "존재하지 않는 채팅방입니다."));

        room.setIsActive(false);
        room.setUpdatedAt(LocalDateTime.now());

        List<ChatRoomMember> activeMembers = chatRoomMemberRepository.findAllByRoomIdAndIsActiveTrue(roomId);
        activeMembers.forEach(this::deactivateMember);
    }

    private void deactivateMember(ChatRoomMember member) {
        member.setIsActive(false);
        member.setMutedUntil(null);
        member.setPinnedAt(null);
        member.setPinOrder(null);
        member.setKickedAt(null);
        member.setKickedBy(null);
        member.setKickReason(null);
    }

    private boolean isOwner(ChatRoomMember member) {
        return member.getRole() != null && member.getRole() == Role.OWNER;
    }
}
