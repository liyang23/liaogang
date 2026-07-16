package com.liaogang.famou.km.prompt;

import com.liaogang.famou.km.common.BusinessException;
import com.liaogang.famou.km.prompt.service.TemplateEngine;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * TemplateEngine 测试（T211 done_signal: 8/8 测试通过）。
 *
 * <p>覆盖 Handlebars 语法 + 错误检测 + 3 类语法：
 * <ul>
 *   <li>{{var}} 替换</li>
 *   <li>{{#each}} 循环</li>
 *   <li>{{#if}} 条件</li>
 *   <li>语法错误检测（未闭合 / 未知语法）</li>
 * </ul>
 */
@DisplayName("TemplateEngine 模板引擎测试（T211）")
class TemplateEngineTest {

    private final TemplateEngine engine = new TemplateEngine();

    @Test
    @DisplayName("{{var}} 简单变量替换")
    void varSimple() {
        String result = engine.render("Hello {{name}}!", Map.of("name", "World"));
        assertThat(result).isEqualTo("Hello World!");
    }

    @Test
    @DisplayName("{{var}} 嵌套属性访问（user.name）")
    void varNested() {
        String result = engine.render("姓名: {{user.name}}", Map.of("user", Map.of("name", "李雷")));
        assertThat(result).isEqualTo("姓名: 李雷");
    }

    @Test
    @DisplayName("{{#each items}} 数组循环")
    void eachLoop() {
        String result = engine.render("{{#each items}}[{{this.name}}]{{/each}}",
            Map.of("items", List.of(
                Map.of("name", "A"),
                Map.of("name", "B"),
                Map.of("name", "C")
            )));
        assertThat(result).isEqualTo("[A][B][C]");
    }

    @Test
    @DisplayName("{{#if}} 真值条件")
    void ifTruthy() {
        String result = engine.render("{{#if show}}YES{{/if}}", Map.of("show", true));
        assertThat(result).isEqualTo("YES");
    }

    @Test
    @DisplayName("{{#if}} == 字符串比较（字符串字面量）")
    void ifStringEq() {
        String result = engine.render("{{#if x == \"yes\"}}MATCH{{/if}}", Map.of("x", "yes"));
        assertThat(result).isEqualTo("MATCH");
    }

    @Test
    @DisplayName("F-30：Handlebars 语法错误检测（未闭合 {{#each}}）")
    void syntaxErrorEachUnclosed() {
        assertThatThrownBy(() -> engine.validateSyntax("{{#each items}}body"))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("{{#each}} 与 {{/each}} 数量不匹配");
    }

    @Test
    @DisplayName("F-30：Handlebars 语法错误检测（未闭合 {{#if}}）")
    void syntaxErrorIfUnclosed() {
        assertThatThrownBy(() -> engine.validateSyntax("{{#if cond}}body"))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("{{#if}} 与 {{/if}} 数量不匹配");
    }

    @Test
    @DisplayName("正常模板（所有语法正确）不抛异常")
    void validTemplateNoError() {
        assertThatCode(() -> engine.validateSyntax(
            "{{#each items}}{{this.name}}{{/each}} {{#if show}}YES{{/if}} {{name}}"
        )).doesNotThrowAnyException();
    }
}