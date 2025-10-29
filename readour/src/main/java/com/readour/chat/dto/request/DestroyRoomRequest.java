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
@Schema(description = "채팅방 삭제(폭파) 요청 DTO")
public class DestroyRoomRequest {

    @NotNull(message = "ownerId는 필수입니다.")
    @Schema(description = "방장 사용자 ID", example = "123")
    private Long ownerId;

    @NotNull(message = "confirmDestroy는 필수입니다.")
    @Schema(description = "방 삭제 확정 여부", example = "true")
    private Boolean confirmDestroy;

    /*@Schema(description = "삭제 사유", example = "채팅방 사용 종료")
    private String reason;*/
}
