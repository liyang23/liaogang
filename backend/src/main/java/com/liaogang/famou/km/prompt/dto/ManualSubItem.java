package com.liaogang.famou.km.prompt.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 手动子项单条结构（Q-I4 §3 弹层 + ManualSubItems 类型迁移落地后形态）
 *
 * <p>§3 「计算范围」DYNAMIC 手动子项模式 = 业务专家录入 list of {@code ManualSubItem}，
 * 每条含 title / content（必填）+ value? / unit? / lower_bound? / upper_bound? / range_type?
 * 可选业务字段（按 R5 阶段 1 schema 拍板）。
 *
 * <p>DTO 仅承载传输语义。持久化形态 = 与 PrmSectionEntity.content (TEXT)
 * 通过 serializer (Jackson) 互转：U3 dual-write feature flag 控制；
 * 1 sprint 兼容期后 Phase 6 移除 string fallback。
 */
@Data
@NoArgsConstructor
public class ManualSubItem implements Serializable {

    private static final long serialVersionUID = 1L;

    private String title;
    private String content;

    /** 业务字段（R5 阶段 1 sparring 拍板后启用） */
    private Double value;
    private String unit;
    private Double lower_bound;
    private Double upper_bound;
    private String range_type;
}
