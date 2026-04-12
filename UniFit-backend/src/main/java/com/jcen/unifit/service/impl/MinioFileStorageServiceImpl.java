package com.jcen.unifit.service.impl;

import com.jcen.unifit.common.ErrorCode;
import com.jcen.unifit.config.MinioProperties;
import com.jcen.unifit.exception.BusinessException;
import com.jcen.unifit.service.FileStorageService;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.UUID;

@Service
public class MinioFileStorageServiceImpl implements FileStorageService {

    @Resource
    private MinioClient minioClient;

    @Resource
    private MinioProperties minioProperties;

    @Override
    public String upload(MultipartFile file, String bizType) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "上传文件不能为空");
        }
        String bucket = minioProperties.getBucket();
        if (StringUtils.isBlank(bucket)) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "MinIO bucket 未配置");
        }
        String cleanBizType = StringUtils.defaultIfBlank(bizType, "exercise").replaceAll("[^a-zA-Z0-9_-]", "");
        String ext = getExtension(file.getOriginalFilename());
        String objectName = cleanBizType + "/" + LocalDate.now() + "/" + UUID.randomUUID().toString().replace("-", "")
                + (StringUtils.isBlank(ext) ? "" : ("." + ext));

        try (InputStream inputStream = file.getInputStream()) {
            ensureBucket(bucket);
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectName)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文件上传失败: " + e.getMessage());
        }

        return buildFileUrl(bucket, objectName);
    }

    private void ensureBucket(String bucket) throws Exception {
        boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
        if (!found) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
        }
    }

    private String buildFileUrl(String bucket, String objectName) {
        String endpoint = StringUtils.defaultIfBlank(minioProperties.getPublicEndpoint(), minioProperties.getEndpoint());
        if (StringUtils.isBlank(endpoint)) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "MinIO endpoint 未配置");
        }
        String normalizedEndpoint = endpoint.endsWith("/") ? endpoint.substring(0, endpoint.length() - 1) : endpoint;
        return normalizedEndpoint + "/" + bucket + "/" + objectName;
    }

    private String getExtension(String filename) {
        if (StringUtils.isBlank(filename) || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1);
    }
}
