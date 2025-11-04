package com.readour.chat.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "채팅 공지 생성 요청 DTO")
public class ChatAnnouncementCreateRequest {

    @NotBlank(message = "title은 필수입니다.")
    @Schema(description = "공지 제목", example = "이번 주 독서 모임 안내")
    private String title;

    @NotBlank(message = "content는 필수입니다.")
    @Schema(description = "공지 내용", example = "이번 주 모임은 온라인으로 진행됩니다.")
    private String content;
}
