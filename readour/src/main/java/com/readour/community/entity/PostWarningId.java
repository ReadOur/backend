package com.readour.community.entity;

import lombok.*;
import java.io.Serializable;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode
public class PostWarningId implements Serializable {
    private Long postId;
    private String warning;
}
