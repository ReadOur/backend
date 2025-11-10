package com.readour.community.repository;

import com.readour.community.entity.BookHighlight;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookHighlightRepository extends JpaRepository<BookHighlight, Long> {

    Page<BookHighlight> findAllByBookId(Long bookId, Pageable pageable);

    Optional<BookHighlight> findByHighlightIdAndUserId(Long highlightId, Long userId);

    Page<BookHighlight> findAllByUserId(Long userId, Pageable pageable);
}