package com.readour.chat.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "채팅방 강퇴 요청 DTO")
public class KickMemberRequest {

    @NotNull(message = "userId는 필수입니다.")
    @Schema(description = "강퇴를 수행하는 사용자 ID", example = "2025001")
    private Long userId;

    @NotNull(message = "targetUserId는 필수입니다.")
    @Schema(description = "강퇴 대상 사용자 ID", example = "2025002")
    private Long targetUserId;

    @Schema(description = "강퇴 사유", example = "공지 위반", nullable = true)
    private String reason;

    @Schema(description = "검색어 필터", example = "프로젝트", nullable = true)
    private String query;

    @Schema(description = "재조회할 페이지 번호", example = "0", nullable = true)
    private Integer page;

    @Schema(description = "재조회할 페이지 크기", example = "20", nullable = true)
    private Integer size;
}
