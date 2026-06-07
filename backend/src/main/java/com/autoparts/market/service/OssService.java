package com.autoparts.market.service;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.PutObjectRequest;
import com.autoparts.market.config.OssProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OssService {

    private final OssProperties ossProperties;

    public String upload(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        String ext = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            ext = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String objectName = "products/" + UUID.randomUUID().toString().replace("-", "") + ext;

        OSS ossClient = new OSSClientBuilder().build(
                ossProperties.getEndpoint(),
                ossProperties.getAccessKeyId(),
                ossProperties.getAccessKeySecret());

        try (InputStream inputStream = file.getInputStream()) {
            ossClient.putObject(new PutObjectRequest(ossProperties.getBucketName(), objectName, inputStream));
            return "https://" + ossProperties.getBucketName() + "." + ossProperties.getEndpoint() + "/" + objectName;
        } catch (Exception e) {
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        } finally {
            ossClient.shutdown();
        }
    }
}
