package com.readour.community.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.readour.common.entity.User;
import com.readour.common.enums.ErrorCode;
import com.readour.common.enums.Gender;
import com.readour.common.exception.CustomException;
import com.readour.community.dto.*;
import com.readour.community.dto.AverageRatingProjection;
import com.readour.community.dto.LibraryApiDtos.SearchDoc;
import com.readour.community.entity.Book;
import com.readour.community.entity.BookHighlight;
import com.readour.community.entity.BookReview;
import com.readour.community.entity.BookWishlist;
import com.readour.community.entity.BookWishlistId;
import com.readour.community.repository.BookWishlistRepository;
import com.readour.community.entity.UserInterestedLibrary;
import com.readour.community.entity.UserInterestedLibraryId;
import com.readour.community.repository.BookHighlightRepository;
import com.readour.community.repository.BookRepository;
import com.readour.community.repository.BookReviewRepository;
import com.readour.community.repository.UserInterestedLibraryRepository;
import com.readour.common.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
@Slf4j
@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final BookReviewRepository bookReviewRepository;
    private final BookHighlightRepository bookHighlightRepository;
    private final UserInterestedLibraryRepository userInterestedLibraryRepository;
    private final BookWishlistRepository bookWishlistRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${library.api.key}")
    private String apiKey;

    @Value("${library.api.base-url}")
    private String baseUrl;

    // 로컬 DB에서 도서명으로 책을 검색합니다.
    @Transactional(readOnly = true)
    public Page<Book> searchBooksInDbByTitle(String title, Pageable pageable) {
        log.debug("Searching DB for title: {}", title);
        return bookRepository.findByBooknameContaining(title, pageable);
    }


    // 정보나루 API를 호출하여 도서를 검색합니다. (DB 저장 X)
    @Transactional(readOnly = true)
    public Page<BookSummaryDto> searchBooksFromApi(String keyword, Pageable pageable) {
        log.debug("Searching API for keyword: {}", keyword);

        // 1. [API Search] 외부 API(#16) 호출
        URI uri = UriComponentsBuilder
                .fromUriString(baseUrl + "/srchBooks")
                .queryParam("authKey", apiKey)
                .queryParam("keyword", keyword)
                .queryParam("pageNo", pageable.getPageNumber() + 1)
                .queryParam("pageSize", pageable.getPageSize())
                .queryParam("format", "json")
                .build()
                .encode()
                .toUri();

        try {
            String jsonResponse = restTemplate.getForObject(uri, String.class);
            log.info("External API Response JSON for keyword [{}]: {}", keyword, jsonResponse);

            LibraryApiDtos.SearchResponseWrapper wrapper = objectMapper.readValue(jsonResponse, LibraryApiDtos.SearchResponseWrapper.class);

            if (wrapper == null || wrapper.getResponse() == null || wrapper.getResponse().getDocs() == null) {
                log.warn("API response is empty or malformed for keyword: {}", keyword);
                return Page.empty(pageable);
            }

            LibraryApiDtos.SearchResponse response = wrapper.getResponse();

            List<SearchDoc> apiDocs = response.getDocs().stream()
                    .map(LibraryApiDtos.SearchDocWrapper::getDoc)
                    .toList();

            if (apiDocs.isEmpty()) {
                return Page.empty(pageable);
            }

            // 2. [DB Book Fetch] API 결과의 ISBN으로 우리 DB 조회
            List<String> isbns = apiDocs.stream()
                    .map(SearchDoc::getIsbn13)
                    .filter(isbn -> isbn != null && !isbn.isBlank())
                    .toList();

            Map<String, Book> existingBooksMap = Collections.emptyMap();
            if (!isbns.isEmpty()) {
                existingBooksMap = bookRepository.findByIsbn13In(isbns).stream()
                        .collect(Collectors.toMap(Book::getIsbn13, Function.identity()));
            }

            // 3. [DB Rating Fetch] DB에 있는 책들의 bookId로 평점/리뷰 수 조회
            Collection<Long> existingBookIds = existingBooksMap.values().stream()
                    .map(Book::getBookId)
                    .toList();

            Map<Long, AverageRatingProjection> ratingInfoMap;
            if (!existingBookIds.isEmpty()) {
                ratingInfoMap = bookReviewRepository.findAverageRatingsByBookIds(existingBookIds).stream()
                        .collect(Collectors.toMap(AverageRatingProjection::getBookId, Function.identity()));
            } else {
                ratingInfoMap = Collections.emptyMap();
            }

            // 4. [Merge] API 결과와 DB 데이터를 조합
            Map<String, Book> finalExistingBooksMap = existingBooksMap;
            List<BookSummaryDto> bookSummaries = apiDocs.stream()
                    .map(doc -> {
                        Book existingBook = finalExistingBooksMap.get(doc.getIsbn13());
                        AverageRatingProjection ratingInfo = (existingBook != null)
                                ? ratingInfoMap.get(existingBook.getBookId())
                                : null;
                        return BookSummaryDto.from(doc, existingBook, ratingInfo);
                    })
                    .collect(Collectors.toList());

            return new PageImpl<>(bookSummaries, pageable, response.getNumFound());

        } catch (Exception e) {
            log.error("Failed to search books from API. URI: " + uri, e);
            throw new CustomException(ErrorCode.BAD_GATEWAY, "도서 API 호출에 실패했습니다.");
        }
    }

    // ISBN으로 로컬 DB를 먼저 조회하고, 없으면 API #6(상세조회)를 호출하여 DB에 저장한 후 반환합니다.
    @Transactional
    public BookResponseDto findOrCreateBookByIsbn(String isbn13, Long currentUserId) {
        // 1. 로컬 DB에서 ISBN으로 책을 조회
        Optional<Book> existingBook = bookRepository.findByIsbn13(isbn13);

        Book bookEntity;

        if (existingBook.isPresent()) {
            log.debug("Book found in DB. isbn: {}", isbn13);
            bookEntity = existingBook.get();
        } else {
            // 2. DB에 없으면, 정보나루 API #6 (상세조회) 호출
            log.debug("Book not in DB. Calling API #6 for isbn: {}", isbn13);
            LibraryApiDtos.BookInfo apiBook = fetchBookDetailsFromApi(isbn13);

            // 3. API 결과를 Book 엔티티로 변환
            Book newBook = mapApiDtoToBookEntity(apiBook);

            // 4. 새 책 정보를 DB에 저장
            bookEntity = bookRepository.save(newBook);
            log.info("New book saved to DB. bookId: {}, isbn: {}", bookEntity.getBookId(), isbn13);
        }

        // 5. 저장/조회된 bookId로 평점/리뷰 수 조회
        AverageRatingProjection ratingInfo = bookReviewRepository.findAverageRatingByBookId(bookEntity.getBookId())
                .orElse(null); // 리뷰가 없으면 null

        // 6. 위시리스트 추가 여부 반환
        boolean isWishlisted = false;
        if (currentUserId != null) { // 비회원이 아닐 경우에만 조회
            isWishlisted = bookWishlistRepository.existsByIdUserIdAndIdBookId(currentUserId, bookEntity.getBookId());
        }

        // 7. 최종 DTO로 변환하여 반환
        return BookResponseDto.fromEntity(bookEntity, ratingInfo, isWishlisted);
    }

    // 사용자 정보(성별, 연령)를 기반으로 인기대출도서 API(#3)를 호출합니다.(Wrapper DTO 사용 O)
    @Transactional(readOnly = true)
    public Page<PopularBookDto> getPopularBooks(Long userId, Pageable pageable) {
        // 1. 사용자 정보 조회 (성별, 연령 확인용)
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "User not found with id: " + userId));

        // 2. API 파라미터로 변환
        String genderCode = mapGenderToApiCode(user.getGender());
        String ageCode = mapBirthDateToApiAgeCode(user.getBirthDate());

        log.debug("Fetching popular books for userId: {}. genderCode: {}, ageCode: {}", userId, genderCode, ageCode);

        // 3. API URI 빌드 (API #3 - loanItemSrch)
        UriComponentsBuilder uriBuilder = UriComponentsBuilder
                .fromUriString(baseUrl + "/loanItemSrch")
                .queryParam("authKey", apiKey)
                .queryParam("pageNo", pageable.getPageNumber() + 1)
                .queryParam("pageSize", pageable.getPageSize())
                .queryParam("format", "json");

        if (genderCode != null) {
            uriBuilder.queryParam("gender", genderCode);
        }
        if (ageCode != null) {
            uriBuilder.queryParam("age", ageCode);
        }

        URI uri = uriBuilder.build()
                .encode()
                .toUri();

        // 4. API 호출 및 파싱
        try {
            String jsonResponse = restTemplate.getForObject(uri, String.class);
            log.info("External API Response JSON for popular books: {}", jsonResponse);

            LibraryApiDtos.PopularBookResponseWrapper wrapper = objectMapper.readValue(jsonResponse, LibraryApiDtos.PopularBookResponseWrapper.class);

            if (wrapper == null || wrapper.getResponse() == null || wrapper.getResponse().getDocs() == null) {
                log.warn("Popular books API response is empty or malformed.");
                return Page.empty(pageable);
            }

            LibraryApiDtos.PopularBookResponse response = wrapper.getResponse();

            List<PopularBookDto> popularBooks = response.getDocs().stream()
                    .map(LibraryApiDtos.PopularBookDocWrapper::getDoc) // PopularBookDocWrapper -> PopularBookDoc
                    .map(PopularBookDto::from)                   // PopularBookDoc -> PopularBookDto
                    .collect(Collectors.toList());

            return new PageImpl<>(popularBooks, pageable, response.getNumFound());

        } catch (Exception e) {
            log.error("Failed to fetch popular books from API. URI: " + uri, e);
            throw new CustomException(ErrorCode.BAD_GATEWAY, "인기 도서 API 호출에 실패했습니다.");
        }
    }

    // DB에 책이 있는지 확인 (Controller용)
    @Transactional(readOnly = true)
    public boolean isBookInDb(String isbn13) {
        return bookRepository.findByIsbn13(isbn13).isPresent();
    }

    // DB에 저장된 도서 상세 정보 조회
    @Transactional(readOnly = true)
    public BookResponseDto getBookDetailsById(Long bookId, Long currentUserId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "Book not found with id: " + bookId));

        // 2. 평점 정보 조회
        AverageRatingProjection ratingInfo = bookReviewRepository.findAverageRatingByBookId(bookId)
                .orElse(null); // 리뷰가 없으면 null

        // 3. 위시리스트 상태 조회
        boolean isWishlisted = false;
        if (currentUserId != null) { // 비회원이 아닐 경우에만 조회
            isWishlisted = bookWishlistRepository.existsByIdUserIdAndIdBookId(currentUserId, bookId);
        }

        // 3. DTO로 변환하여 반환
        return BookResponseDto.fromEntity(book, ratingInfo, isWishlisted);
    }

    // API #6 호출 (Wrapper DTO 사용 X)
    private LibraryApiDtos.BookInfo fetchBookDetailsFromApi(String isbn13) {
        URI uri = UriComponentsBuilder
                .fromUriString(baseUrl + "/srchDtlList")
                .queryParam("authKey", apiKey)
                .queryParam("isbn13", isbn13)
                .queryParam("format", "json")
                .build()
                .encode()
                .toUri();

        try {
            String jsonResponse = restTemplate.getForObject(uri, String.class);
            log.info("External API Response JSON for detail [{}]: {}", isbn13, jsonResponse);

            // [1] DetailResponseWrapper.class 로 파싱합니다.
            LibraryApiDtos.DetailResponseWrapper wrapper = objectMapper.readValue(jsonResponse, LibraryApiDtos.DetailResponseWrapper.class);

            // [2] 수정된 DTO 구조(List)에 맞게 검증 로직을 수정합니다.
            if (wrapper == null || wrapper.getResponse() == null ||
                    wrapper.getResponse().getDetail() == null || // 리스트 자체가 null인지
                    wrapper.getResponse().getDetail().isEmpty() || // 리스트가 비었는지
                    wrapper.getResponse().getDetail().get(0).getBook() == null) // 첫 항목에 book이 있는지
            {
                log.warn("API response for detail is empty or malformed for isbn: {}", isbn13);
                throw new CustomException(ErrorCode.NOT_FOUND, "API에서 도서 정보를 찾을 수 없습니다. ISBN: " + isbn13);
            }

            // [3] [3] "detail" 리스트의 첫 번째 항목(get(0))에서 book 정보를 반환합니다.
            return wrapper.getResponse().getDetail().get(0).getBook();

        } catch (JsonProcessingException e) {
            log.error("Failed to parse book detail from API. ISBN: " + isbn13, e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "API 응답 파싱에 실패했습니다.");
        } catch (Exception e) {
            log.error("Failed to fetch book detail from API. URI: " + uri, e);
            throw new CustomException(ErrorCode.BAD_GATEWAY, "도서 상세 API 호출에 실패했습니다.");
        }
    }

    // PI DTO -> 엔티티 변환
    private Book mapApiDtoToBookEntity(LibraryApiDtos.BookInfo apiBook) {
        Integer publicationYear = null;
        try {
            if (apiBook.getPublicationYear() != null && !apiBook.getPublicationYear().isBlank()) {
                publicationYear = Integer.parseInt(apiBook.getPublicationYear().trim());
            }
        } catch (NumberFormatException e) {
            log.warn("Could not parse publicationYear: {}", apiBook.getPublicationYear());
        }

        // publicationDate 파싱 로직
        LocalDate publicationDate = null;
        try {
            // "YYYY-MM-DD" 형식을 파싱 시도
            if (apiBook.getPublicationDate() != null && apiBook.getPublicationDate().length() >= 10) {
                publicationDate = LocalDate.parse(apiBook.getPublicationDate().substring(0, 10));
            }
            // 파싱 실패 시, "YYYY"라도 파싱해서 해당 년도의 1월 1일로 저장
            else if (publicationYear != null) {
                publicationDate = LocalDate.of(publicationYear, 1, 1);
            }
        } catch (Exception e) {
            log.warn("Could not parse publicationDate: {}", apiBook.getPublicationDate());
            if (publicationYear != null) { // 파싱 실패 시 Year 기준으로 재시도
                publicationDate = LocalDate.of(publicationYear, 1, 1);
            }
        }

        return Book.builder()
                .isbn10(apiBook.getIsbn())
                .isbn13(apiBook.getIsbn13())
                .bookname(apiBook.getBookname())
                .authors(apiBook.getAuthors())
                .publisher(apiBook.getPublisher())
                .publicationYear(publicationYear)
                .publicationDate(publicationDate)
                .classNo(apiBook.getClassNo())
                .classNm(apiBook.getClassNm())
                .description(apiBook.getDescription())
                .bookImageUrl(apiBook.getBookImageURL())
                .vol(apiBook.getVol())
                .additionSymbol(apiBook.getAdditionSymbol())
                .build();
    }

    // ReadOur Gender(MALE, FEMALE)를 API 코드(0, 1)로 변환
    private String mapGenderToApiCode(Gender gender) {
        if (gender == null) {
            return null;
        }
        switch (gender) {
            case MALE:
                return "0"; // API 가이드: 0 (남성)
            case FEMALE:
                return "1"; // API 가이드: 1 (여성)
            default:
                return null;
        }
    }

    // 생년월일을 API 연령 코드(0, 6, 8, 14, 20, 30...)로 변환
    private String mapBirthDateToApiAgeCode(LocalDate birthDate) {
        if (birthDate == null) {
            return null;
        }
        int age = Period.between(birthDate, LocalDate.now()).getYears();

        if (age <= 19) return "10"; // 10: 10대
        if (age <= 29) return "20"; // 20: 20대
        if (age <= 39) return "30"; // 30: 30대
        if (age <= 49) return "40"; // 40: 40대
        if (age <= 59) return "50"; // 50: 50대
        return "60"; // 60: 60세 이상
    }

    // 위시리스트 토글
    @Transactional
    public boolean toggleWishlist(Long bookId, Long userId) {
        // 1. 책 존재 여부 확인
        if (!bookRepository.existsById(bookId)) {
            throw new CustomException(ErrorCode.NOT_FOUND, "Book not found with id: " + bookId);
        }

        // 2. PostLike와 동일한 토글 로직
        BookWishlistId wishlistId = new BookWishlistId(userId, bookId);
        Optional<BookWishlist> existingWish = bookWishlistRepository.findById(wishlistId);

        if (existingWish.isPresent()) {
            // 이미 있으면 삭제 (해제)
            bookWishlistRepository.deleteById(wishlistId);
            log.debug("Wishlist item removed for bookId={}, userId={}", bookId, userId);
            return false; // 위시리스트에서 해제됨
        } else {
            // 없으면 추가 (등록)
            BookWishlist newWish = BookWishlist.builder().id(wishlistId).build();
            bookWishlistRepository.save(newWish);
            log.debug("Wishlist item added for bookId={}, userId={}", bookId, userId);
            return true; // 위시리스트에 추가됨
        }
    }

    // (SD-27) 책 리뷰 작성
    @Transactional
    public BookReviewResponseDto addBookReview(Long bookId, Long userId, BookReviewCreateRequestDto dto) {
        // 1. 책 존재 여부 확인
        if (!bookRepository.existsById(bookId)) {
            throw new CustomException(ErrorCode.NOT_FOUND, "Book not found with id: " + bookId);
        }

        // 2. 작성자 정보 확인
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "User not found with id: " + userId));

        // 3. 리뷰 중복 작성 확인 (uq_book_user 제약조건)
        bookReviewRepository.findByBookIdAndUserId(bookId, userId).ifPresent(review -> {
            throw new CustomException(ErrorCode.CONFLICT, "이미 해당 도서에 대한 리뷰를 작성했습니다.");
        });

        // 4. 엔티티 생성 및 저장
        BookReview review = BookReview.builder()
                .bookId(bookId)
                .userId(userId)
                .content(dto.getContent())
                .rating(dto.getRating())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        BookReview savedReview = bookReviewRepository.save(review);

        return BookReviewResponseDto.fromEntity(savedReview, author);
    }

    // (SD-27) 책 리뷰 조회 (정렬: 최신순)
    @Transactional(readOnly = true)
    public Page<BookReviewResponseDto> getBookReviews(Long bookId, Pageable pageable) {
        // 1. 책 존재 여부 확인
        if (!bookRepository.existsById(bookId)) {
            throw new CustomException(ErrorCode.NOT_FOUND, "Book not found with id: " + bookId);
        }

        // 2. 정렬 기준을 createdAt 내림차순(최신순)으로 고정
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "createdAt"));

        // 3. 리뷰 조회
        Page<BookReview> reviewPage = bookReviewRepository.findAllByBookId(bookId, sortedPageable);

        // 4. 작성자 닉네임 매핑
        // (N+1 문제 방지를 위해 userId로 User Map 생성)
        List<Long> authorIds = reviewPage.getContent().stream()
                .map(BookReview::getUserId)
                .distinct()
                .toList();

        Map<Long, User> authorMap = userRepository.findAllById(authorIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        // 5. DTO로 변환
        return reviewPage.map(review ->
                BookReviewResponseDto.fromEntity(review, authorMap.get(review.getUserId()))
        );
    }

    // (SD-28) 책 리뷰 수정
    @Transactional
    public BookReviewResponseDto updateBookReview(Long reviewId, Long userId, BookReviewUpdateRequestDto dto) {
        // 1. 수정 권한 확인 (리뷰 ID + 작성자 ID)
        BookReview review = bookReviewRepository.findByReviewIdAndUserId(reviewId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.FORBIDDEN, "리뷰를 수정할 권한이 없거나 리뷰가 존재하지 않습니다."));

        // 2. 내용 및 평점 수정
        review.setContent(dto.getContent());
        review.setRating(dto.getRating());
        review.setUpdatedAt(LocalDateTime.now());

        // 3. 작성자 정보 (DTO 반환용)
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "User not found with id: " + userId));

        return BookReviewResponseDto.fromEntity(review, author);
    }

    // (SD-29) 책 리뷰 삭제
    @Transactional
    public void deleteBookReview(Long reviewId, Long userId) {
        // 1. 삭제 권한 확인 (리뷰 ID + 작성자 ID)
        BookReview review = bookReviewRepository.findByReviewIdAndUserId(reviewId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.FORBIDDEN, "리뷰를 삭제할 권한이 없거나 리뷰가 존재하지 않습니다."));

        // 2. 삭제
        bookReviewRepository.delete(review);
    }

    // (SD-31) 책 하이라이트 작성
    @Transactional
    public BookHighlightResponseDto addBookHighlight(Long bookId, Long userId, BookHighlightCreateRequestDto dto) {
        // 1. 책 존재 여부 확인
        if (!bookRepository.existsById(bookId)) {
            throw new CustomException(ErrorCode.NOT_FOUND, "Book not found with id: " + bookId);
        }

        // 2. 작성자 정보 확인
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "User not found with id: " + userId));

        // 3. 엔티티 생성 및 저장
        BookHighlight highlight = BookHighlight.builder()
                .bookId(bookId)
                .userId(userId)
                .content(dto.getContent())
                .pageNumber(dto.getPageNumber())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        BookHighlight savedHighlight = bookHighlightRepository.save(highlight);

        return BookHighlightResponseDto.fromEntity(savedHighlight, author);
    }

    // (SD-31) 책 하이라이트 조회
    @Transactional(readOnly = true)
    public Page<BookHighlightResponseDto> getBookHighlights(Long bookId, Pageable pageable) {
        // 1. 책 존재 여부 확인
        if (!bookRepository.existsById(bookId)) {
            throw new CustomException(ErrorCode.NOT_FOUND, "Book not found with id: " + bookId);
        }

        // 2. 정렬 기준을 createdAt 내림차순(최신순)으로 고정
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "createdAt"));

        // 3. 하이라이트 조회
        Page<BookHighlight> highlightPage = bookHighlightRepository.findAllByBookId(bookId, sortedPageable);

        // 4. 작성자 닉네임 매핑 (N+1 방지)
        List<Long> authorIds = highlightPage.getContent().stream()
                .map(BookHighlight::getUserId)
                .distinct()
                .toList();

        Map<Long, User> authorMap = userRepository.findAllById(authorIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        // 5. DTO로 변환
        return highlightPage.map(highlight ->
                BookHighlightResponseDto.fromEntity(highlight, authorMap.get(highlight.getUserId()))
        );
    }

    // (SD-32) 책 하이라이트 수정
    @Transactional
    public BookHighlightResponseDto updateBookHighlight(Long highlightId, Long userId, BookHighlightUpdateRequestDto dto) {
        // 1. 수정 권한 확인 (하이라이트 ID + 작성자 ID)
        BookHighlight highlight = bookHighlightRepository.findByHighlightIdAndUserId(highlightId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.FORBIDDEN, "하이라이트를 수정할 권한이 없거나 하이라이트가 존재하지 않습니다."));

        // 2. 내용 및 페이지 번호 수정
        highlight.setContent(dto.getContent());
        highlight.setPageNumber(dto.getPageNumber());
        highlight.setUpdatedAt(LocalDateTime.now());

        // 3. 작성자 정보 (DTO 반환용)
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "User not found with id: " + userId));

        return BookHighlightResponseDto.fromEntity(highlight, author);
    }

    // (SD-33) 책 하이라이트 삭제
    @Transactional
    public void deleteBookHighlight(Long highlightId, Long userId) {
        // 1. 삭제 권한 확인 (하이라이트 ID + 작성자 ID)
        BookHighlight highlight = bookHighlightRepository.findByHighlightIdAndUserId(highlightId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.FORBIDDEN, "하이라이트를 삭제할 권한이 없거나 하이라이트가 존재하지 않습니다."));

        // 2. 삭제
        bookHighlightRepository.delete(highlight);
    }

    // 선호 도서관 등록을 위한 도서관 검색
    @Transactional(readOnly = true)
    public Page<LibrarySearchResponseDto> searchLibraries(String region, String dtlRegion, Pageable pageable) {

        UriComponentsBuilder uriBuilder = UriComponentsBuilder
                .fromUriString(baseUrl + "/libSrch") // 👈 [1] API #1 (/libSrch) 호출
                .queryParam("authKey", apiKey)
                .queryParam("region", region) // 👈 [2] 필수 지역 코드 (예: "11" 서울)
                .queryParam("pageNo", pageable.getPageNumber() + 1)
                .queryParam("pageSize", pageable.getPageSize())
                .queryParam("format", "json");

        if (dtlRegion != null && !dtlRegion.isBlank()) {
            uriBuilder.queryParam("dtl_region", dtlRegion); // 👈 [3] 선택 세부 지역 코드 (예: "11010" 종로구)
        }

        URI uri = uriBuilder.build().encode().toUri();

        try {
            String jsonResponse = restTemplate.getForObject(uri, String.class);
            log.debug("API #1 Response for region [{}]: {}", region, jsonResponse);

            LibraryApiDtos.LibSearchResponseWrapper wrapper = objectMapper.readValue(jsonResponse, LibraryApiDtos.LibSearchResponseWrapper.class);

            if (wrapper == null || wrapper.getResponse() == null || wrapper.getResponse().getLibs() == null) {
                log.warn("API #1 response is empty or malformed. URI: {}", uri);
                return Page.empty(pageable);
            }

            LibraryApiDtos.LibSearchResponse response = wrapper.getResponse();

            List<LibrarySearchResponseDto> dtos = response.getLibs().stream()
                    .map(LibraryApiDtos.LibWrapper::getLib)
                    .map(LibrarySearchResponseDto::from)
                    .collect(Collectors.toList());

            return new PageImpl<>(dtos, pageable, response.getNumFound());

        } catch (Exception e) {
            log.error("Failed to fetch API #1. URI: " + uri, e);
            throw new CustomException(ErrorCode.BAD_GATEWAY, "도서관 API 호출에 실패했습니다.");
        }
    }

    // (SD-34-1) 사용자 선호 도서관 등록
    @Transactional
    public UserLibraryResponseDto registerInterestedLibrary(Long userId, String libraryCode, String libraryName) {
        // 1. 사용자 존재 여부 확인
        if (!userRepository.existsById(userId)) {
            throw new CustomException(ErrorCode.NOT_FOUND, "User not found with id: " + userId);
        }

        // 2. 선호 도서관 개수 제한 (최대 3개)
        long currentCount = userInterestedLibraryRepository.countByUserId(userId);
        if (currentCount >= 3) {
            // 403 Forbidden 또는 400 Bad Request가 적절합니다. (정책 위반)
            throw new CustomException(ErrorCode.FORBIDDEN, "선호 도서관은 최대 3개까지만 등록할 수 있습니다.");
        }

        // 3. 이미 등록되었는지 확인
        UserInterestedLibraryId id = new UserInterestedLibraryId(userId, libraryCode);
        if (userInterestedLibraryRepository.existsById(id)) {
            throw new CustomException(ErrorCode.CONFLICT, "이미 등록된 도서관입니다.");
        }

        // 4. 등록
        UserInterestedLibrary entity = UserInterestedLibrary.builder()
                .userId(userId)
                .libraryCode(libraryCode)
                .libraryName(libraryName)
                .createdAt(LocalDateTime.now())
                .build();

        UserInterestedLibrary savedEntity = userInterestedLibraryRepository.save(entity);
        return UserLibraryResponseDto.fromEntity(savedEntity);
    }

    // (SD-34-2) 사용자 선호 도서관 삭제
    @Transactional
    public void deleteInterestedLibrary(Long userId, String libraryCode) {
        UserInterestedLibraryId id = new UserInterestedLibraryId(userId, libraryCode);

        UserInterestedLibrary entity = userInterestedLibraryRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "등록되지 않은 도서관입니다."));

        userInterestedLibraryRepository.delete(entity);
    }

    // (Helper) 사용자 선호 도서관 목록 조회
    @Transactional(readOnly = true)
    public List<UserLibraryResponseDto> getInterestedLibraries(Long userId) {
        return userInterestedLibraryRepository.findByUserId(userId).stream()
                .map(UserLibraryResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    // (SD-34) 선호 도서관 대상, 책 대출 가능 여부 조회
    @Transactional(readOnly = true)
    public List<LibraryAvailabilityDto> checkBookAvailability(Long userId, String isbn13) {
        // 1. 사용자의 선호 도서관 목록 조회
        List<UserInterestedLibrary> libraries = userInterestedLibraryRepository.findByUserId(userId);
        if (libraries.isEmpty()) {
            return new ArrayList<>(); // 선호 도서관이 없으면 빈 리스트 반환
        }

        // 2. 각 도서관에 대해 API #11 호출
        return libraries.stream()
                .map(lib -> fetchBookAvailabilityFromApi(lib.getLibraryCode(), lib.getLibraryName(), isbn13))
                .collect(Collectors.toList());
    }

    // [Helper] (SD-34) API #11 (/bookExist) 호출
    private LibraryAvailabilityDto fetchBookAvailabilityFromApi(String libCode, String libName, String isbn13) {
        URI uri = UriComponentsBuilder
                .fromUriString(baseUrl + "/bookExist")
                .queryParam("authKey", apiKey)
                .queryParam("libCode", libCode)
                .queryParam("isbn13", isbn13)
                .queryParam("format", "json")
                .build()
                .encode()
                .toUri();

        try {
            String jsonResponse = restTemplate.getForObject(uri, String.class);
            log.debug("API #11 Response for libCode [{}], isbn [{}]: {}", libCode, isbn13, jsonResponse);

            // API #11 응답 DTO (Wrapper)로 파싱
            LibraryApiDtos.BookExistResponseWrapper wrapper = objectMapper.readValue(jsonResponse, LibraryApiDtos.BookExistResponseWrapper.class);

            if (wrapper == null || wrapper.getResponse() == null || wrapper.getResponse().getResult() == null) {
                log.warn("API #11 response is malformed. URI: {}", uri);
                return LibraryAvailabilityDto.from(libCode, libName, null); // 실패 시 (소장X)
            }

            return LibraryAvailabilityDto.from(libCode, libName, wrapper.getResponse().getResult());

        } catch (HttpClientErrorException.NotFound e) {
            // 404 NotFound는 API가 도서관/책 정보를 못찾은 경우로, "소장 안함"으로 간주
            log.warn("API #11 returned 404. URI: {}", uri);
            return LibraryAvailabilityDto.from(libCode, libName, null);
        } catch (Exception e) {
            log.error("Failed to fetch API #11. URI: " + uri, e);
            // API 호출 자체 실패 시 (소장X)로 간주
            return LibraryAvailabilityDto.from(libCode, libName, null);
        }
    }
}
