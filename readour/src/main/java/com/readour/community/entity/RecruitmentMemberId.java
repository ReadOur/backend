package com.readour.community.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode
public class RecruitmentMemberId implements Serializable {

    @Column(name = "recruitment_id")
    private Long recruitmentId;

    @Column(name = "user_id")
    private Long userId;
}