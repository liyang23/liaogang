package com.liaogang.famou.km.ko.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * KO 版本历史实体（v0.32 PRD §5.2.1.3 + §10.7.7.5）。
 *
 * <p>每次编辑触发新版本（Draft → Review → Approved → Published），原 KO 表的 `version` 字段更新为最新发布版本
 * <p>版本号格式 v{MAJOR}.{MINOR}.{PATCH}（§10.7.7.5）：MAJOR 大版本不兼容 / MINOR 向后兼容新功能 / PATCH 修复
 * <p>同一 KO 同时最多 1 个 in-flight 工作版本（OQ-12 状态机约束）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("ko_version")
public class KoVersionEntity {

    /** PK: 自增主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** FK → ko.id */
    private String koId;

    /** 本次版本号: v{MAJOR}.{MINOR}.{PATCH} */
    private String version;

    /** 本次版本标题 */
    private String title;

    /** 本次版本形式化定义 */
    private String definition;

    /** 本次版本状态: Draft/Review/Approved/Published */
    private String status;

    /** 创建人 sub（来自 JWT） */
    private String createdBy;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
