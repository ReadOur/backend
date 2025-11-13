package com.readour.community.repository;

import com.readour.community.entity.PostWarning;
import com.readour.community.entity.PostWarningId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostWarningRepository extends JpaRepository<PostWarning, PostWarningId> {

    List<PostWarning> findByIdPostId(Long postId);

    @Modifying
    @Query("DELETE FROM PostWarning pw WHERE pw.post.postId = :postId")
    void deleteByPostId(@Param("postId") Long postId);
}