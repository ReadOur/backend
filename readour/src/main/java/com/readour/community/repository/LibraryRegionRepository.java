package com.readour.community.repository;

import com.readour.community.entity.LibraryRegion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LibraryRegionRepository extends JpaRepository<LibraryRegion, String> {

    /**
     * 이름으로 지역 검색 (사용자 요구사항)
     */
    List<LibraryRegion> findByNameContainingOrderByNameAsc(String name);

    /**
     * 대분류(광역시/도) 목록 조회
     */
    List<LibraryRegion> findByParentCodeIsNullOrderByNameAsc();

    /**
     * 특정 대분류에 속한 세부 지역(시/군/구) 목록 조회
     */
    List<LibraryRegion> findByParentCodeOrderByNameAsc(String parentCode);
}