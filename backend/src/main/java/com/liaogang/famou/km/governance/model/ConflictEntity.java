package com.liaogang.famou.km.governance.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 冲突实体（U7 知识治理 / PRD §5.2.3.1）
 *
 * <p>6 C 类冲突 + 1 H 类健康 + 13 项待治理项，冲突指纹算法（§5.2.3.1）：
 * MD5(ko_a_id + ko_b_id + conflict_type + scope_key + field_key)[:12]
 * 命中指纹 → 沿用旧 id，不命中 → 新建。
 *
 * <p>引用：U7 T301 / T302 / T304 / U8 T305（陈旧快照扫描）/ U9 T308（USER_CONFLICT_ARBITRATE 审计）
 */
@Data
@NoArgsConstructor
@TableName("km_conflict")
public class ConflictEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 冲突指纹 MD5(ko_a_id + ko_b_id + conflict_type + scope_key + field_key)[:12] */
    @TableField("fingerprint")
    private String fingerprint;

    /** C1 字段值冲突 / C2 模板不一致 / C3 字段类型冲突 / C4 引用过期 / C5 时序冲突 / C6 命名歧义 / H2 数据漂移 */
    @TableField("conflict_type")
    private String conflictType;

    @TableField("ko_a_id")
    private String koAId;

    @TableField("ko_b_id")
    private String koBId;

    @TableField("scope_key")
    private String scopeKey;

    @TableField("field_key")
    private String fieldKey;

    /** pending / reviewing / resolved / auto_resolved */
    @TableField("status")
    private String status;

    @TableField("confidence")
    private Double confidence;

    @TableField("llm_suggestion")
    private String llmSuggestion;

    @TableField("llm_rationale")
    private String llmRationale;

    /** OQ-8 仲裁快路径：检测时为 Draft / 管理员点击仲裁后系统自动 Review→Approved→Published */
    @TableField("resolution_state")
    private String resolutionState;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;

    @TableField("resolved_at")
    private LocalDateTime resolvedAt;
}
