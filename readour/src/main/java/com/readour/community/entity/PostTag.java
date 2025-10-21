package com.readour.community.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity @Table(name = "post_tag")
@IdClass(PostTagId.class)
public class PostTag {
    @Id private Long postId;
    @Id private Long tagId;
}
