/*
package com.readour.community.service;

import com.readour.common.security.UserPrincipal;
import com.readour.community.dto.MainPageResponseDto;
import com.readour.community.dto.PopularBookDto;
import com.readour.community.dto.PostSummaryDto;
import com.readour.community.entity.Post;
import com.readour.community.enums.PostCategory;
import com.readour.community.enums.RecruitmentStatus;
import com.readour.community.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MainPageService {

    private final BookService bookService;
    private final CommunityService communityService; // PostSummaryDto 변환 헬퍼용
    private final PostRepository postRepository;

    private static final int MAX_LIST_SIZE = 10;
    private static final Sort DESC_BY_CREATED = Sort.by(Sort.Direction.DESC, "createdAt");

    public MainPageResponseDto getMainPageData(UserPrincipal currentUser) {

        Long currentUserId = (currentUser != null) ? currentUser.getId() : null;

        // 1. 주간 인기 게시글 (좋아요 순, 10개)
        Pageable popularPostPageable = PageRequest.of(0, MAX_LIST_SIZE); // (정렬은 @Query에서 처리)
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusWeeks(1);

        Page<Post> popularPostPage = postRepository.findPopularPostsSince(oneWeekAgo, popularPostPageable);
        List<PostSummaryDto> popularPosts = communityService.convertToPostSummaryPage(popularPostPage, currentUserId)
                .getContent();

        // 2. 모집 게시글 (최신순, 10개)
        Pageable recruitmentPageable = PageRequest.of(0, MAX_LIST_SIZE); // (정렬은 @Query에서 처리)

        Page<Post> recruitmentPostPage = postRepository.findRecruitmentPosts(
                PostCategory.GROUP,
                RecruitmentStatus.RECRUITING,
                recruitmentPageable
        );
        List<PostSummaryDto> recruitmentPosts = communityService.convertToPostSummaryPage(recruitmentPostPage, currentUserId)
                .getContent();

        // 3. 인기 도서 (회원 맞춤형, 10개)
        Pageable popularBookPageable = PageRequest.of(0, MAX_LIST_SIZE);

        Page<PopularBookDto> popularBooks = bookService.getPopularBooks(currentUser, popularBookPageable);

        // 4. DTO로 조립
        return MainPageResponseDto.builder()
                .popularPosts(popularPosts)
                .recruitmentPosts(recruitmentPosts)
                .popularBooks(popularBooks)
                .build();
    }

}*/