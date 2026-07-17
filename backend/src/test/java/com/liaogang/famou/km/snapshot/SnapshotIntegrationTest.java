package com.liaogang.famou.km.snapshot;

import com.liaogang.famou.km.audit.AuditLogEntity;
import com.liaogang.famou.km.audit.AuditLogService;
import com.liaogang.famou.km.prompt.dto.ManualSubItem;
import com.liaogang.famou.km.snapshot.model.PromptSnapshotEntity;
import com.liaogang.famou.km.snapshot.repository.PromptSnapshotMapper;
import com.liaogang.famou.km.snapshot.service.SnapshotService;
import com.liaogang.famou.km.snapshot.service.StaleSnapshotJob;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * T307 集成测试 (U8) - 渲染 → hash 命中 → PAR 变更 → stale + 113 KO 装配回归
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SnapshotIntegrationTest {

    @Mock private PromptSnapshotMapper promptSnapshotMapper;
    @Mock private AuditLogService auditLogService;

    @InjectMocks private SnapshotService snapshotService;
    @InjectMocks private StaleSnapshotJob staleSnapshotJob;

    private List<PromptSnapshotEntity> snapshotStore;

    @BeforeEach
    void setUp() {
        snapshotStore = new ArrayList<>();
        // 模拟 SNP store: findByHash / insert / markStaleByPrmExcludingHash
        when(promptSnapshotMapper.findByHash(anyString())).thenAnswer(inv -> {
            String hash = inv.getArgument(0);
            return snapshotStore.stream().filter(s -> hash.equals(s.getHash())).findFirst().orElse(null);
        });
        org.mockito.Mockito.doAnswer(inv -> {
            PromptSnapshotEntity s = inv.getArgument(0);
            snapshotStore.removeIf(x -> x.getHash().equals(s.getHash()));
            if (s.getId() == null) s.setId((long) (snapshotStore.size() + 1));
            snapshotStore.add(s);
            return 1;
        }).when(promptSnapshotMapper).insert(any(PromptSnapshotEntity.class));
        when(promptSnapshotMapper.markStaleByPrmExcludingHash(anyString(), anyString())).thenAnswer(inv -> {
            String prmId = inv.getArgument(0);
            String excludeHash = inv.getArgument(1);
            int affected = 0;
            for (PromptSnapshotEntity s : snapshotStore) {
                if (prmId.equals(s.getPrmId()) && !excludeHash.equals(s.getHash())) {
                    s.setStale(true);
                    affected++;
                }
            }
            return affected;
        });
    }

    @Test
    @DisplayName("Happy path 1: 渲染 → hash 不命中 → 创建 SNP")
    void render_hashMiss_createSnp() {
        SnapshotService.RenderResult result = snapshotService.render(
                "KO-PRM-0001", "v3.0",
                List.of("KO-A", "KO-B"), List.of("v1.0", "v1.0"),
                Map.of("k1", "v1"),
                IntStream.range(0, 38).mapToObj(i -> {
                    ManualSubItem m = new ManualSubItem();
                    m.setTitle("料场" + i);
                    m.setContent("内容" + i);
                    return m;
                }).collect(Collectors.toList()),
                Map.of("var0", "PAR-0"),
                "rendered content", "user-001");

        assertNotNull(result);
        assertEquals(16, result.hash.length());
        assertNotNull(result.snapshotId);
        verify(promptSnapshotMapper, times(1)).insert(any(PromptSnapshotEntity.class));
    }

    @Test
    @DisplayName("Happy path 2: 相同输入 → hash 命中 → 复用 SNP 不创建新")
    void render_hashHit_reuseSnp() {
        // 第一次渲染
        SnapshotService.RenderResult r1 = snapshotService.render(
                "KO-PRM-0001", "v3.0",
                List.of("KO-A"), List.of("v1.0"),
                Map.of(), List.of(), Map.of(),
                "rendered content", "user-001");
        long firstId = r1.snapshotId;

        // 第二次相同输入渲染
        SnapshotService.RenderResult r2 = snapshotService.render(
                "KO-PRM-0001", "v3.0",
                List.of("KO-A"), List.of("v1.0"),
                Map.of(), List.of(), Map.of(),
                "rendered content (different content)", "user-002");
        assertEquals(firstId, r2.snapshotId, "hash 命中复用同一 SNP id");
        assertEquals(r1.hash, r2.hash);
    }

    @Test
    @DisplayName("Happy path 3: 113 KO 装配演示值 (OQ-16) - 演示值 hash 稳定")
    void demo113koAssemblyHash_stable() {
        String h1 = snapshotService.demo113koAssemblyHash();
        String h2 = snapshotService.demo113koAssemblyHash();
        assertEquals(h1, h2, "113 KO 演示值 hash 稳定");
        assertEquals(16, h1.length());
    }

    @Test
    @DisplayName("Edge case: PAR 变更 → 标记陈旧 + 写 SNP_STALE_DETECTED 审计")
    void parChange_marksStale_auditTriggered() {
        // 创建 2 个 SNP (不同 hash 但同 PRM)
        snapshotService.render("KO-PRM-0001", "v3.0",
                List.of("KO-A", "KO-B"), List.of("v1.0", "v1.0"),
                Map.of(), List.of(), Map.of("var0", "PAR-0"),
                "rendered v1", "user-001");
        String excludeHash = snapshotService.render("KO-PRM-0001", "v3.0",
                List.of("KO-A", "KO-C"), List.of("v1.0", "v1.0"),
                Map.of(), List.of(), Map.of("var0", "PAR-1"),
                "rendered v2 (with new PAR-1)", "user-002").hash;

        // 触发 PAR 变更
        int affected = staleSnapshotJob.markStaleForPrm("KO-PRM-0001", excludeHash);

        assertEquals(1, affected, "仅 1 个 SNP 被标记陈旧 (排除当前 hash)");
        PromptSnapshotEntity staleSnp = snapshotStore.stream()
                .filter(s -> s.getHash().equals(excludeHash))
                .findFirst().orElseThrow();
        assertFalse(staleSnp.getStale(), "excludeHash 的 SNP 不被标记");
        PromptSnapshotEntity otherSnp = snapshotStore.stream()
                .filter(s -> !s.getHash().equals(excludeHash))
                .findFirst().orElseThrow();
        assertTrue(otherSnp.getStale(), "其他 SNP 被标记 stale");

        verify(auditLogService, times(1)).record(any(AuditLogEntity.class));
    }

    @Test
    @DisplayName("Edge case: 不同 prm_id 不影响 stale 标记 (PAR 变更只影响本 PRM)")
    void parChange_otherPrmNotAffected() {
        snapshotService.render("KO-PRM-0001", "v3.0",
                List.of("KO-A"), List.of("v1.0"),
                Map.of(), List.of(), Map.of(),
                "p1 content", "user-001");
        snapshotService.render("KO-PRM-0002", "v3.0",
                List.of("KO-D"), List.of("v1.0"),
                Map.of(), List.of(), Map.of(),
                "p2 content", "user-001");

        // 标记 KO-PRM-0001 stale
        int affected = staleSnapshotJob.markStaleForPrm("KO-PRM-0001", "dummy_exclude");

        assertEquals(1, affected);
        PromptSnapshotEntity p1 = snapshotStore.stream()
                .filter(s -> "KO-PRM-0001".equals(s.getPrmId()))
                .findFirst().orElseThrow();
        PromptSnapshotEntity p2 = snapshotStore.stream()
                .filter(s -> "KO-PRM-0002".equals(s.getPrmId()))
                .findFirst().orElseThrow();
        assertTrue(p1.getStale());
        assertFalse(p2.getStale(), "KO-PRM-0002 不应被标记 stale");
    }

    @Test
    @DisplayName("113 KO 装配回归 - 演示值与 38 演示值重放都通过")
    void demo113koAndDemo38_regression() {
        String h113 = snapshotService.demo113koAssemblyHash();
        assertNotNull(h113);
        assertEquals(16, h113.length());

        // 38 演示值回归: 自定义 38 manualSubItems
        List<ManualSubItem> items38 = IntStream.range(0, 38).mapToObj(i -> {
            ManualSubItem m = new ManualSubItem();
            m.setTitle("料场" + i);
            m.setContent("内容" + i);
            return m;
        }).collect(Collectors.toList());
        String h38 = snapshotService.computeAssemblyHash(
                "KO-PRM-0001", "v3.0",
                List.of("KO-A"), List.of("v1.0"),
                Map.of(), items38, Map.of());
        assertNotNull(h38);
        assertEquals(16, h38.length());
        // 113 vs 38 不同 hash (因为 ko_ids / ko_versions 不同)
        assertNotEquals(h113, h38);
    }
}
