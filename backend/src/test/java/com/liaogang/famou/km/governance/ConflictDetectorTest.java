package com.liaogang.famou.km.governance;

import com.liaogang.famou.km.governance.model.ConflictEntity;
import com.liaogang.famou.km.governance.repository.ConflictMapper;
import com.liaogang.famou.km.governance.service.ConflictDetector;
import com.liaogang.famou.km.ko.model.KoEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * U7 / T301 / ConflictDetector 6 类检测 + 1 H 类健康 + 指纹算法去重 (PRD §5.2.3.1)
 *
 * 5+ 测试覆盖 happy path + edge case + error path
 */
@ExtendWith(MockitoExtension.class)
class ConflictDetectorTest {

    @Mock
    private ConflictMapper conflictMapper;

    @InjectMocks
    private ConflictDetector detector;

    private KoEntity koA;
    private KoEntity koB;

    @BeforeEach
    void setUp() {
        koA = new KoEntity();
        koA.setId("KO-MAT-0042");
        koA.setTitle("料场库存上下限");
        koA.setType("MAT");
        koA.setDefinition("料场上限 1000 吨, 下限 850 吨");

        koB = new KoEntity();
        koB.setId("KO-MAT-0043");
        koB.setTitle("料场库存上下限");
        koB.setType("MAT");
        koB.setDefinition("料场上限 1100 吨, 下限 900 吨");
    }

    @Test
    @DisplayName("C1 字段值冲突检测 — 同字段不同值")
    void c1_fieldValueConflict_detected() {
        ConflictEntity c = detector.detectC1FieldValue(koA, koB);
        assertNotNull(c, "C1 冲突应被检测出");
        assertEquals("C1", c.getConflictType());
        assertEquals("KO-MAT-0042", c.getKoAId());
        assertEquals("KO-MAT-0043", c.getKoBId());
        assertNotNull(c.getFingerprint());
        assertEquals(12, c.getFingerprint().length(), "指纹 MD5 截断 12 位");
        assertTrue(c.getConfidence() > 0.5, "C1 字段值冲突置信度 > 0.5");
    }

    @Test
    @DisplayName("C6 命名歧义 — 标题编辑距离 ≤3 触发")
    void c6_namingAmbiguity_levenshtein3_triggers() {
        koB.setTitle("料场库存上下线");  // 编辑距离 1 (限→线)
        ConflictEntity c = detector.detectC6NamingAmbiguity(koA, koB);
        assertNotNull(c, "C6 命名歧义应被检测出");
        assertEquals("C6", c.getConflictType());
        assertTrue(c.getConfidence() >= 0.5);
    }

    @Test
    @DisplayName("C6 命名歧义 — 标题编辑距离 >3 不触发")
    void c6_namingAmbiguity_levenshteinGreaterThan3_noTrigger() {
        koB.setTitle("完全不同的另一个 KO 标题");
        ConflictEntity c = detector.detectC6NamingAmbiguity(koA, koB);
        assertNull(c, "编辑距离 > 3 不应触发 C6");
    }

    @Test
    @DisplayName("指纹算法 — 相同输入产出相同指纹 (12 位 MD5 截断)")
    void fingerprint_sameInputSameFingerprint() {
        String fp1 = detector.fingerprint("KO-A", "KO-B", "C1", "MAT:min", "min");
        String fp2 = detector.fingerprint("KO-A", "KO-B", "C1", "MAT:min", "min");
        assertEquals(fp1, fp2);
        assertEquals(12, fp1.length());
    }

    @Test
    @DisplayName("指纹算法 — 不同 scope_key 产出不同指纹")
    void fingerprint_differentScope_differentFingerprint() {
        String fp1 = detector.fingerprint("KO-A", "KO-B", "C1", "MAT:min", "min");
        String fp2 = detector.fingerprint("KO-A", "KO-B", "C1", "MAT:max", "min");
        assertFalse(fp1.equals(fp2), "不同 scope_key 应产出不同指纹");
    }

    @Test
    @DisplayName("detectForKo — C1 + C6 双重检测 (happy path)")
    void detectForKo_c1AndC6_bothDetected() {
        koB.setTitle("料场库存上下线");  // 同名变体触发 C6
        when(conflictMapper.findByFingerprint(org.mockito.ArgumentMatchers.anyString()))
                .thenReturn(null);  // 指纹未命中
        List<ConflictEntity> conflicts = detector.detectForKo(koA, List.of(koB));
        assertNotNull(conflicts);
        // 至少包含 C1 (字段值冲突) 和 C6 (命名歧义)
        assertTrue(conflicts.stream().anyMatch(c -> "C1".equals(c.getConflictType())));
        assertTrue(conflicts.stream().anyMatch(c -> "C6".equals(c.getConflictType())));
    }

    @Test
    @DisplayName("detectForKo — 自身跳过 (same KO id)")
    void detectForKo_skipSelf() {
        // self-skip 路径不调用 findByFingerprint, 无需 mock
        List<ConflictEntity> conflicts = detector.detectForKo(koA, List.of(koA));
        assertTrue(conflicts.isEmpty(), "自身 KO 不应被检测为冲突");
    }
}
