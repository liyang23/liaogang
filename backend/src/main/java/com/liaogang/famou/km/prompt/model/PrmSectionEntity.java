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
 * PRM Section 实体（T208）。
 *
 * <p>v0.32 §5.2.4 Section 类型：
 * <ul>
 *   <li>FIXED：变量赋值型（含 {{var}} 占位）</li>
 *   <li>DYNAMIC：动态选择型（KO 模式 / 手动子项模式 / 变量绑定 PAR）</li>
 * </ul>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("prm_section")
public class PrmSectionEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** FK → prm_template.id */
    private String templateId;

    /** Section 序号（1-9 / 1-3 / 1-5）*/
    private Integer sectionIndex;

    /** Section 标题 */
    private String title;

    /** FIXED / DYNAMIC */
    private String sectionType;

    /** Section 内容模板（含 {{var}} / {{#each}} 等） */
    private String content;

    private LocalDateTime createdAt;
}
