package com.readour.community.repository;

import com.readour.community.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findAllByPostId(Long postId);
    Long countByPostIdAndIsDeletedFalse(Long postId);
    Optional<Comment> findByCommentIdAndIsDeletedFalse(Long commentId);
    Page<Comment> findByUserIdAndIsDeletedFalse(Long userId, Pageable pageable);

    // [N+1 해결] 여러 post ID의 (삭제되지 않은) 댓글 '카운트'를 Map<postId, count>로 반환
    @Query("SELECT c.postId as postId, COUNT(c.commentId) as commentCount " +
            "FROM Comment c WHERE c.postId IN :postIds AND c.isDeleted = false GROUP BY c.postId")
    List<Map<String, Object>> findCommentCountsByPostIds(@Param("postIds") Collection<Long> postIds);
}