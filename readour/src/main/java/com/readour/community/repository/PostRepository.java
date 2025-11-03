package com.readour.community.repository;

import com.readour.community.entity.Post; // Post 엔티티 import
import org.springframework.data.domain.Page; //
import org.springframework.data.domain.Pageable; //
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findAllByCategory(String category);

    List<Post> findByUserId(Long userId);

    @Modifying
    @Query("UPDATE Post p SET p.hit = p.hit + 1 WHERE p.postId = :postId")
    void incrementHit(@Param("postId") Long postId);

    Page<Post> findAllByIsDeletedFalse(Pageable pageable);
    Page<Post> findAllByCategoryAndIsDeletedFalse(String category, Pageable pageable);
}