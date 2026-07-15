package com.liaogang.famou.km.audit;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * AuditAspect 集成测试（it profile；mvn failsafe 触发）。
 * <p>验证：@AuditLog 注解方法触发时，AuditAspect AOP 拦截并写 AUDIT_LOG 日志。
 * <p>F-12 修复回归：成功 + 失败路径都写审计日志（action + "_FAILED" 后缀）。
 * <p>当前 AuditLogService 走日志落盘（PRD §5.2.6 后续接 ClickHouse/ELK），
 *     本测试用 Logback ListAppender 拦截；audit_log 表实装后追加 JDBC 验证。
 */
@SpringBootTest(properties = {
    // F-25 修复：AuditAspectIT 不依赖业务表，跳过 Flyway V9001 seed 迁移
    // （V9001 假设 role/ko 等表已存在，但 Sprint 1 范围无建表 migration；U4/U9 实装时补）
    "spring.flyway.enabled=false",
    "spring.jpa.hibernate.ddl-auto=none"
})
@ActiveProfiles("it")
@Import(AuditAspectIT.TestServiceConfig.class)
class AuditAspectIT {

    @Autowired
    private AuditedTestService auditedService;

    private ListAppender<ILoggingEvent> listAppender;
    private Logger auditLogger;

    @BeforeEach
    void setUp() {
        // 1. 拦截 AuditLogService 类的 INFO 日志（"AUDIT_LOG: id=..., action=..., ..."）
        auditLogger = (Logger) LoggerFactory.getLogger(AuditLogService.class);
        listAppender = new ListAppender<>();
        listAppender.start();
        auditLogger.addAppender(listAppender);

        // 2. 模拟当前用户（避免依赖 SecurityContextHolder 链）
        Authentication auth = mock(Authentication.class);
        // 测试用例中按需覆盖 userId
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        auditLogger.detachAppender(listAppender);
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("成功调用 @AuditLog 方法 → 写 AUDIT_LOG 日志（含 ID/Action/UserId/SUCCESS）")
    void shouldWriteAuditLogOnSuccess() {
        // given
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        when(auth.getPrincipal()).thenReturn("test-user-001");

        // when
        String result = auditedService.doSomething("ko-123", "PAR");

        // then
        assertThat(result).isEqualTo("ok:ko-123:PAR");
        waitForAsync();
        assertThat(auditMessages())
            .anyMatch(m -> m.contains("AUDIT_LOG")
                && m.contains("action=KO_UPDATE")
                && m.contains("userId=test-user-001")
                && m.contains("SUCCESS"));
    }

    @Test
    @DisplayName("失败调用 @AuditLog 方法 → 写 AUDIT_LOG 日志（action + _FAILED + FAILURE）")
    void shouldWriteAuditLogOnFailure() {
        // given
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        when(auth.getPrincipal()).thenReturn("test-user-002");

        // when
        try {
            auditedService.throwError("ko-456");
        } catch (IllegalStateException expected) {
            // 预期异常，验证 finally 块仍写审计
        }

        // then
        waitForAsync();
        assertThat(auditMessages())
            .anyMatch(m -> m.contains("AUDIT_LOG")
                && m.contains("action=KO_DELETE_FAILED")
                && m.contains("userId=test-user-002")
                && m.contains("FAILURE"));
    }

    @Test
    @DisplayName("审计 ID 格式：AUDIT-YYYYMMDD-NNNNNN（日期当天 + 6 位自增）")
    void shouldGenerateAuditIdWithFormat() {
        // given
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        when(auth.getPrincipal()).thenReturn("test-user-003");

        // when
        auditedService.doSomething("ko-789", "RUL");

        // then
        waitForAsync();
        assertThat(auditMessages())
            .filteredOn(m -> m.contains("AUDIT_LOG"))
            .anyMatch(m -> m.matches(".*id=AUDIT-\\d{8}-\\d{6}.*"));
    }

    @Test
    @DisplayName("未认证调用 → userId=anonymous（不抛错，优雅降级）")
    void shouldHandleAnonymousUser() {
        // given
        SecurityContextHolder.clearContext();

        // when
        auditedService.doSomething("ko-anon", "CON");

        // then
        waitForAsync();
        assertThat(auditMessages())
            .anyMatch(m -> m.contains("AUDIT_LOG") && m.contains("userId=anonymous"));
    }

    private java.util.List<String> auditMessages() {
        return listAppender.list.stream()
            .map(ILoggingEvent::getFormattedMessage)
            .collect(java.util.stream.Collectors.toList());
    }

    private void waitForAsync() {
        // @Async recordAsync 让写日志异步；轮询直到 AUDIT_LOG 出现或超时
        long deadline = System.currentTimeMillis() + 2000;
        while (System.currentTimeMillis() < deadline) {
            boolean hasAny = listAppender.list.stream()
                .anyMatch(e -> e.getFormattedMessage().contains("AUDIT_LOG"));
            if (hasAny) {
                return;
            }
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    @TestConfiguration
    static class TestServiceConfig {
        @Bean
        AuditedTestService auditedTestService() {
            return new AuditedTestService();
        }
    }
}
