package com.readour.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@Schema(description = "임시 파일 업로드 응답 DTO")
public class TempFileUploadResponseDto {

    @Schema(description = "임시 업로드 컨텍스트 ID")
    private Long tempId;

    @Schema(description = "업로드된 파일 목록")
    private List<FileResponseDto> files;
}
