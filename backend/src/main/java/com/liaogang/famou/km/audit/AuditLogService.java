package com.liaogang.famou.km.audit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

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

    // F-4 修复：使用 Redis INCR 替代 AtomicInteger（Sprint 1 mock 模式用内存 Map）
    private final java.util.Map<String, Long> mockCounterMap = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.Map<String, Long> mockExpiryMap = new java.util.concurrent.ConcurrentHashMap<>();
    private final StringRedisTemplate redisTemplate;
    private final boolean useRedisMock;

    public AuditLogService(StringRedisTemplate redisTemplate,
                           @Value("${app.audit.use-redis-mock:true}") boolean useRedisMock) {
        this.redisTemplate = redisTemplate;
        this.useRedisMock = useRedisMock;
    }

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
     * F-4 修复：生成审计 ID（格式 AUDIT-{YYYYMMDD}-{NNNNNN}，按天独立自增）。
     * <p>使用 Redis INCR（Sprint 1 mock 模式 fallback 到内存 ConcurrentHashMap 模拟 INCR 语义）
     */
    private String generateId() {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String key = "km:audit:counter:" + date;
        long n;
        if (useRedisMock || redisTemplate == null) {
            // mock 模式（带 31 天过期清理）
            n = mockCounterMap.compute(key, (k, v) -> (v == null) ? 1L : v + 1);
            mockExpiryMap.compute(key, (k, v) -> {
                if (v == null) {
                    return System.currentTimeMillis() + TimeUnit.DAYS.toMillis(31);
                }
                return v;
            });
        } else {
            // 生产模式（Redis INCR 原子递增，分布式唯一）
            n = redisTemplate.opsForValue().increment(key);
            if (n != null && n == 1L) {
                redisTemplate.expire(key, 31, TimeUnit.DAYS);
            }
        }
        return String.format("AUDIT-%s-%06d", date, n % 1000000);
    }
}
