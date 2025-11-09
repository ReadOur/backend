package com.readour.community.repository;

import com.readour.community.entity.BookWishlist;
import com.readour.community.entity.BookWishlistId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookWishlistRepository extends JpaRepository<BookWishlist, BookWishlistId> {
    boolean existsByIdUserIdAndIdBookId(Long userId, Long bookId);
    Page<BookWishlist> findAllById_UserId(Long userId, Pageable pageable);
}