package com.readour.common.repository;

import com.readour.common.entity.FileAsset;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface FileAssetRepository extends JpaRepository<FileAsset, Long> {

    List<FileAsset> findAllByFileIdIn(Collection<Long> fileIds);
}
