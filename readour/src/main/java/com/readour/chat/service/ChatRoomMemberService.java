package com.readour.chat.service;

import com.readour.chat.entity.ChatRoom;
import com.readour.chat.entity.ChatRoomMember;
import com.readour.chat.repository.ChatMessageRepository;
import com.readour.chat.repository.ChatRoomMemberRepository;
import com.readour.chat.repository.ChatRoomRepository;
import com.readour.chat.dto.response.RoomMemberProfileResponse;
import com.readour.common.repository.UserRepository;
import com.readour.common.entity.User;
import com.readour.common.enums.ErrorCode;
import com.readour.common.enums.Role;
import com.readour.common.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ChatRoomMemberService {

    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;

    /**
     * 해당 채팅방의 멤버 또는 관리자의 경우 방 나가기를 실행할 수 있음
     */
    @Transactional
    public void leaveRoom(Long roomId, Long userId) {
        ChatRoomMember member = getActiveMemberOrThrow(roomId, userId);
        ensureNotOwner(member);
        deactivateMember(member);
    }

    @Transactional
    public void leaveRooms(Long userId, List<Long> roomIds) {
        LinkedHashSet<Long> distinctRoomIds = new LinkedHashSet<>(roomIds);
        for (Long roomId : distinctRoomIds) {
            if (roomId == null) {
                continue;
            }
            ChatRoomMember member = getActiveMemberOrThrow(roomId, userId);
            ensureNotOwner(member);
            deactivateMember(member);
        }
    }

    /**
     * 해당 채팅방에서 역할이 방장인 경우 방 나가기 대신 방을 폭파시킴. 프론트에서 경고메시지 주기...?
     */
    @Transactional
    public void destroyRoom(Long roomId, Long ownerId, Boolean confirmDestroy) {
        if (!Boolean.TRUE.equals(confirmDestroy)) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "방 삭제를 확정(confirmDestroy=true)해야 요청이 처리됩니다.");
        }

        ChatRoomMember owner = getActiveMemberOrThrow(roomId, ownerId);

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

    @Transactional
    public void pinRoom(Long roomId, Long userId) {
        ChatRoomMember member = getActiveMemberOrThrow(roomId, userId);
        LocalDateTime now = LocalDateTime.now();

        if (member.getPinnedAt() == null) {
            Integer currentMaxOrder = chatRoomMemberRepository.findMaxPinOrderByUserIdAndIsActiveTrue(userId);
            int nextOrder = (currentMaxOrder == null ? 0 : currentMaxOrder) + 1;
            member.setPinOrder(nextOrder);
        }

        member.setPinnedAt(now);
    }

    @Transactional
    public void unpinRoom(Long roomId, Long userId) {
        ChatRoomMember member = getActiveMemberOrThrow(roomId, userId);
        member.setPinnedAt(null);
        member.setPinOrder(null);
    }

    @Transactional
    public void reorderPins(Long userId, Map<Long, Integer> desiredOrders) {
        if (desiredOrders == null || desiredOrders.isEmpty()) {
            return;
        }

        Set<Long> roomIds = desiredOrders.keySet();
        List<ChatRoomMember> members = chatRoomMemberRepository.findAllByUserIdAndIsActiveTrueAndRoomIdIn(userId, roomIds);
        if (members.size() != roomIds.size()) {
            throw new CustomException(ErrorCode.FORBIDDEN, "핀 순서를 변경할 권한이 없는 채팅방이 포함되어 있습니다.");
        }

        Map<Long, ChatRoomMember> memberByRoomId = new HashMap<>();
        for (ChatRoomMember member : members) {
            if (member.getPinnedAt() == null) {
                throw new CustomException(ErrorCode.BAD_REQUEST, "핀으로 고정된 방만 순서를 변경할 수 있습니다.");
            }
            memberByRoomId.put(member.getRoomId(), member);
        }

        Set<Integer> usedOrders = new HashSet<>();
        for (Map.Entry<Long, Integer> entry : desiredOrders.entrySet()) {
            Integer order = entry.getValue();
            if (order == null || order < 1) {
                throw new CustomException(ErrorCode.BAD_REQUEST, "pinOrder는 1 이상의 정수여야 합니다.");
            }
            if (!usedOrders.add(order)) {
                throw new CustomException(ErrorCode.BAD_REQUEST, "pinOrder 값은 중복될 수 없습니다.");
            }
        }

        desiredOrders.forEach((roomId, order) -> {
            ChatRoomMember member = memberByRoomId.get(roomId);
            if (member == null) {
                throw new CustomException(ErrorCode.BAD_REQUEST, "존재하지 않는 채팅방입니다.");
            }
            member.setPinOrder(order);
        });
    }

    @Transactional
    public void joinRoom(Long roomId, Long userId) {
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "존재하지 않는 채팅방입니다."));

        if (!Boolean.TRUE.equals(room.getIsActive())) {
            throw new CustomException(ErrorCode.FORBIDDEN, "비활성화된 채팅방입니다.");
        }

        LocalDateTime now = LocalDateTime.now();
        Long latestMsgId = resolveLatestMessageId(roomId);

        ChatRoomMember member = chatRoomMemberRepository.findByRoomIdAndUserId(roomId, userId).orElse(null);
        if (member == null) {
            ChatRoomMember newMember = ChatRoomMember.builder()
                    .roomId(roomId)
                    .userId(userId)
                    .role(Role.MEMBER)
                    .joinedAt(now)
                    .mutedUntil(null)
                    .lastReadMsgId(latestMsgId)
                    .isActive(true)
                    .pinnedAt(null)
                    .pinOrder(null)
                    .kickedAt(null)
                    .kickedBy(null)
                    .kickReason(null)
                    .build();
            chatRoomMemberRepository.save(newMember);
        } else {
            if (Boolean.TRUE.equals(member.getIsActive())) {
                return; // 이미 참여 중이면 그대로 둔다.
            }
            member.setIsActive(true);
            member.setJoinedAt(now);
            member.setMutedUntil(null);
            member.setPinnedAt(null);
            member.setPinOrder(null);
            member.setKickedAt(null);
            member.setKickedBy(null);
            member.setKickReason(null);
            member.setLastReadMsgId(latestMsgId);
        }

        room.setUpdatedAt(now);
    }

    @Transactional
    public void kickMember(Long roomId, Long actorUserId, Long targetUserId, String reason) {
        if (roomId == null || actorUserId == null || targetUserId == null) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "roomId, userId, targetUserId는 필수입니다.");
        }
        if (Objects.equals(actorUserId, targetUserId)) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "자기 자신을 강퇴할 수 없습니다.");
        }

        ChatRoomMember actor = getActiveMemberOrThrow(roomId, actorUserId);
        if (!isManagerOrOwner(actor)) {
            throw new CustomException(ErrorCode.FORBIDDEN, "강퇴 권한이 없습니다.");
        }

        ChatRoomMember target = chatRoomMemberRepository.findByRoomIdAndUserIdAndIsActiveTrue(roomId, targetUserId)
                .orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST, "강퇴 대상이 채팅방에 참여 중이 아닙니다."));

        if (isOwner(target)) {
            throw new CustomException(ErrorCode.FORBIDDEN, "방장은 강퇴할 수 없습니다.");
        }
        if (actor.getRole() == Role.MANAGER && target.getRole() == Role.MANAGER) {
            throw new CustomException(ErrorCode.FORBIDDEN, "매니저는 다른 매니저를 강퇴할 수 없습니다.");
        }

        LocalDateTime now = LocalDateTime.now();
        target.setIsActive(false);
        target.setMutedUntil(null);
        target.setPinnedAt(null);
        target.setPinOrder(null);
        target.setKickedAt(now);
        target.setKickedBy(actorUserId);
        target.setKickReason(reason);
        chatRoomMemberRepository.save(target);
    }

    @Transactional(readOnly = true)
    public RoomMemberProfileResponse getMemberProfile(Long roomId, Long requesterId, Long targetUserId) {
        if (roomId == null || requesterId == null || targetUserId == null) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "roomId, requesterId, targetUserId는 필수입니다.");
        }

        getActiveMemberOrThrow(roomId, requesterId);

        ChatRoomMember target = chatRoomMemberRepository.findByRoomIdAndUserIdAndIsActiveTrue(roomId, targetUserId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "채팅방에 존재하지 않는 사용자입니다."));

        User user = userRepository.findById(targetUserId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "사용자 정보를 찾을 수 없습니다."));

        return RoomMemberProfileResponse.builder()
                .userId(user.getId())
                .nickname(user.getNickname())
                .role(target.getRole().name())
                .build();
    }

    private ChatRoomMember getActiveMemberOrThrow(Long roomId, Long userId) {
        return chatRoomMemberRepository.findByRoomIdAndUserIdAndIsActiveTrue(roomId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST, "활성 상태의 채팅방 멤버가 아닙니다."));
    }

    private void deactivateMember(ChatRoomMember member) {
        member.setIsActive(false);
        member.setMutedUntil(null);
        member.setPinnedAt(null);
        member.setPinOrder(null);
        member.setKickedAt(null);
        member.setKickedBy(null);
        member.setKickReason(null);
        chatRoomMemberRepository.save(member);
    }

    private void ensureNotOwner(ChatRoomMember member) {
        if (isOwner(member)) {
            throw new CustomException(ErrorCode.FORBIDDEN,
                    "방장을 위임한 후에 나가기를 하거나, 방 삭제를 진행하세요.");
        }
    }

    private boolean isOwner(ChatRoomMember member) {
        return member.getRole() != null && member.getRole() == Role.OWNER;
    }

    private boolean isManagerOrOwner(ChatRoomMember member) {
        if (member.getRole() == null) {
            return false;
        }
        return member.getRole() == Role.MANAGER || member.getRole() == Role.OWNER;
    }

    private Long resolveLatestMessageId(Long roomId) {
        return chatMessageRepository.findFirstByRoomIdOrderByIdDesc(roomId)
                .map(message -> message.getId())
                .orElse(null);
    }
}
