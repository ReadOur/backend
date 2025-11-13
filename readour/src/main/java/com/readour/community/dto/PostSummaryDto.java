package com.readour.community.dto;

import com.readour.common.entity.User;
import com.readour.community.entity.Book;
import com.readour.community.entity.Post;
import com.readour.community.entity.Recruitment;
import com.readour.community.enums.PostCategory;
import io.swagger.v3.oas.annotations.media.Schema;
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
    private PostCategory category;
    private String authorNickname;
    private Integer hit;
    private Long likeCount;
    private Long commentCount;
    private Boolean isLiked;
    private Boolean isSpoiler;
    private Long bookId;
    private LocalDateTime createdAt;

    @Schema(description = "현재 인원 (GROUP 전용)")
    private Integer currentMemberCount;

    @Schema(description = "모집 정원 (GROUP 전용)")
    private Integer recruitmentLimit;

    @Schema(description = "현재 사용자가 이 모임에 지원했는지 여부 (GROUP 전용)")
    private Boolean isApplied;

    public static PostSummaryDto fromEntity(Post post, Long likeCount, Long commentCount, Boolean isLiked, Boolean isApplied) {
        User author = post.getUser();
        Book book = post.getBook();
        Recruitment recruitment = post.getRecruitment();

        PostSummaryDtoBuilder builder = PostSummaryDto.builder()
                .postId(post.getPostId())
                .title(post.getTitle())
                .category(post.getCategory())
                .authorNickname(author != null ? author.getNickname() : "탈퇴한 유저")
                .hit(post.getHit())
                .likeCount(likeCount)
                .commentCount(commentCount)
                .isLiked(isLiked)
                .isSpoiler(post.getIsSpoiler())
                .bookId(book != null ? book.getBookId() : null)
                .createdAt(post.getCreatedAt());

        if (recruitment != null && post.getCategory() == PostCategory.GROUP) {
            builder.currentMemberCount(recruitment.getCurrentMemberCount())
                    .recruitmentLimit(recruitment.getRecruitmentLimit())
                    .isApplied(isApplied);
        }

        return builder.build();
    }
}