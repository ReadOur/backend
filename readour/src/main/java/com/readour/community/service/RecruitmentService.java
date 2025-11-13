package com.readour.community.service;

import com.readour.chat.dto.request.RoomCreateRequest;
import com.readour.chat.dto.response.RoomCreateResponse;
import com.readour.chat.enums.ChatRoomScope;
import com.readour.chat.service.ChatRoomMemberService;
import com.readour.chat.service.ChatRoomService;
import com.readour.common.entity.User;
import com.readour.common.enums.ErrorCode;
import com.readour.common.repository.UserRepository;
import com.readour.common.exception.CustomException;
import com.readour.community.entity.Post;
import com.readour.community.entity.Recruitment;
import com.readour.community.entity.RecruitmentMember;
import com.readour.community.entity.RecruitmentMemberId;
import com.readour.community.enums.PostCategory;
import com.readour.community.enums.RecruitmentStatus;
import com.readour.community.repository.PostRepository;
import com.readour.community.repository.RecruitmentMemberRepository;
import com.readour.community.repository.RecruitmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecruitmentService {

    private final RecruitmentRepository recruitmentRepository;
    private final RecruitmentMemberRepository recruitmentMemberRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final ChatRoomService chatRoomService;
    private final ChatRoomMemberService chatRoomMemberService;


    /**
     * 모임 지원 토글 (Apply/Cancel 통합)
     *
     * @return boolean: 토글 후의 최종 지원 상태 (true=지원, false=취소)
     */
    @Transactional
    public boolean toggleRecruitment(Long postId, Long userId) {
        // 1. 게시글 및 모임 정보 조회
        Post post = postRepository.findByPostIdAndIsDeletedFalse(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "모집 게시글을 찾을 수 없습니다."));

        Recruitment recruitment = post.getRecruitment();
        if (recruitment == null || post.getCategory() != PostCategory.GROUP) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "모집 게시글이 아닙니다.");
        }

        // 2. 생성자 본인 지원/취소 금지
        if (post.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN, "모임 생성자는 본인의 모임에 지원/취소할 수 없습니다.");
        }

        // 3. 모집 상태 확인 (모집 중일 때만 지원/취소 가능)
        if (recruitment.getStatus() != RecruitmentStatus.RECRUITING) {
            throw new CustomException(ErrorCode.CONFLICT, "이미 모집이 완료되었거나 취소된 모임입니다.");
        }

        // 4. 현재 지원 상태 확인
        RecruitmentMemberId memberId = new RecruitmentMemberId(recruitment.getRecruitmentId(), userId);
        Optional<RecruitmentMember> existingMemberOpt = recruitmentMemberRepository.findById(memberId);

        if (existingMemberOpt.isPresent()) {
            // --- 4a. [취소 로직] 이미 지원한 경우 -> 지원 취소 ---
            recruitment.getMembers().remove(existingMemberOpt.get()); // (orphanRemoval=true로 자동 DELETE)
            recruitment.setCurrentMemberCount(recruitment.getCurrentMemberCount() - 1);

            log.info("User {} cancelled application for recruitment {}", userId, recruitment.getRecruitmentId());
            return false; // 최종 상태: 미지원

        } else {
            // --- 4b. [지원 로직] 미지원 상태인 경우 -> 지원 ---

            // 5. 인원 수 검증
            if (recruitment.getCurrentMemberCount() >= recruitment.getRecruitmentLimit()) {
                // (혹시 모를 상태 동기화)
                recruitment.setStatus(RecruitmentStatus.COMPLETED);
                throw new CustomException(ErrorCode.CONFLICT, "모집 인원이 마감되었습니다.");
            }

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "User not found"));

            RecruitmentMember newMember = RecruitmentMember.builder()
                    .id(memberId)
                    .recruitment(recruitment)
                    .user(user)
                    .build();
            recruitmentMemberRepository.save(newMember); // (Save)

            // 6. 현재 인원 수 증가
            Integer newCount = recruitment.getCurrentMemberCount() + 1;
            recruitment.setCurrentMemberCount(newCount);

            log.info("User {} applied to recruitment {}. Current count: {}/{}", userId, recruitment.getRecruitmentId(), newCount, recruitment.getRecruitmentLimit());

            // 7. 모집 완료 및 채팅방 자동 생성
            if (newCount.equals(recruitment.getRecruitmentLimit())) {
                // 모집 완료 및 채팅방 자동 생성
                recruitment.setStatus(RecruitmentStatus.COMPLETED);
                List<Long> members = recruitmentMemberRepository.findAllUserIdsByRecruitmentId(recruitment.getRecruitmentId());

                RoomCreateRequest roomRequest = new RoomCreateRequest(
                        ChatRoomScope.GROUP,
                        recruitment.getChatRoomName(),
                        recruitment.getChatRoomDescription(),
                        members
                );

                Long authorId = post.getUser().getId();
                RoomCreateResponse roomCreateResponse = chatRoomService.createRoom(authorId, roomRequest);

                recruitment.setChatRoomId(roomCreateResponse.getId());

                log.info("Recruitment COMPLETED. ChatRoom {} created for postId {}.", roomCreateResponse.getId(), postId);
            }
            return true; // 최종 상태: 지원
        }
    }
}