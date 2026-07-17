package com.liaogang.famou.km.snapshot.service;

import com.liaogang.famou.km.prompt.dto.ManualSubItem;
import com.liaogang.famou.km.snapshot.model.PromptRecordEntity;
import com.liaogang.famou.km.snapshot.model.PromptSnapshotEntity;
import com.liaogang.famou.km.snapshot.repository.PromptSnapshotMapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 提示词快照服务（U8 / T305 / PRD v0.32 §5.2.4 + OQ-16）
 *
 * <p>ko_assembly_hash 算法（v0.32 §5.2.4）：
 * SHA256(prm_id + prm_version + sorted(ko_ids) + sorted(ko_versions) + sorted(ko_field_values) + manual_subitems_hash + sorted(var_bindings))[:16]
 *
 * <p>命中 hash → 复用 SNP，仅创建 PRP；不命中 → 创建 SNP + PRP。
 *
 * <p>强依赖 Q-I4 §3 ManualSubItems array schema（Q-I4 commit 63606c8 已落地，dual-write dual-read）。
 * 实际持久化 prm_section.content 仍为 TEXT 字段（V9004）；ManualSubItems 通过 dual-write 适配
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SnapshotService {

    private final PromptSnapshotMapper promptSnapshotMapper;

    /**
     * 计算 ko_assembly_hash (16 位 SHA256 截断)
     */
    public String computeAssemblyHash(String prmId, String prmVersion,
                                      List<String> koIds, List<String> koVersions,
                                      Map<String, Object> koFieldValues,
                                      List<ManualSubItem> manualSubItems,
                                      Map<String, String> varBindings) {
        String sortedKoIds = sorted(koIds);
        String sortedKoVersions = sorted(koVersions);
        String sortedFieldValues = sortedMap(koFieldValues);
        String manualSubitemsHash = computeManualSubitemsHash(manualSubItems);
        String sortedVarBindings = sortedMap(varBindings);

        String raw = prmId + "|" + prmVersion + "|" + sortedKoIds + "|" + sortedKoVersions
                + "|" + sortedFieldValues + "|" + manualSubitemsHash + "|" + sortedVarBindings;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest).substring(0, 16);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    /**
     * 渲染 PRM（hash 命中复用 SNP，否则创建 SNP + PRP）
     *
     * @return 渲染结果 (含 SNP + PRP id 引用)
     */
    public RenderResult render(String prmId, String prmVersion,
                                List<String> koIds, List<String> koVersions,
                                Map<String, Object> koFieldValues,
                                List<ManualSubItem> manualSubItems,
                                Map<String, String> varBindings,
                                String renderedText, String userId) {
        String hash = computeAssemblyHash(prmId, prmVersion, koIds, koVersions,
                koFieldValues, manualSubItems, varBindings);

        // 命中 hash → 复用 SNP
        PromptSnapshotEntity snp = promptSnapshotMapper.findByHash(hash);
        if (snp == null) {
            snp = new PromptSnapshotEntity();
            snp.setHash(hash);
            snp.setPrmId(prmId);
            snp.setPrmVersion(prmVersion);
            snp.setRenderedTextCanonical(renderedText);
            snp.setKoIds(joinArray(sorted(koIds)));
            snp.setKoVersions(joinArray(sorted(koVersions)));
            snp.setKoFieldValues(sortedMap(koFieldValues));
            snp.setManualSubitemsHash(computeManualSubitemsHash(manualSubItems));
            snp.setVarBindings(sortedMap(varBindings));
            snp.setStale(false);
            promptSnapshotMapper.insert(snp);
        }

        // 创建 PRP (每次渲染一条; placeholder: PromptRecordMapper 后续接入)
        PromptRecordEntity prp = new PromptRecordEntity();
        prp.setSnapshotHash(hash);
        prp.setRenderedText(renderedText);
        prp.setRenderTime(java.time.LocalDateTime.now());
        prp.setCharCount(renderedText.length());
        prp.setTokenCount((int) Math.ceil(renderedText.length() / 2.0));
        prp.setUserId(userId);
        prp.setForceRendered(false);
        // PRP mapper 暂不注入 (T305 PR 范围外), recordId 留 0
        long recordIdPlaceholder = 0L;

        return new RenderResult(hash, snp.getId(), recordIdPlaceholder);
    }

    /**
     * 113 KO 装配演示值重放（Q-I4 §3 + v0.32 OQ-16）
     * 45 KO 模式 + 15 变量绑定 PARs + 38 手动子项 + 15 引用 SCHs = 113 KO
     */
    public String demo113koAssemblyHash() {
        return computeAssemblyHash(
                "KO-PRM-0001",
                "v3.0",
                java.util.stream.IntStream.range(0, 45).mapToObj(i -> "KO-MAT-" + i).collect(Collectors.toList()),
                java.util.stream.IntStream.range(0, 45).mapToObj(i -> "v" + (i % 3 + 1) + ".0").collect(Collectors.toList()),
                Map.of("key1", "val1", "key2", "val2"),
                java.util.stream.IntStream.range(0, 38).mapToObj(i -> {
                    ManualSubItem m = new ManualSubItem();
                    m.setTitle("料场" + i);
                    m.setContent("内容" + i);
                    return m;
                }).collect(Collectors.toList()),
                java.util.stream.IntStream.range(0, 15).mapToObj(i -> "var" + i).collect(Collectors.toMap(i -> "ko" + i, i -> "PAR-" + i))
        );
    }

    private String computeManualSubitemsHash(List<ManualSubItem> items) {
        if (items == null || items.isEmpty()) return "empty";
        String joined = items.stream()
                .map(it -> (it.getTitle() == null ? "" : it.getTitle())
                        + "|" + (it.getContent() == null ? "" : it.getContent()))
                .collect(Collectors.joining(";"));
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(joined.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest).substring(0, 12);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    private <T> String sorted(List<T> list) {
        if (list == null || list.isEmpty()) return "";
        return list.stream()
                .map(Object::toString)
                .sorted()
                .collect(Collectors.joining(","));
    }

    private String sortedMap(Map<?, ?> map) {
        if (map == null || map.isEmpty()) return "";
        return map.entrySet().stream()
                .sorted((a, b) -> String.valueOf(a.getKey()).compareTo(String.valueOf(b.getKey())))
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining(";"));
    }

    private String joinArray(String s) {
        return s;
    }

    public static class RenderResult {
        public final String hash;
        public final Long snapshotId;
        public final Long recordId;

        public RenderResult(String hash, Long snapshotId, Long recordId) {
            this.hash = hash;
            this.snapshotId = snapshotId;
            this.recordId = recordId;
        }
    }
}
