package com.readour.community.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity @Table(name = "comment")
public class Comment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long commentId;
    private Long postId;
    private Long userId;
    private Long parentCommentId;
    @Lob private String content;
    private Boolean isDeleted = false;
    private Boolean isHidden = false;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public void updateContent(String content) {
        this.content = content;
    }

    public void updateStatus(boolean isDeleted) {
        this.isDeleted = isDeleted;
    }
}
