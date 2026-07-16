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
 * 角色权限 cell 实体（T205）。
 *
 * <p>对应 v0.32 §4.1.2 默认权限矩阵
 * <p>矩阵规则：role_id × menu_id/ko_type × operation → allowed (true/false)
 * <p>T205 实施范围：6 KO 类型 × 5 操作 = 30 cells/角色 × 5 角色 = 150 cells
 * <p>U5 完整版：13 菜单 × 5 操作 × 5 角色 = 325 cells（含 KO 子类型缩进行）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("role_permission")
public class RolePermissionEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** FK → role.id */
    private String roleId;

    /** 菜单 ID 或 KO 类型（CON/RUL/PAR/SCH/PRM/DOC）*/
    private String menuId;

    /** 操作：查阅/新增/更新/删除/审核 */
    private String operation;

    /** 是否允许（true/false）*/
    private Boolean allowed;

    private LocalDateTime createdAt;
}
