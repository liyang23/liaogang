package com.liaogang.famou.km.prompt.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.liaogang.famou.km.common.BusinessException;
import com.liaogang.famou.km.prompt.dto.ManualSubItem;
import com.liaogang.famou.km.prompt.model.PrmSectionEntity;
import com.liaogang.famou.km.prompt.model.PrmTemplateEntity;
import com.liaogang.famou.km.prompt.repository.PrmSectionMapper;
import com.liaogang.famou.km.prompt.repository.PrmTemplateMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * PRM 组装渲染服务（T211）。
 *
 * <p>核心能力：
 * <ul>
 *   <li>PRP 实际装配数动态计算（OQ-16 = selectedKOs.length + varBindings.size + manualSubItems.size）</li>
 *   <li>字符数 + token 数估算（g(M) 函数，2 chars/token 保守）</li>
 *   <li>组装渲染（TemplateEngine 替换 + sections 拼装）</li>
 *   <li>Handlebars 语法错误检测（OQ-15 配套）</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ComposerRenderService {

    private final PrmTemplateMapper prmTemplateMapper;
    private final PrmSectionMapper prmSectionMapper;
    private final TemplateEngine templateEngine;

    /** U3 dual-write feature flag: 控制 array schema 启用比例 (0% → 100%) */
    @Value("${prompt.manualSubItems.arraySchemaRation:0.0}")
    private double arraySchemaRatio;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /** 渲染结果（含 OQ-16 装配数 + 字符数） */
    public static class RenderResult {
        public String rendered;        // 渲染后内容
        public int charCount;           // 字符数
        public int tokenCount;          // token 数（2 chars/token 保守）
        public int assemblyCount;       // OQ-16 装配数
        public int sectionCount;        // Section 数
    }

    /** 装配上下文 */
    public static class ComposeContext {
        public Map<Integer, String[]> selectedKOs = new HashMap<>();      // sectionIndex → koIds
        public Map<Integer, Map<String, String>> varBindings = new HashMap<>();  // sectionIndex → { varKey → koId }
        /** U3 阶段支持 union 形态: String (旧) 或 List<ManualSubItem> (新) */
        public Map<Integer, Object> manualSubItems = new HashMap<>();       // sectionIndex → content | items
    }

    /**
     * 渲染 PRM 组装结果
     */
    public RenderResult render(String templateId, ComposeContext context) {
        // 1. 加载模板
        PrmTemplateEntity template = prmTemplateMapper.selectById(templateId);
        if (template == null) {
            throw new BusinessException(40420, "PRM 模板不存在: " + templateId);
        }
        List<PrmSectionEntity> sections = prmSectionMapper.selectByTemplateId(templateId);
        if (sections == null) sections = List.of();

        // 2. 合并每个 section 的 content（应用 OQ-16 上下文）
        StringBuilder fullContent = new StringBuilder();
        for (PrmSectionEntity section : sections) {
            String sectionContent = mergeSectionContext(section, context);
            fullContent.append("## ").append(section.getTitle()).append("\n\n")
                       .append(sectionContent).append("\n\n");
        }

        // 3. 计算 OQ-16 实际装配数
        int assemblyCount = computeAssemblyCount(context);
        int charCount = fullContent.length();
        int tokenCount = (int) Math.ceil(charCount / 2.0);

        // 4. 构造结果
        RenderResult result = new RenderResult();
        result.rendered = fullContent.toString();
        result.charCount = charCount;
        result.tokenCount = tokenCount;
        result.assemblyCount = assemblyCount;
        result.sectionCount = sections.size();
        return result;
    }

    /**
     * 合并 section content + 上下文（OQ-16 应用 varBindings / manualSubItems / selectedKOs）
     *
     * <p>U3 阶段：manualSubItems 既支持旧 string 形态（dual-write 兼容），
     * 也支持新 List<ManualSubItem> 形态（array schema enabled 时）。
     * 形态识别由 Object 类型决定：List → 逐条拼接 + 业务字段联动；String → content.replace 整段。
     */
    private String mergeSectionContext(PrmSectionEntity section, ComposeContext context) {
        String content = section.getContent();
        int idx = section.getSectionIndex();

        // 应用 varBindings（替换 {{var}} 为变量值）
        Map<String, String> bindings = context.varBindings.get(idx);
        if (bindings != null) {
            for (Map.Entry<String, String> entry : bindings.entrySet()) {
                content = content.replace("{{" + entry.getKey() + "}}", entry.getValue());
            }
        }

        // 应用 manualSubItems — U3 dual-write 骨架
        // 形态识别:Object 是 List<ManualSubItem> (新) → 逐条拼接；String (旧) → content.replace 整段
        Object manualItem = context.manualSubItems.get(idx);
        if (manualItem != null && content.contains("{{#each items}}")) {
            if (manualItem instanceof List) {
                @SuppressWarnings("unchecked")
                List<ManualSubItem> items = (List<ManualSubItem>) manualItem;
                content = expandEachBlock(content, items);
            } else if (manualItem instanceof String) {
                content = content.replace(
                    "{{#each items}}" + extractEachBody(content) + "{{/each}}",
                    (String) manualItem
                );
            }
        }

        // 应用 selectedKOs（替换 {{#each items}} 列表为 KO IDs）
        String[] koIds = context.selectedKOs.get(idx);
        if (koIds != null && content.contains("{{#each items}}")) {
            content = content.replace(
                "{{#each items}}" + extractEachBody(content) + "{{/each}}",
                String.join(", ", koIds)
            );
        }

        return content;
    }

    /**
     * U3 新增：把 {{#each items}}...{{/each}} 块体内 body 对 List<ManualSubItem> 逐条拼接
     * 保留块体模板（如 "{{title}}\n{{content}}"），每条实例化一行后用换行分隔
     * R7 决议允许嵌入 {{var}} 时，按 varBindings 字典命中替换（R15b 局部命名空间）
     */
    private String expandEachBlock(String content, List<ManualSubItem> items) {
        String body = extractEachBody(content);
        if (body == null || body.isEmpty()) return content;
        String expanded = items.stream()
            .map(item -> {
                String rendered = body;
                if (item.getTitle() != null) {
                    rendered = rendered.replace("{{title}}", item.getTitle());
                }
                if (item.getContent() != null) {
                    rendered = rendered.replace("{{content}}", item.getContent());
                }
                if (item.getValue() != null) {
                    rendered = rendered.replace("{{value}}", String.valueOf(item.getValue()));
                }
                if (item.getUnit() != null) {
                    rendered = rendered.replace("{{unit}}", item.getUnit());
                }
                return rendered;
            })
            .collect(Collectors.joining("\n"));
        return content.replace("{{#each items}}" + body + "{{/each}}", expanded);
    }

    /**
     * 提取 {{#each items}}...{{/each}} 之间的 body（简化实现）
     */
    private String extractEachBody(String content) {
        int start = content.indexOf("{{#each items}}");
        if (start < 0) return "";
        int bodyStart = start + "{{#each items}}".length();
        int end = content.indexOf("{{/each}}", bodyStart);
        if (end < 0) return "";
        return content.substring(bodyStart, end);
    }

    /**
     * 计算 OQ-16 实际装配数
     * = selectedKOs.length + varBindings.size + manualSubItems.size
     */
    /**
     * 计算 OQ-16 实际装配数（U3 阶段重推导）：
     * = selectedKOs.length + varBindings.size + manualSubItems.size
     *
     * <p>U3 后 manualSubItems.size 在 §3 段的语义：
     * <ul>
     *   <li>List<ManualSubItem> 形态（array schema）：= items.length（38 演示值 = 38 条 manualSubItems = 38 装配）</li>
     *   <li>String 形态（dual-write 兼容）：= 1（按 section 计，老逻辑保持 1 sprint 兼容）</li>
     * </ul>
     *
     * <p>PRD §10.5.1 演示值 38：§3 全部 38 条 manualSubItems 在新 schema 下仍可重放。
     */
    private int computeAssemblyCount(ComposeContext context) {
        int count = 0;
        for (String[] koIds : context.selectedKOs.values()) {
            if (koIds != null) count += koIds.length;
        }
        for (Map<String, String> bindings : context.varBindings.values()) {
            if (bindings != null) count += bindings.size();
        }
        for (Object item : context.manualSubItems.values()) {
            if (item == null) continue;
            if (item instanceof List) {
                @SuppressWarnings("unchecked")
                List<ManualSubItem> items = (List<ManualSubItem>) item;
                count += items.size();
            } else if (item instanceof String) {
                String s = (String) item;
                if (!s.isEmpty()) count += 1;
            }
        }
        return count;
    }
}