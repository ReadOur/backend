package com.readour.community.service;

import com.readour.common.dto.FileResponseDto;
import com.readour.community.entity.*;
import com.readour.common.entity.FileAsset;
import com.readour.common.entity.User;
import com.readour.common.exception.CustomException;
import com.readour.common.enums.ErrorCode;
import com.readour.common.service.FileAssetService;
import com.readour.community.enums.RecruitmentStatus;
import com.readour.community.repository.*;
import com.readour.common.repository.UserRepository;
import com.readour.community.dto.*;
import com.readour.community.enums.PostCategory;
import com.readour.community.enums.PostSearchType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Set;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommunityService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final PostLikeRepository postLikeRepository;
    private final RecruitmentRepository recruitmentRepository;
    private final RecruitmentMemberRepository recruitmentMemberRepository;

    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final PostSpecification postSpecification;
    private final FileAssetService fileAssetService;

    @Transactional(readOnly = true) // Use readOnly for read operations
    public Page<PostSummaryDto> getPostList(Pageable pageable, Long currentUserId, PostCategory category) {
        log.debug("getPostList called. Category: {}", category);
        Set<Long> appliedRecruitmentIds = (currentUserId != null) ?
                recruitmentMemberRepository.findAllRecruitmentIdsByUserId(currentUserId) : Collections.emptySet();

        Page<Post> postPage;
        if (category != null) {
            postPage = postRepository.findAllByCategoryAndIsDeletedFalse(category, pageable);
        } else {
            postPage = postRepository.findAllByIsDeletedFalse(pageable);
        }

        return postPage.map(post -> convertToPostSummaryDto(post, currentUserId, appliedRecruitmentIds));
    }

    // bookId로 게시글 목록 조회
    @Transactional(readOnly = true)
    public Page<PostSummaryDto> getPostListByBookId(Long bookId, Long currentUserId, Pageable pageable) {
        log.debug("getPostListByBookId called. bookId: {}", bookId);
        // (N+1 방지용)
        Set<Long> appliedRecruitmentIds = (currentUserId != null) ?
                recruitmentMemberRepository.findAllRecruitmentIdsByUserId(currentUserId) : Collections.emptySet();

        Page<Post> postPage = postRepository.findAllByBookBookIdAndIsDeletedFalse(bookId, pageable);

        return postPage.map(post -> convertToPostSummaryDto(post, currentUserId, appliedRecruitmentIds));
    }

    @Transactional(readOnly = true)
    public Page<PostSummaryDto> searchPosts(PostSearchType searchType, String keyword, PostCategory category, Pageable pageable, Long currentUserId) {
        // (N+1 방지용)
        Set<Long> appliedRecruitmentIds = (currentUserId != null) ?
                recruitmentMemberRepository.findAllRecruitmentIdsByUserId(currentUserId) : Collections.emptySet();

        Specification<Post> spec = postSpecification.search(searchType, keyword, category);
        Page<Post> postPage = postRepository.findAll(spec, pageable); // (Fetch Join은 Spec과 함께 쓰기 까다로우므로 N+1 발생 가능성 있음)

        return postPage.map(post -> convertToPostSummaryDto(post, currentUserId, appliedRecruitmentIds));
    }

    @Transactional
    public PostResponseDto createPost(PostCreateRequestDto requestDto, Long userId) {
        log.debug("createPost service called by userId: {}. DTO: {}", userId, requestDto);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "User not found with id: " + userId));

        Book book = null;
        if (requestDto.getBookId() != null) {
            book = bookRepository.findById(requestDto.getBookId())
                    .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "Book not found with id: " + requestDto.getBookId()));
        } // TODO: 없으면 도서 검색 api 날려서 찾아보고도 없으면 404 날리기. 있으면 db 추가.

        Recruitment recruitment = null;
        if (requestDto.getCategory() == PostCategory.GROUP) {
            if (requestDto.getRecruitmentLimit() == null || requestDto.getRecruitmentLimit() < 2) {
                throw new CustomException(ErrorCode.BAD_REQUEST, "모집 인원은 최소 2명 이상이어야 합니다.");
            }
            if (requestDto.getChatRoomName() == null || requestDto.getChatRoomName().isBlank()) {
                throw new CustomException(ErrorCode.BAD_REQUEST, "모임(GROUP) 생성 시 채팅방 이름은 필수입니다.");
            }
            recruitment = Recruitment.builder()
                    .recruitmentLimit(requestDto.getRecruitmentLimit())
                    .chatRoomName(requestDto.getChatRoomName())
                    .chatRoomDescription(requestDto.getChatRoomDescription())
                    .currentMemberCount(1)
                    .status(RecruitmentStatus.RECRUITING)
                    .build();
        }

        Post post = Post.builder()
                .user(user)
                .book(book)
                .title(requestDto.getTitle())
                .content(requestDto.getContent())
                .category(requestDto.getCategory())
                .isSpoiler(requestDto.getIsSpoiler() != null ? requestDto.getIsSpoiler() : false)
                .hit(0)
                .isDeleted(false)
                .isHidden(false)
                .recruitment(recruitment)
                .build();

        if (recruitment != null) {
            recruitment.setPost(post);
        }

        Post savedPost = postRepository.save(post);
        log.debug("Post saved with id: {}", savedPost.getPostId());

        if (recruitment != null) {
            RecruitmentMember selfMember = RecruitmentMember.builder()
                    .id(new RecruitmentMemberId(recruitment.getRecruitmentId(), userId))
                    .recruitment(recruitment)
                    .user(user)
                    .build();
            recruitmentMemberRepository.save(selfMember);
            log.info("Recruitment post created. postId: {}, recruitmentId: {}", savedPost.getPostId(), recruitment.getRecruitmentId());
        }

        if (requestDto.getWarnings() != null && !requestDto.getWarnings().isEmpty()) {
            log.debug("Adding {} warnings", requestDto.getWarnings().size());
            requestDto.getWarnings().forEach(savedPost::addWarning);
            savedPost = postRepository.save(savedPost);
        }

        fileAssetService.replaceLinks("POST", savedPost.getPostId(), requestDto.getAttachmentIds());
        List<FileResponseDto> attachments = mapToResponses(fileAssetService.getLinkedAssets("POST", savedPost.getPostId()));

        return getPostDetail(savedPost.getPostId(), userId);
    }

    @Transactional
    public PostResponseDto getPostDetail(Long postId, Long currentUserId) {
        log.debug("getPostDetail service called for postId: {}", postId);

        Post post = postRepository.findByPostIdAndIsDeletedFalse(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "Post not found with id: " + postId));

        // postRepository.incrementHit(postId);

        List<CommentResponseDto> comments = commentRepository.findAllByPostId(postId)
                .stream()
                .filter(comment -> !comment.getIsDeleted())
                .map(CommentResponseDto::fromEntity)
                .collect(Collectors.toList());

        Long likeCount = postLikeRepository.countByIdPostId(postId);
        Long commentCount = commentRepository.countByPostIdAndIsDeletedFalse(postId);
        Boolean isLiked = (currentUserId == null) ? false : postLikeRepository.existsByIdPostIdAndIdUserId(postId, currentUserId);

        List<FileResponseDto> attachments = mapToResponses(fileAssetService.getLinkedAssets("POST", postId));

        boolean isApplied = false;
        if (post.getRecruitment() != null && currentUserId != null) {
            isApplied = recruitmentMemberRepository.existsById(
                    new RecruitmentMemberId(post.getRecruitment().getRecruitmentId(), currentUserId)
            );
        }
        RecruitmentDetailsDto recruitmentDetails = RecruitmentDetailsDto.fromEntity(post.getRecruitment(), isApplied);

        return PostResponseDto.fromEntity(post, comments, likeCount, commentCount, isLiked, attachments, recruitmentDetails);
    }

    @Transactional
    public void incrementPostHit(Long postId) {
        if (postRepository.findByPostIdAndIsDeletedFalse(postId).isPresent()) {
            postRepository.incrementHit(postId);
            log.debug("Post hit incremented for postId: {}", postId);
        } else {
            log.warn("Attempted to increment hit for non-existent or deleted post: {}", postId);
        }
    }

    @Transactional
    public boolean toggleLike(Long postId, Long userId) {
        if (!postRepository.existsById(postId)) {
            throw new CustomException(ErrorCode.NOT_FOUND, "Post not found with id: " + postId);
        }

        PostLikeId likeId = new PostLikeId(postId, userId);
        Optional<PostLike> existingLike = postLikeRepository.findById(likeId);

        if (existingLike.isPresent()) {
            postLikeRepository.deleteById(likeId);
            log.debug("Like removed for postId={}, userId={}", postId, userId);
            return false;
        } else {
            PostLike newLike = PostLike.builder().id(likeId).build();
            postLikeRepository.save(newLike);
            log.debug("Like added for postId={}, userId={}", postId, userId);
            return true;
        }
    }

    @Transactional
    public PostResponseDto updatePost(Long postId, PostUpdateRequestDto requestDto, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "Post not found with id: " + postId));

        if (!post.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN, "User does not have permission to update this post");
        }

        if (requestDto.getCategory() != PostCategory.GROUP && post.getCategory() == PostCategory.GROUP) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "모임 카테고리에서 다른 카테고리로 수정할 수 없습니다.");
        }

        if (requestDto.getCategory() == PostCategory.GROUP && post.getCategory() != PostCategory.GROUP) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "다른 카테고리에서 모임 카테고리로 수정할 수 없습니다.");
        }

        if (requestDto.getTitle() != null) {
            post.setTitle(requestDto.getTitle());
        }
        if (requestDto.getContent() != null) {
            post.setContent(requestDto.getContent());
        }
        if (requestDto.getCategory() != null) {
            post.setCategory(requestDto.getCategory());
        }
        if (requestDto.getIsSpoiler() != null) {
            post.setIsSpoiler(requestDto.getIsSpoiler());
        }
        if (requestDto.getBookId() != null) {
            Book book = bookRepository.findById(requestDto.getBookId())
                    .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "Book not found with id: " + requestDto.getBookId()));
            post.setBook(book);
        } // TODO: 없으면 도서 검색 api 날려서 찾아보고도 없으면 404 날리기. 있으면 db 추가.

        fileAssetService.replaceLinks("POST", postId, requestDto.getAttachmentIds());

        Post updatedPost = postRepository.save(post);

        List<CommentResponseDto> comments = commentRepository.findAllByPostId(postId)
                .stream()
                .filter(comment -> !comment.getIsDeleted())
                .map(CommentResponseDto::fromEntity)
                .collect(Collectors.toList());
        Long likeCount = postLikeRepository.countByIdPostId(postId);
        Long commentCount = commentRepository.countByPostIdAndIsDeletedFalse(postId);
        Boolean isLiked = postLikeRepository.existsByIdPostIdAndIdUserId(postId, userId);

        if (post.getCategory() == PostCategory.GROUP) {
            Recruitment recruitment = post.getRecruitment();
            if (recruitment == null) {
                throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "모임 게시글의 모집 정보가 누락되었습니다.");
            }
            if (recruitment.getStatus() != RecruitmentStatus.RECRUITING) {
                throw new CustomException(ErrorCode.FORBIDDEN, "이미 모집이 시작/완료된 모임의 정보는 변경할 수 없습니다.");
            }

            if (requestDto.getRecruitmentLimit() != null) {
                if (requestDto.getRecruitmentLimit() < recruitment.getCurrentMemberCount()) {
                    throw new CustomException(ErrorCode.BAD_REQUEST, "모집 인원을 현재 인원보다 적게 설정할 수 없습니다.");
                }
                recruitment.setRecruitmentLimit(requestDto.getRecruitmentLimit());
            }
            if (requestDto.getChatRoomName() != null) {
                recruitment.setChatRoomName(requestDto.getChatRoomName());
            }
            if (requestDto.getChatRoomDescription() != null) {
                recruitment.setChatRoomDescription(requestDto.getChatRoomDescription());
            }
        }

        List<FileResponseDto> attachments = mapToResponses(fileAssetService.getLinkedAssets("POST", postId));

        return getPostDetail(updatedPost.getPostId(), userId);
    }

    private List<FileResponseDto> mapToResponses(List<FileAsset> assets) {
        return assets.stream()
                .map(fileAssetService::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deletePost(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "Post not found with id: " + postId));

        if (!post.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN, "User does not have permission to delete this post");
        }

        if (post.getCategory() == PostCategory.GROUP && post.getRecruitment() != null) {
            Recruitment recruitment = post.getRecruitment();
            if (recruitment.getStatus() == RecruitmentStatus.RECRUITING) {
                recruitment.setStatus(RecruitmentStatus.CANCELLED);
                recruitmentRepository.save(recruitment);
                log.debug("Recruitment cancelled for postId: {}", postId);
            }
        }

        post.updateStatus(true);
        postRepository.save(post);
        log.debug("Post soft-deleted with id: {}", postId);

        List<Comment> comments = commentRepository.findAllByPostId(postId);
        comments.forEach(comment -> comment.updateStatus(true));

        if (!comments.isEmpty()) {
            commentRepository.saveAll(comments);
            log.debug("Soft-deleted {} associated comments for post id: {}", comments.size(), postId);
        }
    }

    @Transactional
    public CommentResponseDto addComment(Long postId, CommentCreateRequestDto requestDto, Long userId) {
        if (!postRepository.existsById(postId)) {
            throw new CustomException(ErrorCode.NOT_FOUND, "Post not found with id: " + postId);        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "User not found with id: " + userId));

        Comment comment = Comment.builder()
                .postId(postId)
                .user(user)
                .content(requestDto.getContent())
                .isDeleted(false)
                .isHidden(false)
                .build();

        Comment savedComment = commentRepository.save(comment);

        return CommentResponseDto.fromEntity(savedComment);
    }

    @Transactional
    public CommentResponseDto updateComment(Long commentId, CommentUpdateRequestDto requestDto, Long userId) {
        Comment comment = commentRepository.findByCommentIdAndIsDeletedFalse(commentId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "Comment not found with id: " + commentId));

        if (!comment.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN, "User does not have permission to update this comment");
        }

        comment.updateContent(requestDto.getContent());
        Comment updatedComment = commentRepository.save(comment);

        return CommentResponseDto.fromEntity(updatedComment);
    }

    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findByCommentIdAndIsDeletedFalse(commentId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "Comment not found with id: " + commentId));

        if (!comment.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN, "User does not have permission to delete this comment");
        }

        comment.updateStatus(true);
        commentRepository.save(comment);
    }

    // [Helper] Post -> PostSummaryDto 변환
    private PostSummaryDto convertToPostSummaryDto(Post post, Long currentUserId, Set<Long> appliedRecruitmentIds) {
        Long likeCount = postLikeRepository.countByIdPostId(post.getPostId());
        Long commentCount = commentRepository.countByPostIdAndIsDeletedFalse(post.getPostId());
        Boolean isLiked = (currentUserId == null) ? false : postLikeRepository.existsByIdPostIdAndIdUserId(post.getPostId(), currentUserId);

        Boolean isApplied = false;
        if (post.getRecruitment() != null && currentUserId != null) {
            isApplied = appliedRecruitmentIds.contains(post.getRecruitment().getRecruitmentId());
        }

        return PostSummaryDto.fromEntity(post, likeCount, commentCount, isLiked, isApplied);
    }
}
