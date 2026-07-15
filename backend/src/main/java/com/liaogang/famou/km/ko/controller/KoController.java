package com.liaogang.famou.km.ko.controller;

import com.liaogang.famou.km.common.Result;
import com.liaogang.famou.km.ko.dto.KoDetail;
import com.liaogang.famou.km.ko.dto.KoListItem;
import com.liaogang.famou.km.ko.dto.KoSearchResult;
import com.liaogang.famou.km.ko.model.KoEntity;
import com.liaogang.famou.km.ko.service.KoService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * KO 库 REST API 控制器（T202）。
 *
 * <p>端点：
 * <ul>
 *   <li>POST /api/ko - 创建</li>
 *   <li>GET /api/ko/{id} - 详情</li>
 *   <li>GET /api/ko - 列表（type/projectId/status 过滤 + 分页）</li>
 *   <li>GET /api/ko/search - 跨类搜索（OQ-4）</li>
 *   <li>PUT /api/ko/{id} - 更新</li>
 *   <li>DELETE /api/ko/{id} - 软删除</li>
 * </ul>
 *
 * <p>跨项目隔离：通过 {@code X-Project-Id} header 传递当前用户项目 ID
 */
@RestController
@RequestMapping("/api/ko")
@RequiredArgsConstructor
public class KoController {

    private final KoService koService;

    /**
     * 创建 KO
     * POST /api/ko
     */
    @PostMapping
    public Result<KoEntity> create(@RequestBody KoEntity ko) {
        return Result.ok(koService.createKo(ko));
    }

    /**
     * KO 详情
     * GET /api/ko/{id}
     */
    @GetMapping("/{id}")
    public Result<KoDetail> getById(
            @PathVariable String id,
            @RequestHeader(value = "X-Project-Id", required = false) String projectId) {
        return Result.ok(koService.getById(id, projectId));
    }

    /**
     * KO 列表（按 type/projectId/status 过滤 + 分页）
     * GET /api/ko?type=&projectId=&status=&page=1&size=20
     */
    @GetMapping
    public Result<Page<KoListItem>> list(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String projectId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.ok(koService.listKo(type, projectId, status, page, size));
    }

    /**
     * 跨类搜索（OQ-4：title + id + typeName 3 字段）
     * GET /api/ko/search?query=&types=CON,RUL
     */
    @GetMapping("/search")
    public Result<List<KoSearchResult>> search(
            @RequestParam String query,
            @RequestParam(required = false) List<String> types,
            @RequestHeader(value = "X-Project-Id", required = false) String projectId) {
        return Result.ok(koService.searchKo(query, types, projectId));
    }

    /**
     * 更新 KO
     * PUT /api/ko/{id}
     */
    @PutMapping("/{id}")
    public Result<KoEntity> update(
            @PathVariable String id,
            @RequestBody KoEntity update,
            @RequestHeader(value = "X-Project-Id", required = false) String projectId) {
        return Result.ok(koService.updateKo(id, update, projectId));
    }

    /**
     * 软删除 KO
     * DELETE /api/ko/{id}
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @PathVariable String id,
            @RequestHeader(value = "X-Project-Id", required = false) String projectId) {
        koService.softDelete(id, projectId);
        return Result.ok();
    }
}