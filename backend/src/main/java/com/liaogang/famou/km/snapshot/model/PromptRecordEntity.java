package com.liaogang.famou.km.snapshot.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 提示词渲染记录实体 PRP（U8 / T305 / PRD v0.32 §5.2.4）
 *
 * <p>PRP = 每次渲染一条，与 SNP 是双层关系（PRP 引用 SNP 的 hash）。
 */
@Data
@NoArgsConstructor
@TableName("km_prompt_record")
public class PromptRecordEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("snapshot_hash")
    private String snapshotHash;

    @TableField("rendered_text")
    private String renderedText;

    @TableField("render_time")
    private LocalDateTime renderTime;

    /** OQ-16 字符数 + Token 数 */
    @TableField("char_count")
    private Integer charCount;

    @TableField("token_count")
    private Integer tokenCount;

    @TableField("user_id")
    private String userId;

    /** OQ-5 强制渲染理由 (≥10 字符) */
    @TableField("reason")
    private String reason;

    /** true = 强制渲染 (绕过 stale 检查) */
    @TableField("force_rendered")
    private Boolean forceRendered;

    @TableField("created_at")
    private LocalDateTime createdAt;
}
