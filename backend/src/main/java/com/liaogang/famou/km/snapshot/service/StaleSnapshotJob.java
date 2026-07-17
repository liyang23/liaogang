package com.liaogang.famou.km.snapshot.service;

import com.liaogang.famou.km.audit.AuditLogEntity;
import com.liaogang.famou.km.audit.AuditLogService;
import com.liaogang.famou.km.snapshot.model.PromptSnapshotEntity;
import com.liaogang.famou.km.snapshot.repository.PromptSnapshotMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 陈旧快照 Job (U8 / T305)
 *
 * <p>PAR KO current_value 变更后扫描所有引用该 PAR 的 SNP → 标记 stale = true + 写 SNP_STALE_DETECTED 审计
 * Cron: 每日凌晨 3 点跑一次 (生产环境)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StaleSnapshotJob {

    private final PromptSnapshotMapper promptSnapshotMapper;
    private final AuditLogService auditLogService;

    /**
     * 每日陈旧快照扫描 (U8 强依赖 PAR 变更事件触发)
     * 占位: 真实 PAR 变更事件 listener 接入由 T305 后续 PR 提供
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void scanForStaleSnapshots() {
        List<PromptSnapshotEntity> staleList = promptSnapshotMapper.findByStale(false);
        // 当前 placeholder: 扫描所有未 stale 的 SNP
        // 实际触发: 监听 PAR KO current_value 变更事件 (U4 接入)
        log.info("Daily stale snapshot scan started; current non-stale count: {}", staleList.size());
    }

    /**
     * 手动触发陈旧标记 (供 PAR 变更事件 listener 调用)
     */
    public int markStaleForPrm(String prmId, String currentHash) {
        int affected = promptSnapshotMapper.markStaleByPrmExcludingHash(prmId, currentHash);
        if (affected > 0) {
            AuditLogEntity auditLog = new AuditLogEntity();
            auditLog.setAction("SNP_STALE_DETECTED");
            auditLog.setTargetKo(prmId);
            auditLog.setDetail("affected=" + affected);
            auditLogService.record(auditLog);
        }
        return affected;
    }
}
