package com.readour.community.entity;

import com.readour.common.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "recruitment_member")
public class RecruitmentMember {

    @EmbeddedId
    private RecruitmentMemberId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("recruitmentId")
    @JoinColumn(name = "recruitment_id")
    private Recruitment recruitment;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @CreationTimestamp
    private LocalDateTime joinedAt;
}