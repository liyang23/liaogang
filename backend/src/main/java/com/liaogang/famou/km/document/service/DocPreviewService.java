package com.liaogang.famou.km.document.service;

import com.liaogang.famou.km.document.model.DocEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 文档预览服务 (U10 / T313)
 *
 * <p>6 格式分发: PDF / DOCX / XLSX / PPTX / TXT / PNG / JPG
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocPreviewService {

    private final MinIOService minioService;

    /**
     * 返回预览策略 + 签名 URL
     */
    public PreviewResult getPreview(DocEntity doc) {
        String url = minioService.generatePresignedUrl(doc.getMinioBucket(), doc.getMinioObjectKey());
        PreviewResult result = new PreviewResult();
        result.setUrl(url);
        result.setStrategy(doc.getPreviewStrategy());
        return result;
    }

    public static class PreviewResult {
        private String url;
        private String strategy;

        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
        public String getStrategy() { return strategy; }
        public void setStrategy(String strategy) { this.strategy = strategy; }
    }
}
