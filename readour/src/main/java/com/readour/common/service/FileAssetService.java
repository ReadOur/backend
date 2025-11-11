package com.readour.common.service;

import com.readour.common.config.AwsProperties;
import com.readour.common.dto.FileResponseDto;
import com.readour.common.entity.FileAsset;
import com.readour.common.entity.FileLink;
import com.readour.common.entity.FileLinkId;
import com.readour.common.enums.ErrorCode;
import com.readour.common.exception.CustomException;
import com.readour.common.repository.FileAssetRepository;
import com.readour.common.repository.FileLinkRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FileAssetService {

    private static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";

    private final S3Client s3Client;
    private final AwsProperties awsProperties;
    private final FileAssetRepository fileAssetRepository;
    private final FileLinkRepository fileLinkRepository;

    @Transactional
    public FileAsset upload(MultipartFile file, Long ownerUserId) {
        validateBucket();
        return storeFile(file, ownerUserId);
    }

    @Transactional
    public List<FileAsset> uploadAll(List<MultipartFile> files, Long ownerUserId) {
        validateBucket();
        if (CollectionUtils.isEmpty(files)) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "업로드할 파일이 없습니다.");
        }

        List<FileAsset> uploaded = new ArrayList<>(files.size());
        for (MultipartFile file : files) {
            uploaded.add(storeFile(file, ownerUserId));
        }
        return uploaded;
    }

    private FileAsset storeFile(MultipartFile file, Long ownerUserId) {
        if (file == null || file.isEmpty()) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "업로드할 파일이 없습니다.");
        }

        String objectKey = buildObjectKey(file.getOriginalFilename());
        String contentType = StringUtils.defaultIfBlank(file.getContentType(), DEFAULT_CONTENT_TYPE);

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(awsProperties.getS3().getBucket())
                .key(objectKey)
                .contentType(contentType)
                .build();
        try (var inputStream = file.getInputStream()) {
            s3Client.putObject(request, RequestBody.fromInputStream(inputStream, file.getSize()));
        } catch (IOException e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "파일 업로드에 실패했습니다.");
        }

        FileAsset asset = FileAsset.builder()
                .ownerUserId(ownerUserId)
                .bucket(awsProperties.getS3().getBucket())
                .objectKey(objectKey)
                .originalName(extractFilename(file.getOriginalFilename()))
                .mimeType(contentType)
                .byteSize(file.getSize())
                .isDeleted(false)
                .createdAt(LocalDateTime.now())
                .build();

        return fileAssetRepository.save(asset);
    }

    @Transactional
    public void linkFile(Long fileId, String targetType, Long targetId) {
        if (fileId == null || targetId == null || !StringUtils.isNotBlank(targetType)) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "파일 연결 정보가 올바르지 않습니다.");
        }

        // ensure file exists
        getFile(fileId);

        FileLinkId id = new FileLinkId(fileId, normalizeTargetType(targetType), targetId);
        if (fileLinkRepository.existsById(id)) {
            return;
        }

        FileLink link = FileLink.builder()
                .fileId(id.getFileId())
                .targetType(id.getTargetType())
                .targetId(id.getTargetId())
                .createdAt(LocalDateTime.now())
                .build();
        fileLinkRepository.save(link);
    }

    @Transactional
    public void replaceLinks(String targetType, Long targetId, List<Long> fileIds) {
        String normalized = normalizeTargetType(targetType);
        fileLinkRepository.deleteAllByTarget(normalized, targetId);

        if (CollectionUtils.isEmpty(fileIds)) {
            return;
        }

        Set<Long> distinctIds = fileIds.stream()
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (distinctIds.isEmpty()) {
            return;
        }

        LocalDateTime baseTime = LocalDateTime.now();
        int index = 0;
        for (Long fileId : distinctIds) {
            getFile(fileId);
            FileLink link = FileLink.builder()
                    .fileId(fileId)
                    .targetType(normalized)
                    .targetId(targetId)
                    .createdAt(baseTime.plusNanos(index++))
                    .build();
            fileLinkRepository.save(link);
        }
    }

    @Transactional(readOnly = true)
    public List<FileAsset> getLinkedAssets(String targetType, Long targetId) {
        String normalized = normalizeTargetType(targetType);
        List<FileLink> links = fileLinkRepository.findAllByTargetTypeAndTargetIdOrderByCreatedAtAsc(normalized, targetId);
        if (links.isEmpty()) {
            return List.of();
        }
        List<Long> fileIds = links.stream()
                .map(FileLink::getFileId)
                .toList();
        Map<Long, FileAsset> assetMap = fileAssetRepository.findAllByFileIdIn(fileIds).stream()
                .collect(Collectors.toMap(FileAsset::getFileId, asset -> asset));

        // filter out stale links when asset missing
        boolean hasMissing = fileIds.stream().anyMatch(id -> !assetMap.containsKey(id));
        if (hasMissing) {
            fileLinkRepository.deleteAllByTarget(normalized, targetId);
            return List.of();
        }

        List<FileAsset> ordered = new ArrayList<>();
        for (Long fileId : fileIds) {
            FileAsset asset = assetMap.get(fileId);
            if (asset != null) {
                ordered.add(asset);
            }
        }
        return ordered;
    }

    @Transactional(readOnly = true)
    public FileAsset getFile(Long fileId) {
        return fileAssetRepository.findById(fileId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "파일을 찾을 수 없습니다."));
    }

    @Transactional(readOnly = true)
    public FileDownload download(Long fileId) {
        FileAsset asset = getFile(fileId);

        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(asset.getBucket())
                .key(asset.getObjectKey())
                .build();

        ResponseInputStream<GetObjectResponse> stream = s3Client.getObject(request);
        return new FileDownload(asset, stream);
    }

    @Transactional
    public void delete(Long fileId) {
        FileAsset asset = getFile(fileId);

        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(asset.getBucket())
                .key(asset.getObjectKey())
                .build();
        s3Client.deleteObject(request);

        fileAssetRepository.delete(asset);
    }

    public FileResponseDto toResponse(FileAsset asset) {
        return FileResponseDto.from(asset, buildPublicUrl(asset));
    }

    public String buildPublicUrl(FileAsset asset) {
        String baseUrl = awsProperties.getS3().getBaseUrl();
        if (StringUtils.isNotBlank(baseUrl)) {
            return String.format("%s/%s", stripTrailingSlash(baseUrl), asset.getObjectKey());
        }
        return String.format("https://%s.s3.%s.amazonaws.com/%s",
                asset.getBucket(),
                awsProperties.getRegion(),
                asset.getObjectKey());
    }

    private void validateBucket() {
        if (StringUtils.isBlank(awsProperties.getS3().getBucket())) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "S3 버킷이 설정되지 않았습니다.");
        }
    }

    private String buildObjectKey(String originalFilename) {
        String sanitized = extractFilename(originalFilename);
        String datePath = LocalDate.now().toString().replace('-', '/');
        String uuid = java.util.UUID.randomUUID().toString();
        return String.format(Locale.ROOT, "uploads/%s/%s-%s", datePath, uuid, sanitized);
    }

    private String extractFilename(String originalFilename) {
        if (StringUtils.isBlank(originalFilename)) {
            return "file";
        }
        String sanitized = originalFilename.replace("\\", "/");
        int index = sanitized.lastIndexOf('/');
        if (index >= 0 && index < sanitized.length() - 1) {
            return sanitized.substring(index + 1);
        }
        return sanitized;
    }

    private String normalizeTargetType(String targetType) {
        if (targetType == null) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "targetType은 필수입니다.");
        }
        return targetType.trim().toUpperCase(Locale.ROOT);
    }

    private String stripTrailingSlash(String value) {
        if (value == null) {
            return null;
        }
        if (value.endsWith("/")) {
            return value.substring(0, value.length() - 1);
        }
        return value;
    }
}
