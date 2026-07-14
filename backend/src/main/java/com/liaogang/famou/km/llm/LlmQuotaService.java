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
    public boolean incrementAndCheck(String userSub) {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        long platformCount = increment(PLATFORM_KEY_PREFIX + today);
        long userCount = increment(USER_KEY_PREFIX + today + ":" + userSub);

        // 计算每日限额（按比例）
        int platformLimit = dailyPlatformLimit;
        int userLimit = (int) (dailyPlatformLimit * userPercentage / 100.0);

        boolean platformOk = platformCount <= platformLimit;
        boolean userOk = userCount <= userLimit;

        if (!platformOk || !userOk) {
            log.warn("LLM 配额耗尽: platform={}/{}, user={}/{}",
                    platformCount, platformLimit, userCount, userLimit);
            return false;
        }
        return true;
    }

    private long increment(String key) {
        Long count = redisTemplate.opsForValue().increment(key);
        // 设置每日过期（首次增加时）
        if (count != null && count == 1L) {
            redisTemplate.expire(key, java.time.Duration.ofDays(1));
        }
        return count == null ? 0L : count;
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
