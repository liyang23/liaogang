package com.liaogang.famou.km.snapshot;

import com.liaogang.famou.km.prompt.dto.ManualSubItem;
import com.liaogang.famou.km.snapshot.model.PromptSnapshotEntity;
import com.liaogang.famou.km.snapshot.repository.PromptSnapshotMapper;
import com.liaogang.famou.km.snapshot.service.SnapshotService;
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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * T305 SnapshotService 单元测试 (OQ-16 + ko_assembly_hash 命中复用)
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SnapshotServiceTest {

    @Mock private PromptSnapshotMapper promptSnapshotMapper;

    @InjectMocks private SnapshotService service;

    @BeforeEach
    void setUp() {
        // 默认 hash 命中返回 null (不命中, 创建新 SNP)
        when(promptSnapshotMapper.findByHash(anyString())).thenReturn(null);
    }

    @Test
    @DisplayName("ko_assembly_hash: 16 位 SHA256 截断 + 相同输入产出相同 hash")
    void ko_assemblyHash_16HexSha256_sameInputSameHash() {
        String h1 = service.computeAssemblyHash(
                "KO-PRM-0001", "v3.0",
                List.of("KO-A", "KO-B"), List.of("v1.0", "v1.0"),
                Map.of("k1", "v1"), List.of(), Map.of());
        String h2 = service.computeAssemblyHash(
                "KO-PRM-0001", "v3.0",
                List.of("KO-B", "KO-A"),  // 不同顺序
                List.of("v1.0", "v1.0"),
                Map.of("k1", "v1"), List.of(), Map.of());

        assertEquals(h1, h2, "sorted 后相同输入产出相同 hash");
        assertEquals(16, h1.length(), "SHA256 截断 16 位");
    }

    @Test
    @DisplayName("ko_assembly_hash: 不同 prm_id 产出不同 hash")
    void ko_assemblyHash_differentPrmId_differentHash() {
        String h1 = service.computeAssemblyHash(
                "KO-PRM-0001", "v3.0",
                List.of("KO-A"), List.of("v1.0"),
                Map.of(), List.of(), Map.of());
        String h2 = service.computeAssemblyHash(
                "KO-PRM-0002", "v3.0",
                List.of("KO-A"), List.of("v1.0"),
                Map.of(), List.of(), Map.of());
        assertNotEquals(h1, h2);
    }

    @Test
    @DisplayName("Edge case: 113 KO 装配演示值重放 (OQ-16 §10.5.1)")
    void demo113koAssemblyHash_OQ16() {
        String hash = service.demo113koAssemblyHash();
        assertNotNull(hash);
        assertEquals(16, hash.length());
    }

    @Test
    @DisplayName("Edge case: 38 手动子项重放 (Q-I4 §3 ManualSubItems array schema 兼容)")
    void demo38ManualSubitems_QI4Section3() {
        // 38 manualSubItems array schema 派生 hash
        List<ManualSubItem> items = IntStream.range(0, 38).mapToObj(i -> {
            ManualSubItem m = new ManualSubItem();
            m.setTitle("料场" + i);
            m.setContent("内容" + i);
            return m;
        }).collect(Collectors.toList());

        String hash1 = service.computeAssemblyHash(
                "KO-PRM-0001", "v3.0",
                List.of("KO-A"), List.of("v1.0"),
                Map.of(), items, Map.of());

        // 同一 items 多次调用产出相同 hash (Q-I4 §3 38 演示值稳定)
        String hash2 = service.computeAssemblyHash(
                "KO-PRM-0001", "v3.0",
                List.of("KO-A"), List.of("v1.0"),
                Map.of(), items, Map.of());

        assertEquals(hash1, hash2, "Q-I4 §3 38 演示值 hash 稳定");
        assertEquals(16, hash1.length());
    }

    @Test
    @DisplayName("Render: hash 不命中时创建 SNP + PRP")
    void render_hashMiss_createsSnpAndPrp() {
        when(promptSnapshotMapper.findByHash(anyString())).thenReturn(null);
        // mock insert: 设回 id 模拟 MyBatis-Plus 主键回填
        org.mockito.Mockito.doAnswer(inv -> {
            PromptSnapshotEntity c = inv.getArgument(0);
            c.setId(System.nanoTime());
            return 1;
        }).when(promptSnapshotMapper).insert(any(PromptSnapshotEntity.class));

        List<ManualSubItem> manualSubItems = new ArrayList<>();
        manualSubItems.add(new ManualSubItem());

        SnapshotService.RenderResult result = service.render(
                "KO-PRM-0001", "v3.0",
                List.of("KO-A"), List.of("v1.0"),
                Map.of("k1", "v1"), manualSubItems, Map.of("var0", "PAR-0"),
                "rendered content", "user-001");

        assertNotNull(result);
        assertNotNull(result.hash);
        assertNotNull(result.snapshotId, "mock insert 应回填 id");
        verify(promptSnapshotMapper, times(1)).insert(any(PromptSnapshotEntity.class));
    }

    @Test
    @DisplayName("Render: hash 命中时复用 SNP, 不创建新 SNP")
    void render_hashHit_reusesSnp() {
        PromptSnapshotEntity existingSnp = new PromptSnapshotEntity();
        existingSnp.setId(99L);
        existingSnp.setHash("existing_hash_value");
        existingSnp.setPrmId("KO-PRM-0001");
        when(promptSnapshotMapper.findByHash(anyString())).thenReturn(existingSnp);

        SnapshotService.RenderResult result = service.render(
                "KO-PRM-0001", "v3.0",
                List.of("KO-A"), List.of("v1.0"),
                Map.of("k1", "v1"), List.of(), Map.of(),
                "rendered content", "user-001");

        assertEquals(99L, result.snapshotId, "复用现有 SNP id 99");
        verify(promptSnapshotMapper, never()).insert(any(PromptSnapshotEntity.class));
    }
}
