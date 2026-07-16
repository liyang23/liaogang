package com.liaogang.famou.km.ko.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * KO 跨类搜索结果 DTO（T202，OQ-4）。
 *
 * <p>OQ-4 验收：跨类搜索按 title + id + typeName 3 字段匹配
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KoSearchResult {

    private String id;
    private String type;
    private String typeName;
    private String title;
    private String projectId;

    /** 匹配字段（title / id / typeName），用于前端高亮 */
    private String matchedField;
}