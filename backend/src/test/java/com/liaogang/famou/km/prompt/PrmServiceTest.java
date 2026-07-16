package com.liaogang.famou.km.prompt;

import com.liaogang.famou.km.prompt.model.PrmSectionEntity;
import com.liaogang.famou.km.prompt.model.PrmTemplateEntity;
import com.liaogang.famou.km.prompt.repository.PrmSectionMapper;
import com.liaogang.famou.km.prompt.repository.PrmTemplateMapper;
import com.liaogang.famou.km.prompt.service.PrmService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * PrmService 测试（T208 done_signal: 3/3 测试通过）。
 *
 * <p>覆盖：
 * <ul>
 *   <li>3 预置 PRM 模板自动 seed（KO-PRM-0001/0002/0003）</li>
 *   <li>17 段完整性（9+3+5 = 17）</li>
 *   <li>每段 content 非空 + section_type 合法（FIXED / DYNAMIC）</li>
 * </ul>
 */
@SpringBootTest(properties = {
    "spring.flyway.enabled=false",
    "spring.jpa.hibernate.ddl-auto=none"
})
@ActiveProfiles("it")
@Sql(scripts = "/db/test-schema.sql")
@Transactional
@DisplayName("PrmService 模板加载测试")
class PrmServiceTest {

    @Autowired
    private PrmService prmService;

    @Autowired
    private PrmTemplateMapper prmTemplateMapper;

    @Autowired
    private PrmSectionMapper prmSectionMapper;

    @Test
    @DisplayName("3 预置 PRM 模板自动 seed（KO-PRM-0001/0002/0003）")
    void templatesSeeded() {
        // 手动调用（@PostConstruct 跑时 @Sql 可能未执行）
        prmService.loadTemplates();

        for (String id : new String[]{"KO-PRM-0001", "KO-PRM-0002", "KO-PRM-0003"}) {
            PrmTemplateEntity template = prmTemplateMapper.selectById(id);
            assertThat(template)
                .as("PRM 模板 %s 应被 seed", id)
                .isNotNull();
            assertThat(template.getName()).isNotEmpty();
            assertThat(template.getVersion()).isNotEmpty();
        }
    }

    @Test
    @DisplayName("17 段完整性：KO-PRM-0001 9 段 + KO-PRM-0002 3 段 + KO-PRM-0003 5 段")
    void sectionsCountCorrect() {
        prmService.loadTemplates();
        prmService.loadSections();

        Long count0001 = countSections("KO-PRM-0001");
        Long count0002 = countSections("KO-PRM-0002");
        Long count0003 = countSections("KO-PRM-0003");

        assertThat(count0001).as("KO-PRM-0001 段数").isEqualTo(9L);
        assertThat(count0002).as("KO-PRM-0002 段数").isEqualTo(3L);
        assertThat(count0003).as("KO-PRM-0003 段数").isEqualTo(5L);
        assertThat(count0001 + count0002 + count0003)
            .as("总段数").isEqualTo(17L);
    }

    @Test
    @DisplayName("每段 content 非空 + section_type 合法（FIXED / DYNAMIC）")
    void sectionContentValid() {
        prmService.loadTemplates();
        prmService.loadSections();

        for (String templateId : new String[]{"KO-PRM-0001", "KO-PRM-0002", "KO-PRM-0003"}) {
            var sections = prmService.getSections(templateId);
            for (PrmSectionEntity section : sections) {
                assertThat(section.getContent())
                    .as("Section %s/%d content 非空", templateId, section.getSectionIndex())
                    .isNotEmpty();
                assertThat(section.getSectionType())
                    .as("Section %s/%d type 合法", templateId, section.getSectionIndex())
                    .isIn("FIXED", "DYNAMIC");
                assertThat(section.getTitle())
                    .as("Section %s/%d title 非空", templateId, section.getSectionIndex())
                    .isNotEmpty();
            }
        }
    }

    private long countSections(String templateId) {
        return prmSectionMapper.selectCount(
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<PrmSectionEntity>()
                .eq("template_id", templateId)
        );
    }
}
