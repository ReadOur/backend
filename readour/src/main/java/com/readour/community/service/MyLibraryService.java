package com.readour.community.service;

import com.readour.common.enums.ErrorCode;
import com.readour.common.exception.CustomException;
import com.readour.community.dto.MyHighlightDto;
import com.readour.community.dto.MyLibraryResponseDto;
import com.readour.community.dto.MyReviewDto;
import com.readour.community.dto.MyWishlistDto;
import com.readour.community.entity.Book;
import com.readour.community.entity.BookHighlight;
import com.readour.community.entity.BookReview;
import com.readour.community.entity.BookWishlist;
import com.readour.community.repository.BookHighlightRepository;
import com.readour.community.repository.BookRepository;
import com.readour.community.repository.BookReviewRepository;
import com.readour.community.repository.BookWishlistRepository;
import com.readour.community.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyLibraryService {

    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final BookWishlistRepository bookWishlistRepository;
    private final BookReviewRepository bookReviewRepository;
    private final BookHighlightRepository bookHighlightRepository;

    private static final int PREVIEW_SIZE = 5;

    public MyLibraryResponseDto getMyLibraryData(Long userId) {
        // 1. 사용자 존재 여부 확인
        if (!userRepository.existsById(userId)) {
            throw new CustomException(ErrorCode.NOT_FOUND, "User not found with id: " + userId);
        }

        // 2. 각 항목별로 최신 5개만 조회하기 위한 Pageable 객체 생성
        // (Wishlist는 createdAt이 없으므로 bookId 내림차순으로 대체)
        Pageable wishlistPageable = PageRequest.of(0, PREVIEW_SIZE, Sort.by(Sort.Direction.DESC, "id.bookId"));
        Pageable reviewPageable = PageRequest.of(0, PREVIEW_SIZE, Sort.by(Sort.Direction.DESC, "createdAt"));
        Pageable highlightPageable = PageRequest.of(0, PREVIEW_SIZE, Sort.by(Sort.Direction.DESC, "createdAt"));

        // 3. 위시리스트, 리뷰, 하이라이트 목록 조회 (Page로 받아서 List로 변환)
        List<BookWishlist> wishlistItems = bookWishlistRepository.findAllById_UserId(userId, wishlistPageable).getContent();
        List<BookReview> reviewItems = bookReviewRepository.findAllByUserId(userId, reviewPageable).getContent();
        List<BookHighlight> highlightItems = bookHighlightRepository.findAllByUserId(userId, highlightPageable).getContent();

        // 4. 필요한 모든 bookId 수집 (N+1 방지)
        Set<Long> bookIds = Stream.concat(
                wishlistItems.stream().map(w -> w.getId().getBookId()),
                Stream.concat(
                        reviewItems.stream().map(BookReview::getBookId),
                        highlightItems.stream().map(BookHighlight::getBookId)
                )
        ).collect(Collectors.toSet());

        if (bookIds.isEmpty()) {
            return MyLibraryResponseDto.builder()
                    .wishlist(List.of())
                    .reviews(List.of())
                    .highlights(List.of())
                    .build();
        }

        // 5. bookId로 책 정보 일괄 조회
        Map<Long, Book> bookMap = bookRepository.findAllById(bookIds).stream()
                .collect(Collectors.toMap(Book::getBookId, Function.identity()));

        // 6. DTO 조립
        List<MyWishlistDto> wishlistDtos = wishlistItems.stream()
                .map(item -> bookMap.get(item.getId().getBookId()))
                .filter(book -> book != null)
                .map(MyWishlistDto::fromEntity)
                .collect(Collectors.toList());

        List<MyReviewDto> reviewDtos = reviewItems.stream()
                .map(review -> MyReviewDto.fromEntities(review, bookMap.get(review.getBookId())))
                .collect(Collectors.toList());

        List<MyHighlightDto> highlightDtos = highlightItems.stream()
                .map(highlight -> MyHighlightDto.fromEntities(highlight, bookMap.get(highlight.getBookId())))
                .collect(Collectors.toList());

        return MyLibraryResponseDto.builder()
                .wishlist(wishlistDtos)
                .reviews(reviewDtos)
                .highlights(highlightDtos)
                .build();
    }

    public Page<MyWishlistDto> getWishlist(Long userId, Pageable pageable) {
        validateUser(userId);

        // 1. 위시리스트 페이징 조회
        Page<BookWishlist> wishlistPage = bookWishlistRepository.findAllById_UserId(userId, pageable);

        // 2. 책 정보 N+1 방지를 위해 Book Map 생성
        List<Long> bookIds = wishlistPage.getContent().stream()
                .map(w -> w.getId().getBookId())
                .toList();

        Map<Long, Book> bookMap = bookRepository.findAllById(bookIds).stream()
                .collect(Collectors.toMap(Book::getBookId, Function.identity()));

        // 3. Page<Entity> -> Page<Dto> 변환
        return (Page<MyWishlistDto>) wishlistPage.map(item -> {
            Book book = bookMap.get(item.getId().getBookId());
            return (book != null) ? MyWishlistDto.fromEntity(book) : null;
        }).filter(dto -> dto != null); // (책이 삭제된 경우 null이 될 수 있으므로 필터링)
    }

    public Page<MyReviewDto> getReviews(Long userId, Pageable pageable) {
        validateUser(userId);

        // 1. 리뷰 페이징 조회
        Page<BookReview> reviewPage = bookReviewRepository.findAllByUserId(userId, pageable);

        // 2. 책 정보 N+1 방지를 위해 Book Map 생성
        List<Long> bookIds = reviewPage.getContent().stream()
                .map(BookReview::getBookId)
                .toList();

        Map<Long, Book> bookMap = bookRepository.findAllById(bookIds).stream()
                .collect(Collectors.toMap(Book::getBookId, Function.identity()));

        // 3. Page<Entity> -> Page<Dto> 변환
        return reviewPage.map(review -> MyReviewDto.fromEntities(review, bookMap.get(review.getBookId())));
    }

    public Page<MyHighlightDto> getHighlights(Long userId, Pageable pageable) {
        validateUser(userId);

        // 1. 하이라이트 페이징 조회
        Page<BookHighlight> highlightPage = bookHighlightRepository.findAllByUserId(userId, pageable);

        // 2. 책 정보 N+1 방지를 위해 Book Map 생성
        List<Long> bookIds = highlightPage.getContent().stream()
                .map(BookHighlight::getBookId)
                .toList();

        Map<Long, Book> bookMap = bookRepository.findAllById(bookIds).stream()
                .collect(Collectors.toMap(Book::getBookId, Function.identity()));

        // 3. Page<Entity> -> Page<Dto> 변환
        return highlightPage.map(highlight -> MyHighlightDto.fromEntities(highlight, bookMap.get(highlight.getBookId())));
    }

    // [Helper] 사용자 존재 유무 확인
    private void validateUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new CustomException(ErrorCode.NOT_FOUND, "User not found with id: " + userId);
        }
    }
}