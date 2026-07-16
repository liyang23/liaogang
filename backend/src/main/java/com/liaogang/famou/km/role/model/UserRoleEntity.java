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
 * 用户角色关联实体（T205）。
 *
 * <p>对应 v0.32 §4.1.3 用户角色分配
 * <p>一个用户可以有多角色（ROLE-0001 + ROLE-0002 等）
 * <p>OQ-12 角色变更下次登录生效：旧 JWT 缓存不主动失效
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("user_role")
public class UserRoleEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户 sub（来自 JWT） */
    private String userSub;

    /** FK → role.id */
    private String roleId;

    /** 分配人 sub */
    private String assignedBy;

    private LocalDateTime assignedAt;

    private LocalDateTime createdAt;
}
