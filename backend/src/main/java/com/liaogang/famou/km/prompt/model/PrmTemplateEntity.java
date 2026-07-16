package com.liaogang.famou.km.prompt.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * PRM 提示词模板实体（T208）。
 *
 * <p>v0.32 §5.2.4 提示词系统：3 预置 PRM 模板
 * <ul>
 *   <li>KO-PRM-0001：大窑湾统筹优化（9 段）</li>
 *   <li>KO-PRM-0002：堆场计划优化（3 段）</li>
 *   <li>KO-PRM-0003：泊位分配算法（5 段）</li>
 * </ul>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("prm_template")
public class PrmTemplateEntity {

    @TableId(type = IdType.INPUT)
    private String id;

    /** 模板名称 */
    private String name;

    /** 模板描述 */
    private String description;

    /** 版本 */
    private String version;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
