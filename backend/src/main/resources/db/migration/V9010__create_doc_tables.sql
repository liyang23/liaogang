-- U10 文档预览数据模型 (T313)
-- 与 V9009 后 (T310 dict 表 之后)

CREATE TABLE IF NOT EXISTS `km_doc` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `ko_id` VARCHAR(32) NOT NULL COMMENT '关联 km_ko.id (DOC 类型)',
    `file_name` VARCHAR(255) NOT NULL COMMENT '原始文件名',
    `file_type` VARCHAR(16) NOT NULL COMMENT 'pdf / docx / xlsx / pptx / txt / png / jpg',
    `file_size` BIGINT NOT NULL DEFAULT 0 COMMENT '字节',
    `minio_bucket` VARCHAR(64) NOT NULL COMMENT 'MinIO bucket 名 (如 km-doc)',
    `minio_object_key` VARCHAR(255) NOT NULL COMMENT 'MinIO object key (如 ko/{koId}/{fileName})',
    `minio_etag` VARCHAR(64) NULL COMMENT 'MinIO ETag (用于校验上传完整性)',
    `converted_pdf_key` VARCHAR(255) NULL COMMENT 'LibreOffice 转换后 PDF object key (DOCX/XLSX/PPTX 转换)',
    `preview_strategy` VARCHAR(32) NOT NULL COMMENT 'PDFJS_DIRECT / PDFJS_CONVERTED / TEXT_DIRECT / IMAGE_DIRECT / UNSUPPORTED',
    `uploaded_by` VARCHAR(64) NOT NULL COMMENT '上传人 sub',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_ko_id` (`ko_id`),
    KEY `idx_file_type` (`file_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文档 (MinIO 存储 + LibreOffice 转换)';
