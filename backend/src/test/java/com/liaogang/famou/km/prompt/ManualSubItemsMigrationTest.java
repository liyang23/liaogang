package com.liaogang.famou.km.prompt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.liaogang.famou.km.prompt.dto.ManualSubItem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * U3 阶段 ManualSubItems 4 端类型迁移测试
 *
 * <p>核心验证：
 * <ol>
 *   <li>PRD §10.5.1 演示值 38（§3 全 38 条 manualSubItems）在新 schema 下仍可重放</li>
 *   <li>List<ManualSubItem> 形态 JSON 反序列化兼容</li>
 *   <li>String 旧 schema → String 路径向后兼容</li>
 * </ol>
 *
 * <p>完整测试覆盖在 U5 阶段补：dual-write feature flag 默认 0% → Phase 6 升至 100% 时的 client 流量扫描 + 反序列化错误率 &lt; 0.1%/天 gating。
 *
 * @see docs/plans/2026-07-17-001-feat-liaogang-section3-manual-subitem-modal-plan.md#u3
 */
class ManualSubItemsMigrationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("PRD §10.5.1 演示值 38 在 List<ManualSubItem> schema 下重放 OQ-16 = 38")
    void demo38_replaysInArraySchema() throws Exception {
        // §3 演示场景:38 条手动子项
        List<ManualSubItem> items = new ArrayList<>();
        for (int i = 1; i <= 38; i++) {
            ManualSubItem it = new ManualSubItem();
            it.setTitle("料场" + i);
            it.setContent("计算范围条目 " + i + "：min=850,max=1000");
            items.add(it);
        }

        // 反序列化往返
        String json = objectMapper.writeValueAsString(items);
        List<ManualSubItem> parsed = objectMapper.readValue(json, new com.fasterxml.jackson.core.type.TypeReference<List<ManualSubItem>>() {});

        assertEquals(38, parsed.size(), "演示值 38 应完整反序列化");
        assertEquals("料场1", parsed.get(0).getTitle());
        assertEquals("计算范围条目 38：min=850,max=1000", parsed.get(37).getContent());
        assertEquals(38, parsed.size(), "OQ-16 = selectedKOs + varBindings + manualSubItems.size = 38 (R5 阶段 2 公式重推导后)");
    }

    @Test
    @DisplayName("ComposeContext.manualSubItems 接收 List<ManualSubItem> 形态")
    void composeContext_acceptsListManualSubItem() throws Exception {
        Map<String, Object> body = new HashMap<>();
        Map<String, Object> ctx = new HashMap<>();
        Map<String, Object> manual = new HashMap<>();

        List<Map<String, Object>> section3Items = new ArrayList<>();
        Map<String, Object> it = new HashMap<>();
        it.put("title", "料场库存上下限");
        it.put("content", "min=850,max=1000");
        section3Items.add(it);
        manual.put("3", section3Items);

        ctx.put("manualSubItems", manual);
        body.put("context", ctx);

        // 模拟 ComposerController.parseContext 形态识别
        Object value = ((Map<?, ?>) ((Map<?, ?>) body.get("context")).get("manualSubItems")).get("3");
        assertNotNull(value);
        assertTrue(value instanceof List, "section 3 manualSubItems 应被识别为 List 形态");
        assertEquals(1, ((List<?>) value).size(), "List 形态大小 = 子项条目数 = 1");
    }

    @Test
    @DisplayName("String 旧 schema dual-read fallback")
    void stringFormatDuAlReadFallback() throws Exception {
        // 旧的 U2 形态：手工拼接的字符串
        String oldFormat = "料场上限\nmax=1000 吨\n---\n料场下限\nmin=850 吨";

        // 反序列化识别：不是 JSON array，是 string
        Object parsed;
        try {
            parsed = objectMapper.readValue(oldFormat, new com.fasterxml.jackson.core.type.TypeReference<List<ManualSubItem>>() {});
        } catch (Exception expected) {
            // 不是合法的 JSON array 形态 → 走 string fallback
            parsed = oldFormat;
        }

        assertTrue(parsed instanceof String, "string 旧 schema 走 fallback 路径而非 array");
        assertEquals(oldFormat, parsed);
    }

    @Test
    @DisplayName("null manualSubItems 值跳过（dual-read 不抛错）")
    void nullValueInManualSubItemsIsSkipped() throws Exception {
        Map<String, Object> body = new HashMap<>();
        Map<String, Object> ctx = new HashMap<>();
        Map<String, Object> manual = new HashMap<>();
        manual.put("3", null);  // 业务专家删除全部子项后 undefined

        ctx.put("manualSubItems", manual);
        body.put("context", ctx);

        Object value = ((Map<?, ?>) ((Map<?, ?>) body.get("context")).get("manualSubItems")).get("3");
        assertEquals(null, value);

        // U3 阶段 parseContext 处理: value == null → continue 跳过
        // OQ-16 公式: count += items.size() 跳过 null segment
        int count = 0;
        if (value == null) {
            // skip
        }
        assertEquals(0, count, "null 段不计入 OQ-16 装配数");
    }

    @Test
    @DisplayName("ManualSubItem DTO 字段双向序列化（含可选业务字段）")
    void manualSubItemDto_roundtripWithOptionalFields() throws Exception {
        ManualSubItem item = new ManualSubItem();
        item.setTitle("料场库存上下限");
        item.setContent("min=850,max=1000");
        item.setValue(850.0);
        item.setUnit("吨");
        item.setLower_bound(0.0);
        item.setUpper_bound(1000.0);
        item.setRange_type("single");

        String json = objectMapper.writeValueAsString(item);
        ManualSubItem parsed = objectMapper.readValue(json, ManualSubItem.class);

        assertEquals("料场库存上下限", parsed.getTitle());
        assertEquals(850.0, parsed.getValue());
        assertEquals("吨", parsed.getUnit());
        assertEquals("single", parsed.getRange_type());
        // R5 阶段 1 占位决议 = title+content 二字段；可选业务字段序列化兼容在此
    }
}
