package com.readour.community.service;

import com.readour.community.dto.*;
import com.readour.community.entity.*;
import com.readour.community.exception.ResourceNotFoundException;
import com.readour.community.repository.CommentRepository;
import com.readour.community.repository.PostLikeRepository;
import com.readour.community.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommunityService {
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final PostLikeRepository postLikeRepository;

    @Transactional(readOnly = true) // Use readOnly for read operations
    public Page<PostSummaryDto> getPostList(Pageable pageable) {
        Page<Post> postPage = postRepository.findAllByIsDeletedFalse(pageable);
        List<Post> posts = postPage.getContent();
        List<PostSummaryDto> summaryDtos = posts.stream()
                .map(post -> {
                    Long likeCount = postLikeRepository.countByIdPostId(post.getPostId());
                    Long commentCount = commentRepository.countByPostIdAndIsDeletedFalse(post.getPostId());
                    return PostSummaryDto.fromEntity(post, likeCount, commentCount);
                })
                .collect(Collectors.toList());

        // Convert Page<Post> to Page<PostSummaryDto>
        return new PageImpl<>(summaryDtos, postPage.getPageable(), postPage.getTotalElements());
    }

    @Transactional
    public PostResponseDto createPost(PostCreateRequestDto requestDto, Long userId) {
        Post post = new Post();
        post.setTitle(requestDto.getTitle());
        post.setContent(requestDto.getContent());
        post.setCategory(requestDto.getCategory());
        post.setBookId(requestDto.getBookId());
        post.setIsSpoiler(requestDto.getIsSpoiler());
        post.setUserId(userId);
        post.setHit(0);
        post.setIsDeleted(false);

        Post savedPost = postRepository.save(post);
        return PostResponseDto.fromEntity(savedPost, List.of(), 0L, 0L);
    }

    @Transactional
    public PostResponseDto getPostDetail(Long postId) {
        postRepository.incrementHit(postId);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));

        List<CommentResponseDto> comments = commentRepository.findAllByPostId(postId)
                .stream()
                .filter(comment -> !comment.getIsDeleted())
                .map(CommentResponseDto::fromEntity)
                .collect(Collectors.toList());

        Long likeCount = postLikeRepository.countByIdPostId(postId);
        Long commentCount = commentRepository.countByPostIdAndIsDeletedFalse(postId);

        return PostResponseDto.fromEntity(post, comments, likeCount, commentCount);
    }

    @Transactional
    public boolean toggleLike(Long postId, Long userId) {
        if (!postRepository.existsById(postId)) {
            throw new ResourceNotFoundException("Post not found with id: " + postId);
        }
        PostLikeId likeId = new PostLikeId(postId, userId);
        Optional<PostLike> existingLike = postLikeRepository.findById(likeId);

        if (existingLike.isPresent()) {
            postLikeRepository.deleteById(likeId);
            return false;
        } else {
            PostLike newLike = PostLike.builder().id(likeId).build();
            postLikeRepository.save(newLike);
            return true;
        }
    }

    @Transactional
    public PostResponseDto updatePost(Long postId, PostUpdateRequestDto requestDto, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));

        if (!post.getUserId().equals(userId)) {
            throw new RuntimeException("User does not have permission to update this post");
        }
        if (requestDto.getTitle() != null) {
            post.setTitle(requestDto.getTitle());
        }
        if (requestDto.getContent() != null) {
            post.setContent(requestDto.getContent());
        }
        if (requestDto.getIsSpoiler() != null) {
            post.setIsSpoiler(requestDto.getIsSpoiler());
        }
        if (requestDto.getCategory() != null) {
            post.setCategory(requestDto.getCategory());
        }

        post.update(requestDto);
        Post updatedPost = postRepository.save(post);

        List<CommentResponseDto> comments = commentRepository.findAllByPostId(postId)
                .stream()
                .filter(comment -> !comment.getIsDeleted())
                .map(CommentResponseDto::fromEntity)
                .collect(Collectors.toList());

        Long likeCount = postLikeRepository.countByIdPostId(postId);
        Long commentCount = commentRepository.countByPostIdAndIsDeletedFalse(postId);

        return PostResponseDto.fromEntity(updatedPost, comments, likeCount, commentCount);
    }

    @Transactional
    public void deletePost(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));

        if (!post.getUserId().equals(userId)) {
            throw new RuntimeException("User does not have permission to delete this post");
        }

        post.updateStatus(true);
        postRepository.save(post);
    }

    @Transactional
    public CommentResponseDto addComment(Long postId, CommentCreateRequestDto requestDto, Long userId) {
        if (!postRepository.existsById(postId)) {
            throw new ResourceNotFoundException("Post not found with id: " + postId);
        }

        Comment comment = new Comment();
        comment.setPostId(postId);
        comment.setUserId(userId);
        comment.setContent(requestDto.getContent());
        comment.setIsDeleted(false);

        Comment savedComment = commentRepository.save(comment);

        return CommentResponseDto.fromEntity(savedComment);
    }

    @Transactional
    public CommentResponseDto updateComment(Long commentId, CommentUpdateRequestDto requestDto, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + commentId));

        if (!comment.getUserId().equals(userId)) {
            throw new RuntimeException("User does not have permission to update this comment");
        }

        comment.updateContent(requestDto.getContent());
        Comment updatedComment = commentRepository.save(comment);

        return CommentResponseDto.fromEntity(updatedComment);
    }

    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + commentId));

        if (!comment.getUserId().equals(userId)) {
            throw new RuntimeException("User does not have permission to delete this comment");
        }

        comment.updateStatus(true);
        commentRepository.save(comment);
    }

    // --- (Helper Methods) ---
    private PostResponseDto convertToPostResponseDto(Post post) {
        PostResponseDto dto = new PostResponseDto();
        dto.setPostId(post.getPostId());
        dto.setTitle(post.getTitle());
        dto.setContent(post.getContent());
        dto.setCategory(post.getCategory());
        dto.setAuthorId(post.getUserId());
        // TODO: post.getUserId()로 User를 조회해서 닉네임(authorNickname) 설정
        // dto.setAuthorNickname(userService.getUserNickname(post.getUserId()));
        dto.setHit(post.getHit());
        // ...
        return dto;
    }

    private CommentResponseDto convertToCommentResponseDto(Comment comment) {
        CommentResponseDto dto = new CommentResponseDto();
        dto.setCommentId(comment.getCommentId());
        dto.setContent(comment.getContent());
        dto.setAuthorId(comment.getUserId());
        // TODO: comment.getUserId()로 User를 조회해서 닉네임(authorNickname) 설정
        // ...
        return dto;
    }
}