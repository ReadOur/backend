package com.readour.community.entity;

import com.readour.community.enums.RecruitmentStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "recruitment")
public class Recruitment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long recruitmentId;

    @OneToOne(mappedBy = "recruitment", fetch = FetchType.LAZY)
    private Post post;

    @Column(nullable = false)
    private Integer recruitmentLimit;

    @Column(nullable = false)
    @Builder.Default
    private Integer currentMemberCount = 1; // 현재 인원 (작성자 포함)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private RecruitmentStatus status = RecruitmentStatus.RECRUITING;

    @Column(nullable = false)
    private String chatRoomName;

    private String chatRoomDescription;

    @Column(nullable = true)
    private Long chatRoomId;

    @OneToMany(mappedBy = "recruitment", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Builder.Default
    private Set<RecruitmentMember> members = new HashSet<>();

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}