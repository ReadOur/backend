package com.readour.community.dto;

import com.readour.community.entity.Post;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostSummaryDto {
    private Long postId;
    private String title;
    private String category;
    private String authorNickname; // Placeholder for now
    private Integer hit;
    private Long likeCount;
    private LocalDateTime createdAt;
    // Add comment count or like count later if needed

    public static PostSummaryDto fromEntity(Post post) {
        return PostSummaryDto.builder()
                .postId(post.getPostId())
                .title(post.getTitle())
                .category(post.getCategory())
                .authorNickname("temp_user") // TODO: Fetch actual nickname
                .hit(post.getHit())
                .likeCount(builder().likeCount)
                .createdAt(post.getCreatedAt())
                .build();
    }
}