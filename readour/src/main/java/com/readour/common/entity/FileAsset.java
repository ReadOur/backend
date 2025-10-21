package com.readour.common.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity @Table(name = "file_asset")
public class FileAsset {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long fileId;

    private Long ownerUserId;
    private String bucket;
    private String objectKey;
    private String originalName;
    private String mimeType;
    private Long byteSize;
    private Boolean isDeleted;
    private LocalDateTime createdAt;
}
