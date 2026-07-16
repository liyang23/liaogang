package com.liaogang.famou.km.role.controller;

import com.liaogang.famou.km.common.Result;
import com.liaogang.famou.km.role.model.RoleEntity;
import com.liaogang.famou.km.role.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 角色 REST API 控制器（T205）。
 *
 * <p>端点：
 * <ul>
 *   <li>GET /api/role - 列表</li>
 *   <li>GET /api/role/{code} - 详情</li>
 *   <li>POST /api/role - 创建自定义角色</li>
 *   <li>DELETE /api/role/{code} - 删除（仅自定义角色）</li>
 * </ul>
 *
 * <p>权限校验（T206+ 实施）：@PreAuthorize 注解 + JWT 角色解析
 */
@RestController
@RequestMapping("/api/role")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    public Result<List<RoleEntity>> list() {
        return Result.ok(roleService.list());
    }

    @GetMapping("/{code}")
    public Result<RoleEntity> getByCode(@PathVariable String code) {
        return Result.ok(roleService.getByCode(code));
    }

    @PostMapping
    public Result<RoleEntity> create(@RequestBody Map<String, String> body) {
        return Result.ok(roleService.createCustomRole(body.get("name"), body.get("description")));
    }

    @DeleteMapping("/{code}")
    public Result<Void> delete(@PathVariable String code) {
        roleService.deleteRole(code);
        return Result.ok();
    }
}
