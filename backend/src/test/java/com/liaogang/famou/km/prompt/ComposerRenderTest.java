package com.liaogang.famou.km.prompt;

import com.liaogang.famou.km.prompt.model.PrmSectionEntity;
import com.liaogang.famou.km.prompt.model.PrmTemplateEntity;
import com.liaogang.famou.km.prompt.repository.PrmSectionMapper;
import com.liaogang.famou.km.prompt.repository.PrmTemplateMapper;
import com.liaogang.famou.km.prompt.service.ComposerRenderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ComposerRenderService 测试（T211 done_signal: 5/5 测试通过）。
 *
 * <p>覆盖 OQ-16 PRP 实际装配数动态计算：
 * <ul>
 *   <li>selectedKOs.length + varBindings.size + manualSubItems.size</li>
 *   <li>空上下文时 = 0</li>
 *   <li>混合上下文时 = 各项之和</li>
 * </ul>
 */
@SpringBootTest(properties = {
    "spring.flyway.enabled=false",
    "spring.jpa.hibernate.ddl-auto=none"
})
@ActiveProfiles("it")
@Sql(scripts = "/db/test-schema.sql")
@Transactional
@DisplayName("ComposerRenderService PRP 装配数动态计算")
class ComposerRenderTest {

    @Autowired
    private ComposerRenderService composerRenderService;

    @Autowired
    private PrmTemplateMapper prmTemplateMapper;

    @Autowired
    private PrmSectionMapper prmSectionMapper;

    @BeforeEach
    void seedTestTemplate() {
        // 清空旧数据 + 插入 mock 模板
        prmSectionMapper.delete(null);
        prmTemplateMapper.delete(null);

        PrmTemplateEntity template = PrmTemplateEntity.builder()
            .id("KO-TEST-001")
            .name("测试模板")
            .description("T211 测试用")
            .version("v1.0.0")
            .build();
        prmTemplateMapper.insert(template);

        for (int i = 1; i <= 3; i++) {
            PrmSectionEntity sec = PrmSectionEntity.builder()
                .templateId("KO-TEST-001")
                .sectionIndex(i)
                .title("Section " + i)
                .sectionType(i == 1 ? "FIXED" : "DYNAMIC")
                .content("Section " + i + " content {{var" + i + "}}")
                .build();
            prmSectionMapper.insert(sec);
        }
    }

    @Test
    @DisplayName("空上下文：PRP 装配数 = 0")
    void emptyContext() {
        ComposerRenderService.ComposeContext ctx = new ComposerRenderService.ComposeContext();
        ComposerRenderService.RenderResult result = composerRenderService.render("KO-TEST-001", ctx);
        assertThat(result.assemblyCount).isEqualTo(0);
        assertThat(result.sectionCount).isEqualTo(3);
    }

    @Test
    @DisplayName("OQ-16：selectedKOs 5 + varBindings 3 + manualSubItems 1 = 9")
    void fullContext() {
        ComposerRenderService.ComposeContext ctx = new ComposerRenderService.ComposeContext();
        // 5 个 KO
        ctx.selectedKOs.put(1, new String[]{"KO-A", "KO-B", "KO-C", "KO-D", "KO-E"});
        // 3 个变量绑定
        ctx.varBindings.put(1, Map.of("var1", "VAL1", "var2", "VAL2", "var3", "VAL3"));
        // 1 个手动子项
        ctx.manualSubItems.put(2, "manual content");

        ComposerRenderService.RenderResult result = composerRenderService.render("KO-TEST-001", ctx);
        assertThat(result.assemblyCount).isEqualTo(9);
    }

    @Test
    @DisplayName("只 selectedKOs：装配数 = KO 数")
    void onlySelectedKOs() {
        ComposerRenderService.ComposeContext ctx = new ComposerRenderService.ComposeContext();
        ctx.selectedKOs.put(1, new String[]{"KO-A", "KO-B"});
        ctx.selectedKOs.put(2, new String[]{"KO-C"});

        ComposerRenderService.RenderResult result = composerRenderService.render("KO-TEST-001", ctx);
        assertThat(result.assemblyCount).isEqualTo(3);  // 2 + 1
    }

    @Test
    @DisplayName("字符数 + token 数估算（g(M) 2 chars/token）")
    void charAndTokenCount() {
        ComposerRenderService.ComposeContext ctx = new ComposerRenderService.ComposeContext();
        ComposerRenderService.RenderResult result = composerRenderService.render("KO-TEST-001", ctx);
        // content 包含 "## Section X\n\nSection X content {{varX}}\n\n" × 3
        assertThat(result.charCount).isGreaterThan(50);
        // tokenCount ≈ charCount / 2
        assertThat(result.tokenCount).isEqualTo((int) Math.ceil(result.charCount / 2.0));
    }

    @Test
    @DisplayName("varBindings 替换 {{var}} 占位")
    void varBindingsReplacement() {
        ComposerRenderService.ComposeContext ctx = new ComposerRenderService.ComposeContext();
        ctx.varBindings.put(1, Map.of("var1", "REPLACED"));

        ComposerRenderService.RenderResult result = composerRenderService.render("KO-TEST-001", ctx);
        // Section 1 content "Section 1 content {{var1}}" → 应包含 REPLACED
        assertThat(result.rendered).contains("REPLACED");
    }
}