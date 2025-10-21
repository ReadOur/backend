package com.readour.common.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity @Table(name = "file_link")
@IdClass(FileLinkId.class)
public class FileLink {
    @Id private Long fileId;
    @Id private String targetType;
    @Id private Long targetId;
    private LocalDateTime createdAt;
}
