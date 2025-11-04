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
@Schema(description = "채팅 공지 수정 요청 DTO")
public class ChatAnnouncementUpdateRequest {

    @NotBlank(message = "title은 필수입니다.")
    @Schema(description = "수정할 공지 제목", example = "이번 주 독서 모임 안내 (변경)")
    private String title;

    @NotBlank(message = "content는 필수입니다.")
    @Schema(description = "수정할 공지 내용", example = "시간이 8시로 변경되었습니다.")
    private String content;
}
