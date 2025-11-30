package com.readour.common.repository;

import com.readour.common.entity.FileLink;
import com.readour.common.entity.FileLinkId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FileLinkRepository extends JpaRepository<FileLink, FileLinkId> {

    List<FileLink> findAllByTargetTypeAndTargetIdOrderByCreatedAtAsc(String targetType, Long targetId);

    List<FileLink> findAllByTargetTypeAndTargetIdAndFileIdIn(String targetType, Long targetId, List<Long> fileIds);

    @Modifying
    @Query("delete from FileLink l where l.targetType = :targetType and l.targetId = :targetId")
    void deleteAllByTarget(@Param("targetType") String targetType, @Param("targetId") Long targetId);

    @Modifying
    @Query("delete from FileLink l where l.targetType = :targetType and l.targetId = :targetId and l.fileId in :fileIds")
    void deleteAllByTargetAndFileIds(@Param("targetType") String targetType,
                                     @Param("targetId") Long targetId,
                                     @Param("fileIds") List<Long> fileIds);
}
