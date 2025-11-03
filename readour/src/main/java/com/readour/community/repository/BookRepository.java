package com.readour.community.repository;

import com.readour.common.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    /**
     * ISBN(13자리)으로 도서를 조회합니다.
     * 외부 API 호출 전, DB에 이미 책이 저장되어 있는지 확인하기 위해 사용됩니다.
     */
    Optional<Book> findByIsbn13(String isbn13);

    /**
     * [신규]
     * DB에 저장된 도서 중, 도서명(bookname)에 키워드가 포함된 도서를 검색합니다.
     */
    Page<Book> findByBooknameContaining(String title, Pageable pageable);
}

