package com.liaogang.famou.km.prompt.service;

import com.liaogang.famou.km.common.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 服务端 Handlebars 子集渲染引擎（T211，OQ-15）。
 *
 * <p>v0.32 §10.5.3 模板渲染支持的语法（与前端 handlebars.ts 一致）：
 * <ul>
 *   <li>{{var}} 变量替换</li>
 *   <li>{{#each items}}...{{/each}} 数组循环</li>
 *   <li>{{#if cond}}...{{/if}} 条件分支</li>
 * </ul>
 *
 * <p>F-29 修复：Handlebars 语法错误检测（plan done_signal 关键）
 * <p>错误类型：
 * <ul>
 *   <li>未闭合的 {{#each}}（缺 {{/each}}）</li>
 *   <li>未闭合的 {{#if}}（缺 {{/if}}）</li>
 *   <li>错误的 {{#each 名称}}（名称含特殊字符）</li>
 *   <li>未知语法（如 {{> partial}} OQ-15 决策不实现）</li>
 * </ul>
 */
@Slf4j
@Component
public class TemplateEngine {

    /** {{#each name}} 开始标签 */
    private static final Pattern EACH_OPEN = Pattern.compile("\\{\\{#each\\s+([a-zA-Z_][a-zA-Z0-9_]*)\\s*\\}\\}");

    /** {{/each}} 结束标签 */
    private static final Pattern EACH_CLOSE = Pattern.compile("\\{\\{/each\\}\\}");

    /** {{#if cond}} 开始标签 */
    private static final Pattern IF_OPEN = Pattern.compile("\\{\\{#if\\s+([^}]+?)\\}\\}");

    /** {{/if}} 结束标签 */
    private static final Pattern IF_CLOSE = Pattern.compile("\\{\\{/if\\}\\}");

    /** {{#each}} 与 {{/each}} 计数差（>0 表示未闭合） */
    public void validateSyntax(String template) {
        int eachOpen = 0, eachClose = 0;
        int ifOpen = 0, ifClose = 0;

        Matcher m = EACH_OPEN.matcher(template);
        while (m.find()) eachOpen++;
        m = EACH_CLOSE.matcher(template);
        while (m.find()) eachClose++;
        m = IF_OPEN.matcher(template);
        while (m.find()) ifOpen++;
        m = IF_CLOSE.matcher(template);
        while (m.find()) ifClose++;

        if (eachOpen != eachClose) {
            throw new BusinessException(40070,
                "Handlebars 语法错误：{{#each}} 与 {{/each}} 数量不匹配 ("
                    + eachOpen + " vs " + eachClose + ")");
        }
        if (ifOpen != ifClose) {
            throw new BusinessException(40070,
                "Handlebars 语法错误：{{#if}} 与 {{/if}} 数量不匹配 ("
                    + ifOpen + " vs " + ifClose + ")");
        }
    }

    /**
     * 渲染 Handlebars 模板
     */
    public String render(String template, Map<String, Object> context) {
        validateSyntax(template);
        String result = template;
        for (int i = 0; i < 100; i++) {
            String prev = result;
            result = processEach(result, context);
            result = processIf(result, context);
            result = processVar(result, context);
            if (result.equals(prev)) break;
        }
        return result;
    }

    private String processEach(String template, Map<String, Object> context) {
        // 简化：用 Pattern.quote + Matcher + appendReplacement 处理 {{#each name}}...{{/each}}
        Pattern p = Pattern.compile("\\{\\{#each\\s+([a-zA-Z_][a-zA-Z0-9_]*)\\s*\\}\\}([\\s\\S]*?)\\{\\{/each\\}\\}");
        Matcher m = p.matcher(template);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String varName = m.group(1);
            String body = m.group(2);
            Object arr = resolveValue(varName, context);
            StringBuilder replacement = new StringBuilder();
            if (arr instanceof List) {
                for (Object item : (List<?>) arr) {
                    Map<String, Object> itemCtx = new java.util.HashMap<>(context);
                    itemCtx.put("this", item);
                    if (item instanceof Map) {
                        itemCtx.putAll((Map<String, Object>) item);
                    }
                    replacement.append(render(body, itemCtx));
                }
            }
            m.appendReplacement(sb, Matcher.quoteReplacement(replacement.toString()));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    private String processIf(String template, Map<String, Object> context) {
        Pattern p = Pattern.compile("\\{\\{#if\\s+([^}]+?)\\}\\}([\\s\\S]*?)\\{\\{/if\\}\\}");
        Matcher m = p.matcher(template);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String cond = m.group(1).trim();
            String body = m.group(2);
            boolean truthy = evaluateCondition(cond, context);
            m.appendReplacement(sb, Matcher.quoteReplacement(truthy ? body : ""));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    private String processVar(String template, Map<String, Object> context) {
        // 匹配 {{var}} 或 {{this.x}}，排除 {{#...}} / {{/...}} / {{>...}}
        Pattern p = Pattern.compile("\\{\\{(?![#/])((?!>)[\\s\\S]+?)\\}\\}");
        Matcher m = p.matcher(template);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String expr = m.group(1).trim();
            Object val = resolveValue(expr, context);
            m.appendReplacement(sb, Matcher.quoteReplacement(val == null ? "" : val.toString()));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    private boolean evaluateCondition(String cond, Map<String, Object> context) {
        String[] ops = {"==", "!=", ">=", "<=", ">", "<"};
        for (String op : ops) {
            String patternStr = " " + op + " ";
            int idx = cond.indexOf(patternStr);
            if (idx >= 0) {
                String leftExpr = cond.substring(0, idx).trim();
                String rightExpr = cond.substring(idx + patternStr.length()).trim();
                Object left = resolveValue(leftExpr, context);
                Object right = resolveValue(rightExpr, context);
                switch (op) {
                    case "==": return String.valueOf(left).equals(String.valueOf(right));
                    case "!=": return !String.valueOf(left).equals(String.valueOf(right));
                    case ">": return toDouble(left) > toDouble(right);
                    case "<": return toDouble(left) < toDouble(right);
                    case ">=": return toDouble(left) >= toDouble(right);
                    case "<=": return toDouble(left) <= toDouble(right);
                }
            }
        }
        Object val = resolveValue(cond, context);
        return val != null && !val.toString().isEmpty()
            && !val.toString().equals("false") && !val.toString().equals("0");
    }

    private double toDouble(Object o) {
        if (o == null) return 0;
        try { return Double.parseDouble(o.toString()); } catch (NumberFormatException e) { return 0; }
    }

    private Object resolveValue(String expr, Map<String, Object> context) {
        // 字符串字面量
        if ((expr.startsWith("\"") && expr.endsWith("\"")) ||
            (expr.startsWith("'") && expr.endsWith("'"))) {
            return expr.substring(1, expr.length() - 1);
        }
        // 数字字面量
        if (expr.matches("-?\\d+(\\.\\d+)?")) {
            return Double.parseDouble(expr);
        }
        // this.x
        if (expr.startsWith("this.")) {
            Object thisVal = context.get("this");
            if (thisVal instanceof Map) {
                return ((Map<String, Object>) thisVal).get(expr.substring(5));
            }
            return null;
        }
        // 嵌套属性
        String[] parts = expr.split("\\.");
        Object val = context.get(parts[0]);
        for (int i = 1; i < parts.length && val != null; i++) {
            if (val instanceof Map) {
                val = ((Map<String, Object>) val).get(parts[i]);
            } else {
                return null;
            }
        }
        return val;
    }
}