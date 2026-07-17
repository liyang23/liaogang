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
 * LLM 配额实体（U7 / T301 / T302）
 *
 * <p>平台级 + 用户级双重限制（OQ-9 配比 80/20）：
 * - 平台级：每日 0 点 Redis INCR 重置
 * - 用户级：每日 0 点 Redis INCR 重置
 *
 * <p>与 Sprint 1 已落地的 LlmQuotaService 配合使用（LlmQuotaService 已经在 llm/ 包落地）。
 * 本 entity 为持久化层（数据库表层 quota_used 记录），LlmQuotaService 走 Redis Lua 原子脚本。
 */
@Data
@NoArgsConstructor
@TableName("km_llm_quota")
public class LlmQuotaEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 平台 / 用户 */
    @TableField("scope")
    private String scope;

    /** 平台级 = "platform"; 用户级 = "user:{user_id}" */
    @TableField("scope_key")
    private String scopeKey;

    /** 配额日期 (yyyy-MM-dd) */
    @TableField("quota_date")
    private String quotaDate;

    /** 已用次数（平台级 / 用户级） */
    @TableField("used_count")
    private Integer usedCount;

    /** 配额上限（平台级 = 100/天, 用户级 = 5/天） */
    @TableField("limit_count")
    private Integer limitCount;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
