package com.readour.community.service;

import com.readour.common.entity.Book;
import com.readour.common.entity.User;
import com.readour.common.exception.CustomException;
import com.readour.common.enums.ErrorCode;
import com.readour.common.repository.BookRepository;
import com.readour.common.repository.UserRepository;
import com.readour.community.dto.*;
import com.readour.community.entity.Comment;
import com.readour.community.entity.Post;
import com.readour.community.entity.PostLike;
import com.readour.community.entity.PostLikeId;
import com.readour.community.enums.PostCategory;
import com.readour.community.enums.PostSearchType;
import com.readour.community.repository.CommentRepository;
import com.readour.community.repository.PostLikeRepository;
import com.readour.community.repository.PostRepository;
import com.readour.community.service.PostSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final PostSpecification postSpecification;

    @Transactional(readOnly = true) // Use readOnly for read operations
    public Page<PostSummaryDto> getPostList(Pageable pageable, Long currentUserId, PostCategory category) {
        log.debug("getPostList called. Category: {}", category);
        Page<Post> postPage;
        if (category != null) {
            postPage = postRepository.findAllByCategoryAndIsDeletedFalse(category, pageable);
        } else {
            postPage = postRepository.findAllByIsDeletedFalse(pageable);
        }

        List<Post> posts = postPage.getContent();
        List<PostSummaryDto> summaryDtos = posts.stream()
                .map(post -> {
                    Long likeCount = postLikeRepository.countByIdPostId(post.getPostId());
                    Long commentCount = commentRepository.countByPostIdAndIsDeletedFalse(post.getPostId());
                    Boolean isLiked = (currentUserId == null) ? false : postLikeRepository.existsByIdPostIdAndIdUserId(post.getPostId(), currentUserId);
                    return PostSummaryDto.fromEntity(post, likeCount, commentCount, isLiked);
                })
                .collect(Collectors.toList());

        // Convert Page<Post> to Page<PostSummaryDto>
        return new PageImpl<>(summaryDtos, postPage.getPageable(), postPage.getTotalElements());
    }

    @Transactional(readOnly = true)
    public Page<PostSummaryDto> searchPosts(PostSearchType searchType, String keyword, Pageable pageable, Long currentUserId) {
        log.debug("searchPosts called. Type: {}, Keyword: {}", searchType, keyword);
        Specification<Post> spec = postSpecification.search(searchType, keyword);
        Page<Post> postPage = postRepository.findAll(spec, pageable);

        return postPage.map(post -> {
            Long likeCount = postLikeRepository.countByIdPostId(post.getPostId());
            Long commentCount = commentRepository.countByPostIdAndIsDeletedFalse(post.getPostId());
            Boolean isLiked = (currentUserId == null) ? false : postLikeRepository.existsByIdPostIdAndIdUserId(post.getPostId(), currentUserId);
            return PostSummaryDto.fromEntity(post, likeCount, commentCount, isLiked);
        });
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
                .build();

        Post savedPost = postRepository.save(post);
        log.debug("Post saved with id: {}", savedPost.getPostId());

        if (requestDto.getWarnings() != null && !requestDto.getWarnings().isEmpty()) {
            log.debug("Adding {} warnings", requestDto.getWarnings().size());
            requestDto.getWarnings().forEach(savedPost::addWarning);
            savedPost = postRepository.save(savedPost);
        }

        return PostResponseDto.fromEntity(savedPost, List.of(), 0L, 0L, false);
    }

    @Transactional
    public PostResponseDto getPostDetail(Long postId, Long currentUserId) {
        log.debug("getPostDetail service called for postId: {}", postId);

        Post post = postRepository.findByPostIdAndIsDeletedFalse(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "Post not found with id: " + postId));

        postRepository.incrementHit(postId);

        List<CommentResponseDto> comments = commentRepository.findAllByPostId(postId)
                .stream()
                .filter(comment -> !comment.getIsDeleted())
                .map(CommentResponseDto::fromEntity)
                .collect(Collectors.toList());

        Long likeCount = postLikeRepository.countByIdPostId(postId);
        Long commentCount = commentRepository.countByPostIdAndIsDeletedFalse(postId);
        Boolean isLiked = (currentUserId == null) ? false : postLikeRepository.existsByIdPostIdAndIdUserId(postId, currentUserId);

        return PostResponseDto.fromEntity(post, comments, likeCount, commentCount, isLiked);
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

        Post updatedPost = postRepository.save(post);

        List<CommentResponseDto> comments = commentRepository.findAllByPostId(postId)
                .stream()
                .filter(comment -> !comment.getIsDeleted())
                .map(CommentResponseDto::fromEntity)
                .collect(Collectors.toList());
        Long likeCount = postLikeRepository.countByIdPostId(postId);
        Long commentCount = commentRepository.countByPostIdAndIsDeletedFalse(postId);
        Boolean isLiked = postLikeRepository.existsByIdPostIdAndIdUserId(postId, userId);

        return PostResponseDto.fromEntity(updatedPost, comments, likeCount, commentCount, isLiked);
    }

    @Transactional
    public void deletePost(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "Post not found with id: " + postId));

        if (!post.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN, "User does not have permission to delete this post");
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

        Comment comment = new Comment();
        comment.setPostId(postId);
        comment.setUserId(user.getId());
        comment.setContent(requestDto.getContent());
        comment.setIsDeleted(false);
        comment.setIsHidden(false);

        Comment savedComment = commentRepository.save(comment);

        return CommentResponseDto.fromEntity(savedComment);
    }

    @Transactional
    public CommentResponseDto updateComment(Long commentId, CommentUpdateRequestDto requestDto, Long userId) {
        Comment comment = commentRepository.findByCommentIdAndIsDeletedFalse(commentId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "Comment not found with id: " + commentId));

        if (!comment.getUserId().equals(userId)) {
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

        if (!comment.getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN, "User does not have permission to delete this comment");
        }

        comment.updateStatus(true);
        commentRepository.save(comment);
    }
}