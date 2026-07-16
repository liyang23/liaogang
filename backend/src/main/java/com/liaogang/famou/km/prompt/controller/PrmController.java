package com.liaogang.famou.km.prompt.controller;

import com.liaogang.famou.km.common.Result;
import com.liaogang.famou.km.prompt.model.PrmSectionEntity;
import com.liaogang.famou.km.prompt.model.PrmTemplateEntity;
import com.liaogang.famou.km.prompt.service.PrmService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * PRM 模板 REST API 控制器（T208）。
 *
 * <p>端点：
 * <ul>
 *   <li>GET /api/prm - 模板列表</li>
 *   <li>GET /api/prm/{id} - 模板详情（含 sections）</li>
 *   <li>GET /api/prm/{id}/sections - 模板 sections</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/prm")
@RequiredArgsConstructor
public class PrmController {

    private final PrmService prmService;

    @GetMapping
    public Result<List<PrmTemplateEntity>> list() {
        // TODO T208+ 实际查询：prmTemplateMapper.selectList(null)
        return Result.ok(List.of());
    }

    @GetMapping("/{id}")
    public Result<Map<String, Object>> getById(@PathVariable String id) {
        PrmTemplateEntity template = prmService.getTemplate(id);
        List<PrmSectionEntity> sections = prmService.getSections(id);
        return Result.ok(Map.of("template", template, "sections", sections));
    }

    @GetMapping("/{id}/sections")
    public Result<List<PrmSectionEntity>> sections(@PathVariable String id) {
        return Result.ok(prmService.getSections(id));
    }
}
