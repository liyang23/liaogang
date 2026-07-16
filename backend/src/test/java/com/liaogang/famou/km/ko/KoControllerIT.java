package com.liaogang.famou.km.ko;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.liaogang.famou.km.KmApplication;
import com.liaogang.famou.km.ko.model.KoEntity;
import com.liaogang.famou.km.ko.service.KoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * KoController 集成测试（T202）。
 *
 * <p>覆盖：
 * <ul>
 *   <li>6 类型 KO 各创建 + 查询 1 条</li>
 *   <li>跨类搜索（OQ-4：title + id + typeName）</li>
 *   <li>列表（按 type/projectId 过滤 + 分页）</li>
 *   <li>软删除</li>
 *   <li>跨项目隔离（X-Project-Id 校验）</li>
 * </ul>
 */
@SpringBootTest(classes = KmApplication.class, webEnvironment = WebEnvironment.RANDOM_PORT, properties = {
    // F-30 修复：KoControllerIT 不依赖 Flyway seed，跳过 V9001/V9002 migration（MySQL DDL 与 H2 不兼容）
    "spring.flyway.enabled=false",
    "spring.jpa.hibernate.ddl-auto=none"
})
@AutoConfigureMockMvc(addFilters = false)  // F-31 修复：跳过 Spring Security filter（默认 403 拦截所有未认证请求）
@ActiveProfiles("it")
@Sql(scripts = "/db/test-schema.sql")  // F-30 修复：建 H2 兼容 schema（绕开 Flyway MySQL DDL 不兼容）
@Transactional  // F-32 修复：每个测试方法自动回滚（避免 MySQL 数据累积导致测试不稳定）
@Rollback
@DisplayName("KoController 集成测试")
class KoControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private KoService koService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String[] ALL_TYPES = {"CON", "RUL", "PAR", "SCH", "PRM", "DOC"};

    @Test
    @DisplayName("6 类型 KO 各创建 + 查询 1 条")
    void createAndGetAllSixTypes() throws Exception {
        for (String type : ALL_TYPES) {
            // 创建
            KoEntity ko = new KoEntity();
            ko.setType(type);
            ko.setTitle("CreateAndGetTest " + type);
            ko.setCode(type + "-TEST-" + java.util.UUID.randomUUID());
            ko.setProjectId("PROJ-TEST-001");
            ko.setDefinition(type + " test definition");
            ko.setEffect("Hard");
            ko.setLevel("L1");

            String responseBody = mockMvc.perform(post("/api/ko")
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(ko)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.status").exists())
                .andReturn().getResponse().getContentAsString();

            // 从响应中提取创建的 ID（避免硬编码 KO-CON-1，因 V9001 已有 KO-CON-0001）
            String id = objectMapper.readTree(responseBody).path("data").path("id").asText();
            org.assertj.core.api.Assertions.assertThat(id).startsWith("KO-" + type + "-");

            // 查询
            mockMvc.perform(get("/api/ko/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.type").value(type))
                .andExpect(jsonPath("$.data.title").value("CreateAndGetTest " + type));
        }
    }

    @Test
    @DisplayName("跨类搜索 OQ-4：title 匹配（用唯一 title 验证 6 条）")
    void searchByTitle() throws Exception {
        // F-33 修复：用 UUID 唯一 title 避免 V9001 seed 干扰（之前用"测试"匹配 54 条）
        String uniqueTitle = "SearchByTitleTest " + java.util.UUID.randomUUID();

        // 创建 6 类型 KO，都用 uniqueTitle
        for (String type : ALL_TYPES) {
            KoEntity ko = new KoEntity();
            ko.setType(type);
            ko.setTitle(uniqueTitle);
            ko.setCode(type + "-SBT-" + java.util.UUID.randomUUID());
            ko.setProjectId("PROJ-TEST-001");
            ko.setDefinition(type + " search by title test");
            koService.createKo(ko);
        }

        mockMvc.perform(get("/api/ko/search")
                .param("query", uniqueTitle)
                .header("X-Project-Id", "PROJ-TEST-001"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data.length()").value(6))
            .andExpect(jsonPath("$.data[0].matchedField").value("title"));
    }

    @Test
    @DisplayName("跨类搜索 OQ-4：id 匹配（精确 ID 搜索返回 1 条）")
    void searchById() throws Exception {
        // 先 seed 一个 KO 并获取返回的 id（避免硬编码 KO-CON-1）
        String createdId = seedAndReturnId("CON", "SearchByIdTest");

        // 用精确 ID 搜索
        mockMvc.perform(get("/api/ko/search")
                .param("query", createdId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.length()").value(1))
            .andExpect(jsonPath("$.data[0].id").value(createdId))
            .andExpect(jsonPath("$.data[0].type").value("CON"))
            .andExpect(jsonPath("$.data[0].matchedField").value("id"));
    }

    @Test
    @DisplayName("跨类搜索 OQ-4：typeName 匹配（验证返回结果含 type='CON' 元素）")
    void searchByTypeName() throws Exception {
        // F-35 修复：去掉 X-Project-Id 限制（V9001 CON 在 PROJ-0001~0004，限定 PROJ-TEST-001 会过滤掉）
        mockMvc.perform(get("/api/ko/search")
                .param("query", "约束")
                .param("types", "CON"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data[0].typeName").value("约束"))
            .andExpect(jsonPath("$.data[0].type").value("CON"))
            .andExpect(jsonPath("$.data[0].matchedField").value("typeName"));
    }

    @Test
    @DisplayName("跨类搜索：按 types 参数过滤（types=CON,RUL 只返回这两种）")
    void searchByTypesFilter() throws Exception {
        seedAllTypes();

        // 用唯一 title 标识（避免 V9001 seed 干扰）
        String uniqueTitle = "SearchByTypesFilterTest " + java.util.UUID.randomUUID();
        KoEntity koCon = new KoEntity();
        koCon.setType("CON");
        koCon.setTitle(uniqueTitle);
        koCon.setCode("CON-FILTER-" + java.util.UUID.randomUUID());
        koCon.setProjectId("PROJ-TEST-001");
        koService.createKo(koCon);

        KoEntity koRul = new KoEntity();
        koRul.setType("RUL");
        koRul.setTitle(uniqueTitle);
        koRul.setCode("RUL-FILTER-" + java.util.UUID.randomUUID());
        koRul.setProjectId("PROJ-TEST-001");
        koService.createKo(koRul);

        mockMvc.perform(get("/api/ko/search")
                .param("query", uniqueTitle)
                .param("types", "CON,RUL")
                .header("X-Project-Id", "PROJ-TEST-001"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    @DisplayName("列表：按 type 过滤返回对应类型 KO")
    void listByType() throws Exception {
        // 创建唯一标识的 CON KO（避免 V9001 seed 干扰计数）
        String uniqueTitle = "ListByTypeTest " + java.util.UUID.randomUUID();
        KoEntity ko = new KoEntity();
        ko.setType("CON");
        ko.setTitle(uniqueTitle);
        ko.setCode("CON-LIST-" + java.util.UUID.randomUUID());
        ko.setProjectId("PROJ-TEST-001");
        koService.createKo(ko);

        // 按 type=CON 过滤 + project_id 限定 + 分页 size=10
        mockMvc.perform(get("/api/ko")
                .param("type", "CON")
                .param("projectId", "PROJ-TEST-001")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.records").isArray())
            .andExpect(jsonPath("$.data.records[?(@.title == '" + uniqueTitle + "')]").exists());
    }

    @Test
    @DisplayName("跨项目隔离：X-Project-Id 不同的查询拒绝")
    void crossProjectIsolation() throws Exception {
        // 创建唯一标识的 KO（在 PROJ-TEST-001）
        String uniqueTitle = "CrossProjectTest " + java.util.UUID.randomUUID();
        KoEntity ko = new KoEntity();
        ko.setType("CON");
        ko.setTitle(uniqueTitle);
        ko.setCode("CON-CP-" + java.util.UUID.randomUUID());
        ko.setProjectId("PROJ-TEST-001");
        KoEntity created = koService.createKo(ko);
        String id = created.getId();

        // 不带 X-Project-Id：能查到（系统管理员视角）
        mockMvc.perform(get("/api/ko/" + id))
            .andExpect(status().isOk());

        // 带 PROJ-OTHER：拒绝
        mockMvc.perform(get("/api/ko/" + id)
                .header("X-Project-Id", "PROJ-OTHER"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(40403))
            .andExpect(jsonPath("$.msg").value(org.hamcrest.Matchers.containsString("跨项目")));
    }

    @Test
    @DisplayName("软删除：is_deleted=1 后查询不到")
    void softDelete() throws Exception {
        // 创建唯一标识 KO 并获取 id
        String id = seedAndReturnId("CON", "SoftDeleteTest");

        // 删除
        mockMvc.perform(delete("/api/ko/" + id))
            .andExpect(status().isOk());

        // 查询 40401
        mockMvc.perform(get("/api/ko/" + id))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(40401));
    }

    /** 工具方法：seed 6 类型各 1 条 KO（用 UUID 避免与 V9001 seed 冲突） */
    private void seedAllTypes() {
        String suffix = java.util.UUID.randomUUID().toString().substring(0, 8);
        for (String type : ALL_TYPES) {
            KoEntity ko = new KoEntity();
            ko.setType(type);
            ko.setTitle("测试 " + type + " KO " + suffix);
            ko.setCode(type + "-SEED-" + suffix);
            ko.setProjectId("PROJ-TEST-001");
            ko.setDefinition(type + " seed definition");
            ko.setEffect("Hard");
            ko.setLevel("L1");
            try {
                koService.createKo(ko);
            } catch (Exception ignored) {
                // 已存在跳过
            }
        }
    }

    /** 工具方法：seed 1 条 KO 并返回其生成的 ID */
    private String seedAndReturnId(String type, String titlePrefix) {
        String suffix = java.util.UUID.randomUUID().toString().substring(0, 8);
        KoEntity ko = new KoEntity();
        ko.setType(type);
        ko.setTitle(titlePrefix + " " + suffix);
        ko.setCode(type + "-SEED-" + suffix);
        ko.setProjectId("PROJ-TEST-001");
        ko.setDefinition(type + " seed definition");
        ko.setEffect("Hard");
        ko.setLevel("L1");
        KoEntity created = koService.createKo(ko);
        return created.getId();
    }
}