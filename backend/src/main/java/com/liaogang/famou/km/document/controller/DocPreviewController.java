package com.liaogang.famou.km.document.controller;

import com.liaogang.famou.km.common.Result;
import com.liaogang.famou.km.document.service.DocPreviewService;
import com.liaogang.famou.km.document.service.MinIOService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 文档预览 REST API 控制器 (U10 / T313)
 *
 * <p>API:
 * <ul>
 *   <li>GET /api/doc/{koId}/preview - 6 格式分发预览 (PDF / DOCX / XLSX / PPTX / TXT / 图片)
 *   <li>GET /api/doc/{koId}/file?token=xxx - 签名 URL 访问实际文件 (30 分钟过期)
 * </ul>
 */
@RestController
@RequestMapping("/api/doc")
@RequiredArgsConstructor
public class DocPreviewController {

    private final DocPreviewService docPreviewService;
    private final MinIOService minioService;

    @GetMapping("/{koId}/preview")
    public Result<DocPreviewService.PreviewResult> preview(@PathVariable("koId") String koId) {
        // placeholder: 实际从 db 查 DocEntity
        com.liaogang.famou.km.document.model.DocEntity doc = new com.liaogang.famou.km.document.model.DocEntity();
        doc.setKoId(koId);
        doc.setFileType("pdf");
        doc.setPreviewStrategy("PDFJS_DIRECT");
        doc.setMinioBucket("km-doc");
        doc.setMinioObjectKey("ko/" + koId + "/document.pdf");
        return Result.ok(docPreviewService.getPreview(doc));
    }

    @GetMapping("/{koId}/file")
    public Result<String> file(
            @PathVariable("koId") String koId,
            @RequestParam("token") String token) {
        // placeholder: 校验 token + 返回文件 URL
        boolean expired = minioService.isTokenExpired(token);
        if (expired) {
            return Result.fail(40140, "Token 已过期 (30 分钟), 请重新打开");
        }
        return Result.ok("file content placeholder (实际由 MinIO 签名 URL 提供)");
    }
}
