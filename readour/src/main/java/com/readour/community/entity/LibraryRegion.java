package com.readour.community.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "library_region")
public class LibraryRegion {

    @Id
    @Column(length = 10)
    private String code; // 지역 코드 (예: "11", "11010")

    @Column(nullable = false, length = 50)
    private String name; // 지역 이름 (예: "서울", "종로구")

    @Column(length = 10)
    private String parentCode; // 상위 지역 코드 (예: "11"), 대분류는 null
}