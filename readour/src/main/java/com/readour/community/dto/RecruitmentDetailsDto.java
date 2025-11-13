package com.readour.community.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.readour.common.entity.User;
import com.readour.community.entity.Recruitment;
import com.readour.community.entity.RecruitmentMember;
import com.readour.community.enums.RecruitmentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
@Schema(description = "모임 모집 상세 DTO")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RecruitmentDetailsDto {

    @Schema(description = "모집 ID")
    private Long recruitmentId;

    @Schema(description = "모집 인원 (제한)")
    private Integer recruitmentLimit;

    @Schema(description = "현재 인원")
    private Integer currentMemberCount;

    @Schema(description = "모집 상태 (RECRUITING, COMPLETED)")
    private RecruitmentStatus status;

    @Schema(description = "채팅방 이름")
    private String chatRoomName;

    @Schema(description = "채팅방 설명")
    private String chatRoomDescription;

    @Schema(description = "모집 완료 시 생성된 채팅방 ID (COMPLETED 상태일 때만 존재)")
    private Long chatRoomId;

    @Schema(description = "참가자 목록")
    private List<MemberDto> members;

    @Schema(description = "현재 사용자가 이 모임에 지원했는지 여부")
    private Boolean isApplied;

    // 참가자 내부 DTO
    @Getter
    @Builder
    static class MemberDto {
        private Long userId;
        private String nickname;
        public static MemberDto from(User user) {
            return MemberDto.builder()
                    .userId(user.getId())
                    .nickname(user.getNickname())
                    .build();
        }
    }

    public static RecruitmentDetailsDto fromEntity(Recruitment recruitment, boolean isApplied) {
        if (recruitment == null) return null;

        List<MemberDto> memberDtos = recruitment.getMembers().stream()
                .map(RecruitmentMember::getUser)
                .map(MemberDto::from)
                .collect(Collectors.toList());

        return RecruitmentDetailsDto.builder()
                .recruitmentId(recruitment.getRecruitmentId())
                .recruitmentLimit(recruitment.getRecruitmentLimit())
                .currentMemberCount(recruitment.getCurrentMemberCount())
                .status(recruitment.getStatus())
                .chatRoomName(recruitment.getChatRoomName())
                .chatRoomDescription(recruitment.getChatRoomDescription())
                .chatRoomId(recruitment.getChatRoomId())
                .members(memberDtos)
                .isApplied(isApplied)
                .build();
    }
}