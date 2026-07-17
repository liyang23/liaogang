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
 * 提示词快照实体 SNP（U8 / T305 / PRD v0.32 §5.2.4）
 *
 * <p>SNP = 装配方案版本，hash 命中时复用，否则新建。
 * ko_assembly_hash = SHA256(prm_id + prm_version + sorted(ko_ids) + sorted(ko_versions) + sorted(ko_field_values) + manual_subitems_hash + sorted(var_bindings))[:16]
 */
@Data
@NoArgsConstructor
@TableName("km_prompt_snapshot")
public class PromptSnapshotEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** ko_assembly_hash 16 位截断 */
    @TableField("hash")
    private String hash;

    @TableField("prm_id")
    private String prmId;

    @TableField("prm_version")
    private String prmVersion;

    @TableField("rendered_text_canonical")
    private String renderedTextCanonical;

    /** sorted(ko_ids[]) - JSON 数组字符串 */
    @TableField("ko_ids")
    private String koIds;

    /** sorted(ko_versions[]) - JSON 数组字符串 */
    @TableField("ko_versions")
    private String koVersions;

    /** sorted(ko_field_values[]) - JSON 数组字符串 */
    @TableField("ko_field_values")
    private String koFieldValues;

    /** manual_subitems_hash (Q-I4 §3 ManualSubItems array schema 派生的 hash) */
    @TableField("manual_subitems_hash")
    private String manualSubitemsHash;

    /** sorted(var_bindings[]) - JSON 数组字符串 */
    @TableField("var_bindings")
    private String varBindings;

    /** false = hash 命中复用; true = OQ-9 PAR 变更后陈旧快照 (由 StaleSnapshotJob 设置) */
    @TableField("stale")
    private Boolean stale;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
