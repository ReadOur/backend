package com.readour.community.repository;

import com.readour.community.entity.PostLike;
import com.readour.community.entity.PostLikeId;
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
import java.util.Set;

@Repository
public interface PostLikeRepository extends JpaRepository<PostLike, PostLikeId> {
    Optional<PostLike> findById(PostLikeId id);
    Long countByIdPostId(Long postId);
    void deleteById(PostLikeId id);
    Boolean existsByIdPostIdAndIdUserId(Long postId, Long userId);
    Page<PostLike> findAllByIdUserId(Long userId, Pageable pageable);

    // [N+1 해결] 여러 post ID의 좋아요 '카운트'를 Map<postId, count>로 반환
    @Query("SELECT pl.id.postId as postId, COUNT(pl.id.postId) as likeCount " +
            "FROM PostLike pl WHERE pl.id.postId IN :postIds GROUP BY pl.id.postId")
    List<Map<String, Object>> findLikeCountsByPostIds(@Param("postIds") Collection<Long> postIds);

    // [N+1 해결] 여러 post ID 중 '내가' 좋아요 누른 post ID의 Set만 반환
    @Query("SELECT pl.id.postId FROM PostLike pl WHERE pl.id.userId = :userId AND pl.id.postId IN :postIds")
    Set<Long> findLikedPostIdsByUserId(@Param("userId") Long userId, @Param("postIds") Collection<Long> postIds);
}
