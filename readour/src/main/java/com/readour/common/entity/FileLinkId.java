package com.readour.common.entity;

import lombok.*;
import java.io.Serializable;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode
public class FileLinkId implements Serializable {
    private Long fileId;
    private String targetType;
    private Long targetId;
}
