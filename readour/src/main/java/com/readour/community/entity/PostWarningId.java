package com.readour.community.entity;

import jakarta.persistence.Embeddable;
import lombok.*;
import java.io.Serializable;

@Embeddable
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode
public class PostWarningId implements Serializable {
    private Long postId;
    private String warning;
}
