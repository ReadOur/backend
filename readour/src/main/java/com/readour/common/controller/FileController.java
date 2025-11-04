package com.readour.common.controller;

import com.readour.common.dto.ApiResponseDto;
import com.readour.common.dto.FileResponseDto;
import com.readour.common.entity.FileAsset;
import com.readour.common.enums.ErrorCode;
import com.readour.common.exception.CustomException;
import com.readour.common.service.FileAssetService;
import com.readour.common.service.FileDownload;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;

@RestController
@RequiredArgsConstructor
@RequestMapping("/files")
public class FileController {

    private final FileAssetService fileAssetService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "파일 업로드 / 구현 및 테스트 함 / targetType : POST or CHAT_ROOM, targetId는 해당 게시글 또는 채팅방 / POSTMAN 테스트")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "파일 업로드 성공",
                    content = @Content(schema = @Schema(implementation = FileResponseDto.class)))
    })
    public ResponseEntity<ApiResponseDto<FileResponseDto>> upload(
            @RequestPart("file")
            @Schema(type = "string", format = "binary", description = "업로드할 파일") MultipartFile file,

            @RequestHeader(value = "X-User-Id")
            Long userId,

            @RequestParam(value = "targetType")
            String targetType,

            @RequestParam(value = "targetId")
            Long targetId
    ) {
        FileAsset uploaded = fileAssetService.upload(file, userId);
        if (StringUtils.hasText(targetType) && targetId != null) {
            fileAssetService.linkFile(uploaded.getFileId(), targetType, targetId);
        }

        FileResponseDto body = fileAssetService.toResponse(uploaded);
        ApiResponseDto<FileResponseDto> response = ApiResponseDto.<FileResponseDto>builder()
                .status(200)
                .body(body)
                .message("파일을 업로드했습니다.")
                .build();
        return ResponseEntity.ok(response);
    }


    @Operation(summary = "파일 메타데이터 조회 / 구현 및 테스트 완료")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "메타데이터 조회 성공",
                    content = @Content(schema = @Schema(implementation = FileResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "파일 없음",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class)))
    })
    @GetMapping("/{fileId}")
    public ResponseEntity<ApiResponseDto<FileResponseDto>> getMetadata(@PathVariable Long fileId) {
        FileAsset fileAsset = fileAssetService.getFile(fileId);
        FileResponseDto body = fileAssetService.toResponse(fileAsset);
        ApiResponseDto<FileResponseDto> response = ApiResponseDto.<FileResponseDto>builder()
                .status(200)
                .body(body)
                .message("파일 정보를 조회했습니다.")
                .build();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "파일 다운로드 / 구현 및 테스트 함 ")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "다운로드 성공"),
            @ApiResponse(responseCode = "404", description = "파일 없음",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class)))
    })
    @GetMapping("/{fileId}/download")
    public ResponseEntity<InputStreamResource> download(@PathVariable Long fileId) {
        FileDownload download = fileAssetService.download(fileId);
        FileAsset storedFile = download.getAsset();

        MediaType mediaType = parseMediaType(storedFile.getMimeType());
        String encodedFilename = UriUtils.encode(storedFile.getOriginalName(), StandardCharsets.UTF_8);

        InputStreamResource resource = new InputStreamResource(download.getInputStream()) {
            @Override
            public long contentLength() {
                return storedFile.getByteSize() != null ? storedFile.getByteSize() : -1;
            }

            @Override
            public String getFilename() {
                return storedFile.getOriginalName();
            }
        };

        ResponseEntity.BodyBuilder builder = ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFilename)
                .contentType(mediaType);

        if (storedFile.getByteSize() != null && storedFile.getByteSize() >= 0) {
            builder.contentLength(storedFile.getByteSize());
        }

        return builder.body(resource);
    }

    private MediaType parseMediaType(String contentType) {
        if (!StringUtils.hasText(contentType)) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
        try {
            return MediaType.parseMediaType(contentType);
        } catch (IllegalArgumentException e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "지원하지 않는 콘텐츠 타입입니다.");
        }
    }
}
