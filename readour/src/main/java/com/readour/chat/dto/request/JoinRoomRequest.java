package com.readour.chat.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "채팅방 참여 요청 DTO")
public class JoinRoomRequest {

    @NotNull(message = "userId는 필수입니다.")
    @Schema(description = "채팅방에 참여할 사용자 ID", example = "2025001")
    private Long userId;

    @Schema(description = "검색어 필터 ", example = "프로젝트", nullable = true)
    private String query;

    @Min(value = 0, message = "page는 0 이상이어야 합니다.")
    @Schema(description = "재조회할 페이지 번호", example = "0", nullable = true)
    private Integer page;

    @Min(value = 1, message = "size는 1 이상이어야 합니다.")
    @Max(value = 50, message = "size는 50 이하이어야 합니다.")
    @Schema(description = "재조회할 페이지 크기", example = "20", nullable = true)
    private Integer size;
}
