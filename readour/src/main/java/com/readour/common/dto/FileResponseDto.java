package com.readour.common.dto;

import com.readour.common.entity.FileAsset;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class FileResponseDto {
    private final Long id;
    private final String url;
    private final String originalFilename;
    private final String contentType;
    private final Long size;
    private final Long ownerUserId;
    private final String downloadUrl;

    public static FileResponseDto from(FileAsset asset, String publicUrl) {
        return FileResponseDto.builder()
                .id(asset.getFileId())
                .url(publicUrl)
                .originalFilename(asset.getOriginalName())
                .contentType(asset.getMimeType())
                .size(asset.getByteSize())
                .ownerUserId(asset.getOwnerUserId())
                .downloadUrl("/files/" + asset.getFileId() + "/download")
                .build();
    }
}
