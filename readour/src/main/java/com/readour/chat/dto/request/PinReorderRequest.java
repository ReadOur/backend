package com.readour.chat.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "채팅방 핀 순서 변경 요청 DTO")
public class PinReorderRequest {

    @NotNull(message = "userId는 필수입니다.")
    @Schema(description = "핀 순서를 변경할 사용자 ID", example = "2025001")
    private Long userId;

    @Valid
    @NotEmpty(message = "orders는 비어 있을 수 없습니다.")
    @Schema(description = "변경할 핀 순서 목록")
    private List<Order> orders;

    @Schema(description = "검색어 필터", example = "프로젝트", nullable = true)
    private String query;

    @Min(value = 0, message = "page는 0 이상이어야 합니다.")
    @Schema(description = "재조회할 페이지 번호", example = "0", nullable = true)
    private Integer page;

    @Min(value = 1, message = "size는 1 이상이어야 합니다.")
    @Max(value = 50, message = "size는 50 이하이어야 합니다.")
    @Schema(description = "재조회할 페이지 크기", example = "20", nullable = true)
    private Integer size;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "핀 순서 정보")
    public static class Order {

        @NotNull(message = "roomId는 필수입니다.")
        @Schema(description = "채팅방 ID", example = "42")
        private Long roomId;

        @NotNull(message = "pinOrder는 필수입니다.")
        @Min(value = 1, message = "pinOrder는 1 이상의 값이어야 합니다.")
        @Schema(description = "순위", example = "2")
        private Integer pinOrder;
    }
}
