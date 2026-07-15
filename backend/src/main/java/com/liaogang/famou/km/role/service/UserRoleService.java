package com.liaogang.famou.km.role.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.liaogang.famou.km.common.BusinessException;
import com.liaogang.famou.km.role.event.RoleChangeEvent;
import com.liaogang.famou.km.role.model.RoleEntity;
import com.liaogang.famou.km.role.model.UserRoleEntity;
import com.liaogang.famou.km.role.repository.RoleMapper;
import com.liaogang.famou.km.role.repository.UserRoleMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 用户角色服务（T207）。
 *
 * <p>分配 / 移除用户角色
 * <p>发布 RoleChangeEvent → RoleChangeAuditService 写 USER_ROLE_CHANGE 审计
 * <p>OQ-12：旧 session 保持原角色（JWT 自包含 role claim）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserRoleService {

    private final UserRoleMapper userRoleMapper;
    private final RoleMapper roleMapper;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 分配角色给用户
     */
    @Transactional
    public void assignRole(String userSub, String roleCode, String operatorSub) {
        validateRole(roleCode);
        // 幂等：已存在跳过
        Long count = userRoleMapper.selectCount(
            new QueryWrapper<UserRoleEntity>()
                .eq("user_sub", userSub)
                .eq("role_id", roleCode)
        );
        if (count > 0) {
            log.debug("用户已拥有该角色，跳过: user={}, role={}", userSub, roleCode);
            return;
        }
        UserRoleEntity userRole = UserRoleEntity.builder()
            .userSub(userSub)
            .roleId(roleCode)
            .assignedBy(operatorSub)
            .assignedAt(LocalDateTime.now())
            .createdAt(LocalDateTime.now())
            .build();
        userRoleMapper.insert(userRole);

        // 发布事件 → RoleChangeAuditService 写审计
        eventPublisher.publishEvent(new RoleChangeEvent(
            this, userSub, roleCode,
            RoleChangeEvent.ChangeType.ASSIGN,
            operatorSub
        ));
        log.info("✓ 角色分配: user={}, role={}, operator={}", userSub, roleCode, operatorSub);
    }

    /**
     * 移除用户角色
     */
    @Transactional
    public void removeRole(String userSub, String roleCode, String operatorSub) {
        int deleted = userRoleMapper.delete(
            new QueryWrapper<UserRoleEntity>()
                .eq("user_sub", userSub)
                .eq("role_id", roleCode)
        );
        if (deleted == 0) {
            throw new BusinessException(40060, "用户未拥有该角色");
        }
        eventPublisher.publishEvent(new RoleChangeEvent(
            this, userSub, roleCode,
            RoleChangeEvent.ChangeType.REMOVE,
            operatorSub
        ));
        log.info("✓ 角色移除: user={}, role={}, operator={}", userSub, roleCode, operatorSub);
    }

    private void validateRole(String roleCode) {
        RoleEntity role = roleMapper.selectById(roleCode);
        if (role == null || Integer.valueOf(1).equals(role.getIsDeleted())) {
            throw new BusinessException(40411, "角色不存在: " + roleCode);
        }
    }
}
