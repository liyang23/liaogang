package com.liaogang.famou.km.project.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 项目实体 (U9 / T309 / OQ-1 + OQ-7)
 *
 * <p>4 项目 (PROJ-0001~0004) + status active/archived 切换 + OQ-7 归档仅冻结 KO 不影响 PRP/SNP。
 * 已在 V9002__create_ko_tables.sql 建表; V9008 增量增 archived_at / archived_by 字段。
 */
@Data
@NoArgsConstructor
@TableName("project")
public class ProjectEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    @TableField("code")
    private String code;

    @TableField("name")
    private String name;

    @TableField("description")
    private String description;

    @TableField("organization_code")
    private String organizationCode;

    @TableField("status")
    private String status;

    @TableField("is_deleted")
    private Integer isDeleted;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;

    @TableField("archived_at")
    private LocalDateTime archivedAt;

    @TableField("archived_by")
    private String archivedBy;
}
