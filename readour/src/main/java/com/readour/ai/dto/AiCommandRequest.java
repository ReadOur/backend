package com.readour.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.readour.ai.enums.AiCommandType;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "AI 명령 실행 요청")
public class AiCommandRequest {

    @NotNull(message = "command는 필수입니다.")
    @Schema(description = "실행할 명령", implementation = AiCommandType.class, example = "PUBLIC_SUMMARY")
    private AiCommandType command;

    @Min(value = 5, message = "messageLimit는 최소 5입니다.")
    @Max(value = 400, message = "messageLimit는 최대 400입니다.")
    @Schema(description = "컨텍스트로 사용할 최근 메시지 수 (선택)", example = "30")
    private Integer messageLimit;

    @Schema(description = "추가 지시사항 (선택)", example = "토론에서 합의되지 않은 지점을 강조해줘")
    private String note;
}
