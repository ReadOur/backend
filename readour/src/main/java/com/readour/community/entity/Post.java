package com.readour.community.entity;

import com.readour.community.dto.PostUpdateRequestDto;
import jakarta.persistence.*;
import lombok.*;
import java.time.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity @Table(name = "post")
public class Post {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long postId;
    private Long userId;
    private Long bookId;
    private Long chatRoomId;
    private String category;
    private String title;
    @Lob private String content;
    private Boolean isSpoiler;
    private Integer hit;
    private Boolean isDeleted;
    private Boolean isHidden;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    public void incrementHit() {
        this.hit++;
    }
    public void updateStatus(boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    public void update(PostUpdateRequestDto requestDto) {
        this.title = requestDto.getTitle();
        this.content = requestDto.getContent();
        this.category = requestDto.getCategory();
        this.bookId = requestDto.getBookId();
        this.isSpoiler = requestDto.getIsSpoiler();
    }
}
