package com.liaogang.famou.km.document.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 文档实体 (U10 / T313)
 *
 * <p>6 预览策略: PDFJS_DIRECT / PDFJS_CONVERTED / TEXT_DIRECT / IMAGE_DIRECT / UNSUPPORTED
 */
@Data
@NoArgsConstructor
@TableName("km_doc")
public class DocEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("ko_id")
    private String koId;

    @TableField("file_name")
    private String fileName;

    @TableField("file_type")
    private String fileType;

    @TableField("file_size")
    private Long fileSize;

    @TableField("minio_bucket")
    private String minioBucket;

    @TableField("minio_object_key")
    private String minioObjectKey;

    @TableField("minio_etag")
    private String minioEtag;

    @TableField("converted_pdf_key")
    private String convertedPdfKey;

    @TableField("preview_strategy")
    private String previewStrategy;

    @TableField("uploaded_by")
    private String uploadedBy;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
