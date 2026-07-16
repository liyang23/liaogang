package com.liaogang.famou.km.ko.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * KO 详情 DTO（T202）。
 *
 * <p>包含全部字段 + 关联引用列表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KoDetail {

    private String id;
    private String type;
    private String typeName;
    private String title;
    private String code;
    private String projectId;
    private String definition;
    private String effect;
    private String level;
    private String organization;
    private String status;
    private String version;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /** 引用关系列表（DEPENDENCY/REFERENCE/CONFLICT） */
    private List<KoReference> references;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KoReference {
        private Long id;
        private String targetKoId;
        private String relationType;
        private String description;
    }
}