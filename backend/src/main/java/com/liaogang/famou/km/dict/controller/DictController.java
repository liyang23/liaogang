package com.liaogang.famou.km.dict.controller;

import com.liaogang.famou.km.common.Result;
import com.liaogang.famou.km.dict.model.DictEntity;
import com.liaogang.famou.km.dict.model.UnitEntity;
import com.liaogang.famou.km.dict.service.DictService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * U9 字典管理 REST API 控制器 (T310)
 *
 * <p>API：
 * <ul>
 *   <li>GET /api/dict?type=xxx — 按 dict_type 查询
 *   <li>GET /api/dict — 全部 active 字典
 *   <li>GET /api/dict/unit — 9 预制量纲列表
 *   <li>POST /api/dict/{id}/soft-delete — 软删除
 *   <li>POST /api/dict/{id}/hard-delete — 硬删除 (引用完整性校验)
 *   <li>POST /api/dict/{id}/undelete — 恢复
 * </ul>
 */
@RestController
@RequestMapping("/api/dict")
@RequiredArgsConstructor
public class DictController {

    private final DictService dictService;

    @GetMapping
    public Result<List<DictEntity>> list(
            @RequestParam(value = "type", required = false) String type) {
        if (type != null) {
            return Result.ok(dictService.listByDictType(type));
        }
        return Result.ok(dictService.listAllActive());
    }

    @GetMapping("/unit")
    public Result<List<UnitEntity>> listUnits() {
        return Result.ok(dictService.listAllUnits());
    }

    @PostMapping("/{id}/soft-delete")
    public Result<String> softDelete(@PathVariable("id") Long id) {
        boolean ok = dictService.softDeleteDict(id);
        return ok ? Result.ok("soft-deleted: " + id) : Result.fail("dict not found: " + id);
    }

    @PostMapping("/{id}/hard-delete")
    public Result<String> hardDelete(@PathVariable("id") Long id) {
        try {
            boolean ok = dictService.hardDeleteDict(id);
            return ok ? Result.ok("hard-deleted: " + id) : Result.fail("dict not found: " + id);
        } catch (IllegalStateException e) {
            return Result.fail(e.getMessage());
        }
    }

    @PostMapping("/{id}/undelete")
    public Result<String> undelete(@PathVariable("id") Long id) {
        boolean ok = dictService.undeleteDict(id);
        return ok ? Result.ok("undeleted: " + id) : Result.fail("dict not found: " + id);
    }
}
