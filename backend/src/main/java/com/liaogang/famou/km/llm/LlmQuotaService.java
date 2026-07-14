package com.liaogang.famou.km.llm;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * LLM 配额管理服务（v0.32 PRD §5.2.3 OQ-T01 + §6）。
 *
 * <p>配额分配：平台级 80% / 用户级 20%（OQ-T01 决议）
 * <p>计数：Redis INCR（每日 0 点重置）
 * <p>检查：每次 LLM 调用前 quota.incrementAndCheck(userSub)
 * <p>耗尽：返回 false，调用方应降级为降级策略（不上 LLM，仅显示 mock 建议）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LlmQuotaService {

    @Value("${app.llm.quota.platform-percentage:80}")
    private int platformPercentage;

    @Value("${app.llm.quota.user-percentage:20}")
    private int userPercentage;

    @Value("${app.llm.quota.daily-platform-limit:1000}")
    private int dailyPlatformLimit;

    private final StringRedisTemplate redisTemplate;
    private final java.util.Map<String, Long> mockCounterMap = new java.util.concurrent.ConcurrentHashMap<>();

    /**
     * 平台级 key（PR-01）
     */
    private static final String PLATFORM_KEY_PREFIX = "llm:quota:platform:";

    /**
     * 用户级 key（PR-01）
     */
    private static final String USER_KEY_PREFIX = "llm:quota:user:";

    /**
     * 申请 LLM 调用配额（增加计数 + 检查限额）
     *
     * @param userSub 用户 sub（来自 JWT）
     * @return true = 通过；false = 配额耗尽
     */
    // F-3 修复：Redis Lua 原子脚本（CAS）替代 INCR-then-check TOCTOU
    // 脚本语义：先 INCR 两个 key，再检查是否超过限额，若超过则 DECR 回滚
    private static final String LUA_INCR_AND_CHECK = "        local platformCount = redis.call('INCR', KEYS[1])
        local userCount = redis.call('INCR', KEYS[2])
        if redis.call('EXISTS', KEYS[3]) == 0 then
            redis.call('EXPIRE', KEYS[1], 86400)
            redis.call('EXPIRE', KEYS[2], 86400)
        end
        local platformLimit = tonumber(ARGV[1])
        local userLimit = tonumber(ARGV[2])
        local platformOk = platformCount <= platformLimit
        local userOk = userCount <= userLimit
        if not (platformOk and userOk) then
            redis.call('DECR', KEYS[1])
            redis.call('DECR', KEYS[2])
            return {0, platformCount, userCount}
        end
        return {1, platformCount, userCount}";

    public boolean incrementAndCheck(String userSub) {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String platformKey = PLATFORM_KEY_PREFIX + today;
        String userKey = USER_KEY_PREFIX + today + ":" + userSub;

        // 计算每日限额（按比例）
        int platformLimit = dailyPlatformLimit;
        int userLimit = (int) (dailyPlatformLimit * userPercentage / 100.0);

        try {
            // 原子 INCR + 检查 + 超限回滚（Lua 脚本）
            org.springframework.data.redis.core.script.RedisScript<java.util.List> script = org.springframework.data.redis.core.script
                .RedisScript.of(LUA_INCR_AND_CHECK, java.util.List.class);
            java.util.List result = redisTemplate.execute(script, java.util.List.of(platformKey, userKey, today), platformLimit, userLimit);
            boolean allowed = ((Number) result.get(0)).intValue() == 1;
            long platformCount = ((Number) result.get(1)).longValue();
            long userCount = ((Number) result.get(2)).longValue();
            if (!allowed) {
                log.warn("LLM 配额耗尽: platform={}/{}, user={}/{}", platformCount, platformLimit, userCount, userLimit);
            }
            return allowed;
        } catch (Exception e) {
            // Redis 不可用时降级为本地内存版本（仅 dev/test）
            log.warn("F-3 Lua 脚本执行失败，降级到内存检查: {}", e.getMessage());
            return mockIncrementAndCheck(userSub);
        }
    }

    private long increment(String key) {
        // 注：生产路径已通过 Lua 脚本原子执行，不再单独调用此方法
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1L) {
            redisTemplate.expire(key, java.time.Duration.ofDays(1));
        }
        return count == null ? 0L : count;
    }

    // F-3 降级路径：Redis 不可用时的内存检查
    private boolean mockIncrementAndCheck(String userSub) {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String platformKey = PLATFORM_KEY_PREFIX + today;
        String userKey = USER_KEY_PREFIX + today + ":" + userSub;
        long platformCount = mockCounterMap.compute(platformKey, (k, v) -> (v == null) ? 1L : v + 1);
        long userCount = mockCounterMap.compute(userKey, (k, v) -> (v == null) ? 1L : v + 1);
        int platformLimit = dailyPlatformLimit;
        int userLimit = (int) (dailyPlatformLimit * userPercentage / 100.0);
        if (platformCount > platformLimit || userCount > userLimit) {
            // 回滚
            mockCounterMap.computeIfPresent(platformKey, (k, v) -> Math.max(0, v - 1));
            mockCounterMap.computeIfPresent(userKey, (k, v) -> Math.max(0, v - 1));
            return false;
        }
        return true;
    }

    /**
     * 查询当前配额使用情况
     */
    public QuotaStatus getStatus(String userSub) {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String platformCount = redisTemplate.opsForValue().get(PLATFORM_KEY_PREFIX + today);
        String userCount = redisTemplate.opsForValue().get(USER_KEY_PREFIX + today + ":" + userSub);

        int platformLimit = dailyPlatformLimit;
        int userLimit = (int) (dailyPlatformLimit * userPercentage / 100.0);

        return new QuotaStatus(
            parseInt(platformCount, 0), platformLimit,
            parseInt(userCount, 0), userLimit);
    }

    private int parseInt(String s, int def) {
        if (s == null) return def;
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return def;
        }
    }

    @lombok.Data
    public static class QuotaStatus {
        private final int platformUsed;
        private final int platformLimit;
        private final int userUsed;
        private final int userLimit;

        public QuotaStatus(int platformUsed, int platformLimit, int userUsed, int userLimit) {
            this.platformUsed = platformUsed;
            this.platformLimit = platformLimit;
            this.userUsed = userUsed;
            this.userLimit = userLimit;
        }
    }
}
