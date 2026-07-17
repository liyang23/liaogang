package com.liaogang.famou.km.governance.service;

import com.liaogang.famou.km.governance.model.ConflictEntity;
import com.liaogang.famou.km.governance.repository.ConflictMapper;
import com.liaogang.famou.km.ko.model.KoEntity;
import com.liaogang.famou.km.ko.model.KoVersionEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 冲突检测服务（U7 / T301 / PRD §5.2.3.1）
 *
 * <p>6 C 类冲突 + 1 H 类健康：
 * <ul>
 *   <li>C1 字段值冲突 — 同一字段在不同 KO 中值不同
 *   <li>C2 模板不一致 — PRM 模板结构差异
 *   <li>C3 字段类型冲突 — 字段类型不一致
 *   <li>C4 引用过期 — 引用的 PAR/SCH 已变更
 *   <li>C5 时序冲突 — 草稿期间被修改（Sprint 3 范围外）
 *   <li>C6 命名歧义 — 标题/标识符相似度高
 *   <li>H2 数据漂移 — 实际数据与字段定义漂移
 * </ul>
 *
 * <p>冲突指纹算法（§5.2.3.1）：MD5(ko_a_id + ko_b_id + conflict_type + scope_key + field_key)[:12]
 * 命中指纹 → 沿用旧 id（不创建新记录）。
 *
 * <p>性能：O(n) single KO 检测；O(n^2) pairwise 检测（113 KO 场景约 6.4K pair checks / 次扫描可接受）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConflictDetector {

    private final ConflictMapper conflictMapper;

    /**
     * 计算冲突指纹
     */
    public String fingerprint(String koAId, String koBId, String conflictType,
                              String scopeKey, String fieldKey) {
        String raw = koAId + "|" + koBId + "|" + conflictType + "|" + scopeKey + "|" + fieldKey;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest).substring(0, 12);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("MD5 not available", e);
        }
    }

    /**
     * 单 KO 扫描（写时检测触发点）— 检测该 KO 与其他 KO 的冲突
     * 返回新发现的冲突（不含已存在的）
     */
    public List<ConflictEntity> detectForKo(KoEntity target, List<KoEntity> others) {
        List<ConflictEntity> newConflicts = new ArrayList<>();
        for (KoEntity other : others) {
            if (Objects.equals(target.getId(), other.getId())) continue;
            // C1 definition 冲突 — 同 type 但 definition 描述不一致
            ConflictEntity c1 = detectC1FieldValue(target, other);
            if (c1 != null && !exists(c1.getFingerprint())) {
                newConflicts.add(c1);
            }
            // C3 effect 冲突 — 同 type 但 effect 效力分级不一致
            ConflictEntity c3 = detectC3FieldType(target, other);
            if (c3 != null && !exists(c3.getFingerprint())) {
                newConflicts.add(c3);
            }
            // C6 命名歧义 — 标题编辑距离 ≤3
            ConflictEntity c6 = detectC6NamingAmbiguity(target, other);
            if (c6 != null && !exists(c6.getFingerprint())) {
                newConflicts.add(c6);
            }
        }
        return newConflicts;
    }

    /** C1 definition 冲突 — 同 type 但 definition 描述不一致 (placeholder, KO schema 扩展后接入 field_values) */
    public ConflictEntity detectC1FieldValue(KoEntity a, KoEntity b) {
        if (!Objects.equals(a.getType(), b.getType())) return null;
        if (a.getDefinition() != null && b.getDefinition() != null
                && !a.getDefinition().isBlank() && !a.getDefinition().equals(b.getDefinition())) {
            return buildConflict("C1", a.getId(), b.getId(), "ko_type:" + a.getType(), "definition", 0.75);
        }
        return null;
    }

    /** C2 模板不一致 — 基于 KoVersion.definition 差异 (placeholder, KoVersion 暂无 section_count 字段) */
    public ConflictEntity detectC2TemplateInconsistent(KoVersionEntity a, KoVersionEntity b) {
        if (a.getDefinition() != null && b.getDefinition() != null
                && !a.getDefinition().isBlank() && !a.getDefinition().equals(b.getDefinition())) {
            return buildConflict("C2", a.getKoId(), b.getKoId(),
                    "ko_version",
                    "definition",
                    0.7);
        }
        return null;
    }

    /** C3 字段类型冲突 — 字段类型不一致 */
    public ConflictEntity detectC3FieldType(KoEntity a, KoEntity b) {
        // placeholder: 实际通过 schema 字典对比
        return null;
    }

    /** C4 引用过期 — 引用的 PAR 已变更（U8 陈旧快照扫描触发） */
    public ConflictEntity detectC4ReferenceStale(KoEntity a, KoEntity b) {
        // placeholder: 由 U8 StaleSnapshotJob 触发
        return null;
    }

    /** C5 时序冲突 — Sprint 3 范围外（OQ-T04 deferred 到 Sprint 5+） */
    public ConflictEntity detectC5TemporalConflict(KoEntity a, KoEntity b) {
        return null;
    }

    /** C6 命名歧义 — 标题/标识符相似度高（编辑距离 < 3） */
    public ConflictEntity detectC6NamingAmbiguity(KoEntity a, KoEntity b) {
        if (a.getTitle() == null || b.getTitle() == null) return null;
        int dist = levenshtein(a.getTitle(), b.getTitle());
        if (dist > 0 && dist <= 3) {
            // C6 置信度与编辑距离反比
            double confidence = Math.max(0.5, 1.0 - dist * 0.15);
            return buildConflict("C6", a.getId(), b.getId(), "title", "title", confidence);
        }
        return null;
    }

    /** H2 数据漂移 — 实际数据与字段定义漂移 */
    public ConflictEntity detectH2DataDrift(KoEntity a, KoEntity b, String fieldKey) {
        return buildConflict("H2", a.getId(), b.getId(), "data_drift", fieldKey, 0.6);
    }

    private boolean exists(String fingerprint) {
        return conflictMapper.findByFingerprint(fingerprint) != null;
    }

    private ConflictEntity buildConflict(String type, String koAId, String koBId,
                                        String scopeKey, String fieldKey, double confidence) {
        ConflictEntity c = new ConflictEntity();
        c.setFingerprint(fingerprint(koAId, koBId, type, scopeKey, fieldKey));
        c.setConflictType(type);
        c.setKoAId(koAId);
        c.setKoBId(koBId);
        c.setScopeKey(scopeKey);
        c.setFieldKey(fieldKey);
        c.setStatus("pending");
        c.setConfidence(confidence);
        c.setResolutionState("draft"); // OQ-8 仲裁快路径起点
        return c;
    }

    /** Levenshtein 编辑距离（用于 C6 命名相似度） */
    private int levenshtein(String a, String b) {
        int[] costs = new int[b.length() + 1];
        for (int j = 0; j < costs.length; j++) costs[j] = j;
        for (int i = 1; i <= a.length(); i++) {
            costs[0] = i;
            int nw = i - 1;
            for (int j = 1; j <= b.length(); j++) {
                int cj = Math.min(1 + Math.min(costs[j], costs[j - 1]),
                        a.charAt(i - 1) == b.charAt(j - 1) ? nw : nw + 1);
                nw = costs[j];
                costs[j] = cj;
            }
        }
        return costs[b.length()];
    }
}
