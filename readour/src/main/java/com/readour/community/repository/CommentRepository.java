package com.readour.community.repository;

import com.readour.community.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findAllByPostId(Long postId);
    Long countByPostIdAndIsDeletedFalse(Long postId);
    Optional<Comment> findByCommentIdAndIsDeletedFalse(Long commentId);
}