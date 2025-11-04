package com.readour.community.entity;

import com.readour.common.entity.Book;
import com.readour.common.entity.User;
import com.readour.community.enums.PostCategory;
import jakarta.persistence.*;
import lombok.*;
import java.time.*;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity @Table(name = "post")
public class Post {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long postId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id")
    private Book book;

    private Long chatRoomId;

    @Enumerated(EnumType.STRING)
    @Column(name = "category")
    private PostCategory category;

    private String title;
    @Lob private String content;
    private Boolean isSpoiler;
    private Integer hit;
    private Boolean isDeleted = false;
    private Boolean isHidden = false;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<PostWarning> warnings = new ArrayList<>();

    // --- Helper Methods
    public void incrementHit() {
        if (this.hit == null) this.hit = 0;
        this.hit++;
    }
    public void updateStatus(boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    // --- Warning Helper Methods
    public void clearWarnings() {
        this.warnings.clear();
    }

    public void addWarning(String warningValue) {
        if (this.postId == null) {
            throw new IllegalArgumentException("Post must be saved before adding warnings");
        }

        PostWarningId warningId = new PostWarningId(this.postId, warningValue);

        PostWarning postWarning = PostWarning.builder()
                .id(warningId)
                .post(this)
                .build();
        if (!this.warnings.contains(postWarning)) {
            this.warnings.add(postWarning);
        }
    }

}
