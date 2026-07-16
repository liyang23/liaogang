package com.liaogang.famou.km.role.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;

/**
 * 角色变更事件（T207）。
 *
 * <p>触发场景：
 * <ul>
 *   <li>管理员给用户分配新角色</li>
 *   <li>管理员移除用户角色</li>
 *   <li>角色权限矩阵更新（影响所有持有该角色的用户）</li>
 * </ul>
 *
 * <p>OQ-12 角色变更下次登录生效：本事件仅记录审计，不主动失效旧 JWT
 */
@Getter
public class RoleChangeEvent extends ApplicationEvent {

    /** 变更类型：ASSIGN（分配）/ REMOVE（移除）/ MATRIX_UPDATE（权限矩阵更新） */
    public enum ChangeType {
        ASSIGN, REMOVE, MATRIX_UPDATE
    }

    /** 受影响用户 sub */
    private final String userSub;

    /** 角色 ID */
    private final String roleId;

    /** 变更类型 */
    private final ChangeType changeType;

    /** 变更时间（即下次登录生效的 effective_at） */
    private final LocalDateTime effectiveAt;

    /** 操作人 sub */
    private final String operatorSub;

    public RoleChangeEvent(Object source, String userSub, String roleId,
                          ChangeType changeType, String operatorSub) {
        super(source);
        this.userSub = userSub;
        this.roleId = roleId;
        this.changeType = changeType;
        this.effectiveAt = LocalDateTime.now();
        this.operatorSub = operatorSub;
    }
}
