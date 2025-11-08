package com.readour.community.repository;

import com.readour.community.entity.UserInterestedLibrary;
import com.readour.community.entity.UserInterestedLibraryId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserInterestedLibraryRepository extends JpaRepository<UserInterestedLibrary, UserInterestedLibraryId> {

    /**
     * 사용자가 등록한 모든 선호 도서관 목록을 조회합니다.
     */
    List<UserInterestedLibrary> findByUserId(Long userId);

    /**
     * 사용자가 특정 도서관을 선호 도서관으로 등록했는지 확인합니다.
     */
    Optional<UserInterestedLibrary> findByUserIdAndLibraryCode(Long userId, String libraryCode);

    /**
     * 사용자가 등록한 선호 도서관의 총 개수를 조회합니다.
     */
    long countByUserId(Long userId);
}