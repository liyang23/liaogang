package com.liaogang.famou.km.test;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.errors.ErrorResponseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

/**
 * MinIO bucket 自动创建（it profile，集成测试用）。
 * <p>集成测试启动时确保 app.minio.bucket-name 存在；不存在则建；已存在跳过。
 * <p>幂等：bucket 已存在时不抛错。
 * <p>仅 test classpath 加载：通过 AutoConfiguration.imports 自动发现；main classpath 不可见。
 */
@Slf4j
@TestConfiguration
@Profile("it")
public class MinioBucketInitializer {

    @Value("${app.minio.endpoint}")
    private String endpoint;

    @Value("${app.minio.access-key}")
    private String accessKey;

    @Value("${app.minio.secret-key}")
    private String secretKey;

    @Value("${app.minio.bucket-name}")
    private String bucketName;

    @Bean
    public ApplicationRunner minioBucketInitRunner() {
        return args -> {
            try {
                MinioClient client = MinioClient.builder()
                    .endpoint(endpoint)
                    .credentials(accessKey, secretKey)
                    .build();

                boolean exists;
                try {
                    exists = client.bucketExists(
                        BucketExistsArgs.builder().bucket(bucketName).build());
                } catch (ErrorResponseException e) {
                    // NoSuchBucket 等错误时视为不存在
                    log.debug("[it] bucketExists 异常视为不存在: {}", e.getMessage());
                    exists = false;
                }

                if (!exists) {
                    client.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                    log.info("[it] MinIO bucket 已创建: {}", bucketName);
                } else {
                    log.info("[it] MinIO bucket 已存在: {}", bucketName);
                }
            } catch (Exception e) {
                log.error("[it] MinIO bucket 初始化失败: bucket={}, err={}", bucketName, e.getMessage());
                throw new IllegalStateException("MinIO bucket 初始化失败", e);
            }
        };
    }
}
