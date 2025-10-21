package com.readour.community.entity;

import lombok.*;
import java.io.Serializable;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode
public class PostLikeId implements Serializable {
    private Long postId;
    private Long userId;
}
