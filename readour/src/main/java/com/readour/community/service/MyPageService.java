/*
package com.readour.community.service;


import com.readour.common.entity.User;
import com.readour.common.enums.ErrorCode;
import com.readour.common.exception.CustomException;
import com.readour.common.repository.UserRepository;
import com.readour.community.dto.*;
import com.readour.community.entity.Comment;
import com.readour.community.entity.Post;
import com.readour.community.entity.PostLike;
import com.readour.community.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPageService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final PostLikeRepository postLikeRepository;
    private final RecruitmentMemberRepository recruitmentMemberRepository;
    private final CommunityService communityService;

    private static final int PREVIEW_SIZE = 5;
    private static final Sort DESC_BY_CREATED = Sort.by(Sort.Direction.DESC, "createdAt");

    /**
     * 마이페이지 미리보기 데이터 조회
     *//*
    public MyPageResponseDto getMyPageData(Long userId) {
        User user = validateAndGetUser(userId);

        // 1. 최신 5개씩 조회 (Pageable 생성)
        Pageable previewPageable = PageRequest.of(0, PREVIEW_SIZE, DESC_BY_CREATED);

        // 2. 각 페이징 메서드를 호출 (이 메서드들은 이제 N+1을 처리함)
        List<PostSummaryDto> myPosts = getMyPosts(userId, previewPageable).getPostPage().getContent();
        List<MyCommentDto> myComments = getMyComments(userId, previewPageable).getCommentPage().getContent();
        List<PostSummaryDto> likedPosts = getLikedPosts(userId, previewPageable).getLikedPostsPage().getContent();

        return MyPageResponseDto.from(user, myPosts, myComments, likedPosts);
    }

    /**
     * 내가 쓴 게시글 페이징 조회
     *//*
    public MyPagePostsPageDto getMyPosts(Long userId, Pageable pageable) {
        User user = validateAndGetUser(userId);
        Page<Post> postPage = postRepository.findByUserIdAndIsDeletedFalse(userId, pageable);

        // Post -> PostSummaryDto 변환
        Page<PostSummaryDto> postDtoPage = communityService.convertToPostSummaryPage(postPage, userId);

        return MyPagePostsPageDto.from(user, postDtoPage);
    }

    /**
     * 내가 쓴 댓글 페이징 조회
     *//*
    public MyPageCommentsPageDto getMyComments(Long userId, Pageable pageable) {
        User user = validateAndGetUser(userId);
        // 1. 내 댓글 조회
        Page<Comment> commentPage = commentRepository.findByUserIdAndIsDeletedFalse(userId, pageable);

        // 2. N+1 방지: 댓글의 원본 Post 정보 조회
        Set<Long> postIds = commentPage.getContent().stream().map(Comment::getPostId).collect(Collectors.toSet());
        Map<Long, Post> postMap = postRepository.findAllById(postIds).stream()
                .collect(Collectors.toMap(Post::getPostId, Function.identity()));

        // 3. DTO 변환
        Page<MyCommentDto> commentDtoPage = commentPage.map(comment -> MyCommentDto.fromEntities(comment, postMap.get(comment.getPostId())));

        return MyPageCommentsPageDto.from(user, commentDtoPage);
    }

    /**
     * 내가 좋아요 누른 글 페이징 조회
     */
/*
    public MyPageLikedPostsPageDto getLikedPosts(Long userId, Pageable pageable) {
        User user = validateAndGetUser(userId);
        // 1. 내가 누른 '좋아요'를 페이징
        Page<PostLike> likePage = postLikeRepository.findAllByIdUserId(userId, pageable);

        // 2. '좋아요'에서 Post ID 목록 추출
        List<Long> postIds = likePage.getContent().stream()
                .map(like -> like.getId().getPostId())
                .toList();

        if (postIds.isEmpty()) {
            return MyPageLikedPostsPageDto.from(user, Page.empty(pageable)); // 👈 빈 페이지 반환
        }

        // 3. Post ID 목록으로 실제 Post 정보 조회
        Map<Long, Post> postMap = postRepository.findAllById(postIds).stream()
                .collect(Collectors.toMap(Post::getPostId, Function.identity()));

        Set<Long> appliedRecruitmentIds = (userId != null)
                ? recruitmentMemberRepository.findAllRecruitmentIdsByUserId(userId)
                : Collections.emptySet();

        // 4. Post -> PostSummaryDto 변환 (likePage의 순서대로)
        List<PostSummaryDto> dtoList = postIds.stream()
                .map(postMap::get)
                .filter(Objects::nonNull)
                .map(post -> communityService.convertPostToPostSummaryDto(post, userId, appliedRecruitmentIds))
                .toList();

        Page<PostSummaryDto> likedPostDtoPage = new PageImpl<>(dtoList, pageable, likePage.getTotalElements());

        return MyPageLikedPostsPageDto.from(user, likedPostDtoPage);
    }

    // [Helper] 사용자 검증
    private User validateAndGetUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "User not found with id: " + userId));
    }
}*/