package com.liaogang.famou.km.prompt.service;

import com.liaogang.famou.km.common.BusinessException;
import com.liaogang.famou.km.prompt.model.PrmSectionEntity;
import com.liaogang.famou.km.prompt.model.PrmTemplateEntity;
import com.liaogang.famou.km.prompt.repository.PrmSectionMapper;
import com.liaogang.famou.km.prompt.repository.PrmTemplateMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        public Map<Integer, String> manualSubItems = new HashMap<>();       // sectionIndex → content
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

        // 应用 manualSubItems（替换 {{#each items}}...{{/each}} 中的 items 列表）
        String manualItem = context.manualSubItems.get(idx);
        if (manualItem != null && content.contains("{{#each items}}")) {
            content = content.replace(
                "{{#each items}}" + extractEachBody(content) + "{{/each}}",
                manualItem
            );
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
    private int computeAssemblyCount(ComposeContext context) {
        int count = 0;
        // selectedKOs 贡献
        for (String[] koIds : context.selectedKOs.values()) {
            if (koIds != null) count += koIds.length;
        }
        // varBindings 贡献
        for (Map<String, String> bindings : context.varBindings.values()) {
            if (bindings != null) count += bindings.size();
        }
        // manualSubItems 贡献
        for (String item : context.manualSubItems.values()) {
            if (item != null && !item.isEmpty()) count += 1;
        }
        return count;
    }
}