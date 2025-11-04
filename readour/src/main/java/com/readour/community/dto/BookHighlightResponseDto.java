package com.readour.community.dto;

import com.readour.common.entity.User;
import com.readour.community.entity.BookHighlight;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "책 하이라이트 응답 DTO")
public class BookHighlightResponseDto {

    @Schema(description = "하이라이트 ID", example = "1")
    private Long highlightId;

    @Schema(description = "책 ID", example = "101")
    private Long bookId;

    @Schema(description = "작성자 ID", example = "201")
    private Long authorId;

    @Schema(description = "작성자 닉네임", example = "독서광")
    private String authorNickname;

    @Schema(description = "하이라이트 내용", example = "우리는 모두 연결되어 있다.")
    private String content;

    @Schema(description = "페이지 번호", example = "123")
    private Integer pageNumber;

    @Schema(description = "생성 시각")
    private LocalDateTime createdAt;

    @Schema(description = "수정 시각")
    private LocalDateTime updatedAt;

    public static BookHighlightResponseDto fromEntity(BookHighlight highlight, User author) {
        return BookHighlightResponseDto.builder()
                .highlightId(highlight.getHighlightId())
                .bookId(highlight.getBookId())
                .authorId(highlight.getUserId())
                .authorNickname(author != null ? author.getNickname() : "알 수 없음")
                .content(highlight.getContent())
                .pageNumber(highlight.getPageNumber())
                .createdAt(highlight.getCreatedAt())
                .updatedAt(highlight.getUpdatedAt())
                .build();
    }
}
