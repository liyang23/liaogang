package com.liaogang.famou.km.role.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.liaogang.famou.km.role.model.RoleEntity;
import com.liaogang.famou.km.role.model.RolePermissionEntity;
import com.liaogang.famou.km.role.repository.RoleMapper;
import com.liaogang.famou.km.role.repository.RolePermissionMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 默认权限矩阵加载器（T205）。
 *
 * <p>Spring 启动后自动加载 seed/role-permissions.yaml → 5 预置角色 + 150 cells 权限矩阵
 * <p>幂等：role 表已存在则跳过，role_permission 已存在则跳过
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DefaultMatrixLoader {

    private final RoleMapper roleMapper;
    private final RolePermissionMapper rolePermissionMapper;

    /**
     * 启动后自动加载（@PostConstruct）
     */
    @PostConstruct
    public void load() {
        try {
            loadBuiltinRoles();
            loadDefaultPermissions();
            log.info("✓ 5 预置角色 + 150 cells 默认权限矩阵加载完成");
        } catch (Exception e) {
            log.error("✗ 默认权限矩阵加载失败", e);
        }
    }

    /**
     * 加载 5 预置角色（如已存在跳过）
     */
    public void loadBuiltinRoles() {
        List<Map<String, Object>> roles = readSeed();
        for (Map<String, Object> roleData : roles) {
            String code = (String) roleData.get("code");
            Long count = roleMapper.selectCount(new QueryWrapper<RoleEntity>().eq("code", code));
            if (count > 0) {
                log.debug("角色已存在，跳过: {}", code);
                continue;
            }
            RoleEntity role = RoleEntity.builder()
                .id(code)  // 预置角色 ID = code（ROLE-0001~0005）
                .code(code)
                .name((String) roleData.get("name"))
                .description((String) roleData.get("description"))
                .isBuiltin(roleData.get("is_builtin") != null ? 1 : 0)
                .isDeleted(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
            roleMapper.insert(role);
            log.info("✓ 预置角色已加载: {} - {}", code, role.getName());
        }
    }

    /**
     * 加载默认权限矩阵（如已存在跳过）
     */
    public void loadDefaultPermissions() {
        List<Map<String, Object>> roles = readSeed();
        for (Map<String, Object> roleData : roles) {
            String roleCode = (String) roleData.get("code");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> permissions = (List<Map<String, Object>>) roleData.get("permissions");
            if (permissions == null) continue;

            for (Map<String, Object> perm : permissions) {
                String menuId = (String) perm.get("menu_id");
                String operation = (String) perm.get("operation");
                Boolean allowed = (Boolean) perm.get("allowed");

                // 幂等检查
                Long count = rolePermissionMapper.selectCount(
                    new QueryWrapper<RolePermissionEntity>()
                        .eq("role_id", roleCode)
                        .eq("menu_id", menuId)
                        .eq("operation", operation)
                );
                if (count > 0) {
                    continue;
                }

                RolePermissionEntity entity = RolePermissionEntity.builder()
                    .roleId(roleCode)
                    .menuId(menuId)
                    .operation(operation)
                    .allowed(allowed != null ? allowed : false)
                    .createdAt(LocalDateTime.now())
                    .build();
                rolePermissionMapper.insert(entity);
            }
        }
    }

    /**
     * 读取 seed YAML
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> readSeed() {
        Yaml yaml = new Yaml();
        try (InputStream in = new ClassPathResource("seed/role-permissions.yaml").getInputStream()) {
            Map<String, Object> data = yaml.load(in);
            return (List<Map<String, Object>>) data.get("roles");
        } catch (Exception e) {
            log.error("读取 seed/role-permissions.yaml 失败", e);
            return new ArrayList<>();
        }
    }
}