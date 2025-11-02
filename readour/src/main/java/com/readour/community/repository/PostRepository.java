package com.readour.community.repository;

import com.readour.community.entity.Post;
import com.readour.community.enums.PostCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long>, JpaSpecificationExecutor<Post> {

    List<Post> findByUser_Id(Long userId);
    @Modifying
    @Query("UPDATE Post p SET p.hit = p.hit + 1 WHERE p.postId = :postId")
    void incrementHit(@Param("postId") Long postId);

    Page<Post> findAllByIsDeletedFalse(Pageable pageable);
    Page<Post> findAllByCategoryAndIsDeletedFalse(PostCategory category, Pageable pageable);
    Optional<Post> findByPostIdAndIsDeletedFalse(Long postId);
}