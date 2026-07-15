package com.liaogang.famou.km.role.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 角色实体（T205）。
 *
 * <p>对应 v0.32 §4.1.1 5 预置角色 + 自定义角色
 * <p>role 表 V9002 已建（T201 migration 顺带建；T205 复用）
 * <p>is_builtin=true 表示预置角色（不可删除，OQ-20）
 * <p>is_deleted 软删除（true 表示已删除，逻辑过滤）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("role")
public class RoleEntity {

    /** PK: ROLE-0001~ROLE-0005（预置）/ ROLE-XXXX（自定义 UUID 后缀）*/
    @TableId(type = IdType.INPUT)
    private String id;

    /** 角色编码（业务唯一，如 'ROLE-0001'）*/
    private String code;

    /** 角色名称（如 '系统管理员'）*/
    private String name;

    /** 角色描述 */
    private String description;

    /** 是否预置角色（true 不可删除）*/
    private Integer isBuiltin;

    /** 软删除标记 */
    private Integer isDeleted;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
