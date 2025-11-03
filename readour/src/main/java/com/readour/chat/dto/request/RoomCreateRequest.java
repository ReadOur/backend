package com.readour.chat.dto.request;

import com.readour.chat.enums.ChatRoomScope;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "채팅방 생성 요청 DTO")
public class RoomCreateRequest {

    @NotNull(message = "scope는 필수입니다.")
    @Schema(description = "채팅방 범위", example = "ONE_TO_ONE")
    private ChatRoomScope scope;

    @NotBlank(message = "name은 필수입니다.")
    @Schema(description = "채팅방 이름", example = "독서 모임")
    private String name;

    @Schema(description = "채팅방 설명", example = "매주 토요일 독서 토론")
    private String description;

    @NotEmpty(message = "memberIds는 한 명 이상이어야 합니다.")
    @Size(max = 200, message = "memberIds는 최대 200명까지 지정할 수 있습니다.")
    @Schema(description = "초대할 사용자 ID 목록", example = "[1001,1002]")
    private List<Long> memberIds;
}
