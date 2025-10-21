package com.readour.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "공통 응답 DTO")
public class ApiResponseDto<T> {

    @Schema(description = "HTTP 상태 코드", example = "200")
    private int status;

    @Schema(description = "응답 데이터 본문 - 실제 넣을때는 dto가 들어감", example = "{\"id\":1,\"name\":\"홍길동\"}")
    private T body;

    @Schema(description = "상태 코드 별 응답 메시지", example = "요청이 성공적으로 처리되었습니다.")
    private String message;
}
