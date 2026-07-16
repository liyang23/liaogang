package com.liaogang.famou.km.prompt.controller;

import com.liaogang.famou.km.common.Result;
import com.liaogang.famou.km.prompt.service.ComposerRenderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * PRM 组装器 REST API 控制器（T211）。
 *
 * <p>端点：
 * <ul>
 *   <li>POST /api/composer/render - 渲染 PRM 组装（OQ-16 装配数动态 + 字符数 + token 数）</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/composer")
@RequiredArgsConstructor
public class ComposerController {

    private final ComposerRenderService composerRenderService;

    /**
     * 渲染 PRM 组装
     * POST /api/composer/render
     * Body: { "templateId": "KO-PRM-0001", "context": { "selectedKOs": {...}, "varBindings": {...}, "manualSubItems": {...} } }
     */
    @PostMapping("/render")
    public Result<ComposerRenderService.RenderResult> render(@RequestBody Map<String, Object> body) {
        String templateId = (String) body.get("templateId");
        if (templateId == null || templateId.isEmpty()) {
            throw new com.liaogang.famou.km.common.BusinessException(40080, "templateId 不能为空");
        }
        // 解析 context（selectedKOs / varBindings / manualSubItems）
        ComposerRenderService.ComposeContext context = parseContext(body);
        return Result.ok(composerRenderService.render(templateId, context));
    }

    @SuppressWarnings("unchecked")
    private ComposerRenderService.ComposeContext parseContext(Map<String, Object> body) {
        ComposerRenderService.ComposeContext ctx = new ComposerRenderService.ComposeContext();
        Object ctxObj = body.get("context");
        if (!(ctxObj instanceof Map)) return ctx;
        Map<String, Object> ctxMap = (Map<String, Object>) ctxObj;

        Object sel = ctxMap.get("selectedKOs");
        if (sel instanceof Map) {
            for (Map.Entry<String, Object> e : ((Map<String, Object>) sel).entrySet()) {
                int idx = Integer.parseInt(e.getKey());
                if (e.getValue() instanceof java.util.List) {
                    java.util.List<String> list = (java.util.List<String>) e.getValue();
                    ctx.selectedKOs.put(idx, list.toArray(new String[0]));
                }
            }
        }

        Object vars = ctxMap.get("varBindings");
        if (vars instanceof Map) {
            for (Map.Entry<String, Object> e : ((Map<String, Object>) vars).entrySet()) {
                int idx = Integer.parseInt(e.getKey());
                if (e.getValue() instanceof Map) {
                    Map<String, String> bindings = (Map<String, String>) e.getValue();
                    ctx.varBindings.put(idx, bindings);
                }
            }
        }

        Object manual = ctxMap.get("manualSubItems");
        if (manual instanceof Map) {
            for (Map.Entry<String, Object> e : ((Map<String, Object>) manual).entrySet()) {
                int idx = Integer.parseInt(e.getKey());
                ctx.manualSubItems.put(idx, String.valueOf(e.getValue()));
            }
        }

        return ctx;
    }
}