package com.liaogang.famou.km.ko.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * KO 列表项 DTO（T202）。
 *
 * <p>6 类型 KO 列表通用字段；详情用 {@link KoDetail}
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KoListItem {

    /** KO ID: KO-{TYPE}-{NNNN} */
    private String id;

    /** KO 类型: CON/RUL/PAR/SCH/PRM/DOC */
    private String type;

    /** 类型名称（中文） */
    private String typeName;

    /** KO 标题 */
    private String title;

    /** 当前版本: v{MAJOR}.{MINOR}.{PATCH} */
    private String version;

    /** 状态: Draft/Review/Approved/Published/Active */
    private String status;

    /** 所属项目 ID */
    private String projectId;

    /** 最后更新时间 */
    private LocalDateTime updatedAt;
}