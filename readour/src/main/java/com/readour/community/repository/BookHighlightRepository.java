package com.readour.community.repository;

import com.readour.community.entity.BookHighlight;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookHighlightRepository extends JpaRepository<BookHighlight, Long> {

    /**
     * (SD-31) 책 하이라이트 조회:
     * 특정 책에 대한 모든 하이라이트를 페이징하여 조회
     */
    Page<BookHighlight> findAllByBookId(Long bookId, Pageable pageable);

    /**
     * (SD-32, SD-33) 책 하이라이트 수정/삭제:
     * 특정 하이라이트가 해당 사용자의 소유인지 확인
     */
    Optional<BookHighlight> findByHighlightIdAndUserId(Long highlightId, Long userId);
}
