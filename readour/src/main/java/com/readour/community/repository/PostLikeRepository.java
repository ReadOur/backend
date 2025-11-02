package com.readour.community.repository;

import com.readour.community.entity.PostLike;
import com.readour.community.entity.PostLikeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PostLikeRepository extends JpaRepository<PostLike, PostLikeId> {
    Optional<PostLike> findById(PostLikeId id);
    Long countByIdPostId(Long postId);
    void deleteById(PostLikeId id);
    Boolean existsByIdPostIdAndIdUserId(Long postId, Long userId);
}
