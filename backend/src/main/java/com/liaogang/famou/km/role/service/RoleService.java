package com.liaogang.famou.km.role.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.liaogang.famou.km.common.BusinessException;
import com.liaogang.famou.km.role.model.RoleEntity;
import com.liaogang.famou.km.role.model.UserRoleEntity;
import com.liaogang.famou.km.role.repository.RoleMapper;
import com.liaogang.famou.km.role.repository.UserRoleMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 角色业务服务（T205）。
 *
 * <p>5 预置角色 + 自定义角色 CRUD
 * <p>预置角色不可删除（v0.32 §4.1.1）
 * <p>自定义角色可创建/编辑/删除（未被用户引用时可删）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleMapper roleMapper;
    private final UserRoleMapper userRoleMapper;

    /**
     * 查询所有未删除角色
     */
    public List<RoleEntity> list() {
        return roleMapper.selectList(
            new QueryWrapper<RoleEntity>().eq("is_deleted", 0)
                .orderByAsc("code")
        );
    }

    /**
     * 按 code 查询
     */
    public RoleEntity getByCode(String code) {
        RoleEntity role = roleMapper.selectOne(
            new QueryWrapper<RoleEntity>()
                .eq("code", code)
                .eq("is_deleted", 0)
        );
        if (role == null) {
            throw new BusinessException(40410, "角色不存在: " + code);
        }
        return role;
    }

    /**
     * 创建自定义角色（非预置）
     */
    @Transactional
    public RoleEntity createCustomRole(String name, String description) {
        if (name == null || name.trim().isEmpty()) {
            throw new BusinessException(40050, "角色名称不能为空");
        }
        String code = "ROLE-CUSTOM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        RoleEntity role = RoleEntity.builder()
            .id(code)
            .code(code)
            .name(name)
            .description(description)
            .isBuiltin(0)  // 自定义角色，非预置
            .isDeleted(0)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        roleMapper.insert(role);
        log.info("自定义角色创建: {}", code);
        return role;
    }

    /**
     * 删除角色（预置不可删 / 被引用不可删）
     */
    @Transactional
    public void deleteRole(String code) {
        RoleEntity role = getByCode(code);
        // 预置角色不可删（OQ-20 + v0.32 §4.1.1）
        if (Integer.valueOf(1).equals(role.getIsBuiltin())) {
            throw new BusinessException(40051, "预置角色不可删除（v0.32 §4.1.1）");
        }
        // 检查是否被用户引用
        Long userCount = userRoleMapper.selectCount(
            new QueryWrapper<UserRoleEntity>().eq("role_id", code)
        );
        if (userCount > 0) {
            throw new BusinessException(40052, "角色被 " + userCount + " 个用户引用，不可删除");
        }
        // 软删除
        RoleEntity update = new RoleEntity();
        update.setId(code);
        update.setIsDeleted(1);
        update.setUpdatedAt(LocalDateTime.now());
        roleMapper.updateById(update);
        log.info("角色软删除: {}", code);
    }
}
