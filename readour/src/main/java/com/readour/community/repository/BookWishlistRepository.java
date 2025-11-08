package com.readour.community.repository;

import com.readour.community.entity.BookWishlist;
import com.readour.community.entity.BookWishlistId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookWishlistRepository extends JpaRepository<BookWishlist, BookWishlistId> {

    /**
     * 도서 상세 조회 시, 사용자가 이 책을 위시리스트에 담았는지 확인합니다.
     */
    boolean existsByIdUserIdAndIdBookId(Long userId, Long bookId);
}