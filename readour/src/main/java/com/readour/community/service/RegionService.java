package com.readour.community.service;

import com.readour.common.enums.ErrorCode;
import com.readour.common.exception.CustomException;
import com.readour.community.dto.RegionDto;
import com.readour.community.repository.LibraryRegionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RegionService {

    private final LibraryRegionRepository regionRepository;

    /**
     * 이름으로 지역 검색 (요청사항)
     */
    public List<RegionDto> searchRegionsByName(String name) {
        if (name == null || name.isBlank()) {
            return List.of();
        }
        return regionRepository.findByNameContainingOrderByNameAsc(name).stream()
                .map(RegionDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 대분류 (광역시/도) 목록 조회
     */
    public List<RegionDto> getMainRegions() {
        return regionRepository.findByParentCodeIsNullOrderByNameAsc().stream()
                .map(RegionDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 세부 (시/군/구) 목록 조회
     */
    public List<RegionDto> getDetailedRegions(String parentCode) {
        if (!regionRepository.existsById(parentCode)) {
            throw new CustomException(ErrorCode.NOT_FOUND, "유효하지 않은 상위 지역 코드입니다: " + parentCode);
        }
        return regionRepository.findByParentCodeOrderByNameAsc(parentCode).stream()
                .map(RegionDto::fromEntity)
                .collect(Collectors.toList());
    }
}