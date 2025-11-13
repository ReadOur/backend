package com.readour.community.repository;

import com.readour.community.entity.RecruitmentMember;
import com.readour.community.entity.RecruitmentMemberId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Repository
public interface RecruitmentMemberRepository extends JpaRepository<RecruitmentMember, RecruitmentMemberId> {
    List<RecruitmentMember> findAllByRecruitment_RecruitmentId(Long recruitmentId);
    @Query("SELECT rm.id.recruitmentId FROM RecruitmentMember rm WHERE rm.id.userId = :userId")
    Set<Long> findAllRecruitmentIdsByUserId(@Param("userId") Long userId);

    @Query("SELECT rm.id.userId FROM RecruitmentMember rm WHERE rm.id.recruitmentId = :recruitmentId")
    List<Long> findAllUserIdsByRecruitmentId(@Param("recruitmentId") Long recruitmentId);
    // [N+1 해결] 여러 recruitment ID 중 '내가' 지원한 recruitment ID의 Set만 반환
    @Query("SELECT rm.id.recruitmentId FROM RecruitmentMember rm " +
            "WHERE rm.id.userId = :userId AND rm.id.recruitmentId IN :recruitmentIds")
    Set<Long> findAppliedRecruitmentIdsByUserIdAndRecruitmentIdIn(
            @Param("userId") Long userId, @Param("recruitmentIds") Collection<Long> recruitmentIds);
}