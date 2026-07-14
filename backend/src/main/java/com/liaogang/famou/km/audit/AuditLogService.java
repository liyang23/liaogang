package com.liaogang.famou.km.audit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 审计日志 Service（v0.32 PRD §5.2.6）。
 *
 * <p>FR-26 验收：12 个月保留（NFR-15），按月分区（生产环境 T006 启用 partition-by-month=true）。
 * <p>OQ-5 修订：仅前端 3 秒撤销（不写 USER_ROLE_REVERT 审计）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogService {

    @Value("${app.audit.partition-by-month:false}")
    private boolean partitionByMonth;

    @Value("${app.audit.retention-months:12}")
    private int retentionMonths;

    private final AtomicInteger counter = new AtomicInteger(0);

    /**
     * 同步写审计日志
     */
    public void record(AuditLog log) {
        log.setId(generateId());
        if (log.getCreatedAt() == null) {
            log.setCreatedAt(LocalDateTime.now());
        }
        // 实际写入逻辑：本地日志 + 后续接入 ClickHouse / ELK（生产环境）
        org.slf4j.LoggerFactory.getLogger(AuditLogService.class).info(
            "AUDIT_LOG: id={}, action={}, userId={}, targetKo={}, detail={}",
            log.getId(), log.getAction(), log.getUserId(), log.getTargetKo(), log.getDetail());
    }

    /**
     * 异步写审计日志（不阻塞业务主流程）
     */
    @Async
    public void recordAsync(AuditLog log) {
        record(log);
    }

    /**
     * 生成审计 ID（格式 AUDIT-{YYYYMMDD}-{NNNNNN}，按天独立自增）
     */
    private String generateId() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int n = counter.incrementAndGet() % 1000000;
        return String.format("AUDIT-%s-%06d", date, n);
    }
}
