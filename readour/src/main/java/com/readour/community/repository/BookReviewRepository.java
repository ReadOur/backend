package com.readour.community.repository;

import com.readour.community.entity.BookReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookReviewRepository extends JpaRepository<BookReview, Long> {

    /**
     * (SD-27) 책 리뷰 작성:
     * 사용자가 특정 책에 대해 이미 리뷰를 작성했는지 확인 (uq_book_user 제약조건)
     */
    Optional<BookReview> findByBookIdAndUserId(Long bookId, Long userId);

    /**
     * (SD-27) 책 리뷰 조회:
     * 특정 책에 달린 모든 리뷰를 페이징하여 조회
     */
    Page<BookReview> findAllByBookId(Long bookId, Pageable pageable);

    /**
     * (SD-28, SD-29) 책 리뷰 수정/삭제:
     * 특정 리뷰가 해당 사용자의 소유인지 확인
     */
    Optional<BookReview> findByReviewIdAndUserId(Long reviewId, Long userId);
}