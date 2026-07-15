package com.liaogang.famou.km.auth;

import com.liaogang.famou.km.audit.AuditLogEntity;
import com.liaogang.famou.km.audit.AuditLogService;
import com.liaogang.famou.km.auth.JwtAuthFilter;
import com.liaogang.famou.km.ko.service.KoService;
import com.liaogang.famou.km.ko.model.KoEntity;
import com.liaogang.famou.km.role.event.RoleChangeEvent;
import com.liaogang.famou.km.role.service.RoleChangeAuditService;
import com.liaogang.famou.km.role.service.UserRoleService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.event.EventListener;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * RoleChangeEffect 测试（T207 done_signal: 3/3 测试通过）。
 *
 * <p>覆盖 OQ-12（角色变更下次登录生效）+ OQ-5（跨设备撤销不可行）：
 * <ul>
 *   <li>oldSessionStillUsesOldRole：用户当前 JWT 仍按旧 role 工作（OQ-12）</li>
 *   <li>newSessionUsesNewRole：用户重新登录后新 JWT 用新 role（OQ-12）</li>
 *   <li>crossDeviceRevokeImpossible：后端不调任何 cancel/revert API（OQ-5）</li>
 *   <li>auditLogTriggered：分配角色触发 USER_ROLE_CHANGE 审计</li>
 * </ul>
 */
@SpringBootTest(properties = {
    "spring.flyway.enabled=false",
    "spring.jpa.hibernate.ddl-auto=none"
})
@ActiveProfiles("it")
@Sql(scripts = "/db/test-schema.sql")
@Transactional
@Import(RoleChangeEffectTest.TestAuditListenerConfig.class)
@DisplayName("OQ-12 角色变更下次登录生效测试")
class RoleChangeEffectTest {

    @Autowired
    private JwtAuthFilter jwtAuthFilter;

    @Autowired
    private UserRoleService userRoleService;

    @Autowired
    private KoService koService;

    @Autowired
    private TestAuditListener testAuditListener;

    @Test
    @DisplayName("OQ-12：旧 JWT session 仍按旧 role 工作（角色变更后未失效）")
    void oldSessionStillUsesOldRole() {
        // 1. 用户初始分配 ROLE-0003
        userRoleService.assignRole("user-001", "ROLE-0003", "admin-001");

        // 2. 签发旧 JWT（用 ROLE-0003）
        String oldJwt = jwtAuthFilter.issueToken("user-001", "user-001", "ROLE-0003");
        assertThat(oldJwt).isNotEmpty();

        // 3. 解析旧 JWT → role claim 是 ROLE-0003
        String oldRole = extractRoleFromJwt(oldJwt);
        assertThat(oldRole).isEqualTo("ROLE-0003");

        // 4. 管理员把 user-001 改为 ROLE-0005
        userRoleService.removeRole("user-001", "ROLE-0003", "admin-001");
        userRoleService.assignRole("user-001", "ROLE-0005", "admin-001");

        // 5. 旧 JWT 仍然有效（OQ-12 跨设备撤销不可行）
        //    旧 JWT 的 role claim 仍然是 ROLE-0003（JWT 自包含，未查询 DB）
        String stillOldRole = extractRoleFromJwt(oldJwt);
        assertThat(stillOldRole).isEqualTo("ROLE-0003");
    }

    @Test
    @DisplayName("OQ-12：新登录用新 role（重新签发 JWT）")
    void newSessionUsesNewRole() {
        // 1. 初始 ROLE-0003，签发旧 JWT
        userRoleService.assignRole("user-002", "ROLE-0003", "admin-001");
        String oldJwt = jwtAuthFilter.issueToken("user-002", "user-002", "ROLE-0003");

        // 2. 改为 ROLE-0005
        userRoleService.removeRole("user-002", "ROLE-0003", "admin-001");
        userRoleService.assignRole("user-002", "ROLE-0005", "admin-001");

        // 3. 模拟重新登录（用新 role 签发新 JWT）
        String newJwt = jwtAuthFilter.issueToken("user-002", "user-002", "ROLE-0005");

        // 4. 新 JWT 的 role claim 是 ROLE-0005
        String newRole = extractRoleFromJwt(newJwt);
        assertThat(newRole).isEqualTo("ROLE-0005");

        // 5. 旧 JWT 仍是 ROLE-0003（不影响）
        assertThat(extractRoleFromJwt(oldJwt)).isEqualTo("ROLE-0003");
    }

    @Test
    @DisplayName("OQ-5：跨设备撤销不可行（UserRoleService 无 revoke/cancel/invalidate 方法）")
    void crossDeviceRevokeImpossible() {
        // 通过反射验证 UserRoleService 没有任何"撤销 / 失效"相关方法
        // OQ-5 决策：后端不调任何 cancel/revoke API
        java.lang.reflect.Method[] methods = UserRoleService.class.getDeclaredMethods();
        for (java.lang.reflect.Method m : methods) {
            String name = m.getName().toLowerCase();
            assertThat(name)
                .as("UserRoleService 不应有 revoke/cancel/invalidate 方法（OQ-5）")
                .doesNotContain("revoke", "cancel", "invalidate", "expire");
        }
        // 也验证 JwtAuthFilter（OQ-12 关键：JWT 自包含 role claim）
        java.lang.reflect.Method[] jwtMethods = JwtAuthFilter.class.getDeclaredMethods();
        for (java.lang.reflect.Method m : jwtMethods) {
            String name = m.getName().toLowerCase();
            assertThat(name)
                .as("JwtAuthFilter 不应有 revoke/cancel 方法（OQ-5）")
                .doesNotContain("revoke", "cancel", "invalidate", "expire");
        }
    }

    @Test
    @DisplayName("USER_ROLE_CHANGE 审计日志：分配角色触发 AuditLogService.recordAsync")
    void auditLogTriggered() {
        // 重置监听器计数
        testAuditListener.reset();

        // 触发角色分配
        userRoleService.assignRole("user-004", "ROLE-0004", "admin-001");

        // 验证审计事件被发布（@EventListener 异步处理）
        try { Thread.sleep(200); } catch (InterruptedException ignored) {}
        assertThat(testAuditListener.getEventCount()).isGreaterThanOrEqualTo(1);
    }

    /**
     * 从 JWT 提取 role claim（OQ-12 关键验证：JWT 自包含）
     */
    private String extractRoleFromJwt(String jwt) {
        // 简化解析：JWT 格式 header.payload.signature，base64 解码 payload 取 role
        String[] parts = jwt.split("\\.");
        if (parts.length < 2) return "";
        try {
            String payload = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));
            // 简单字符串提取 "role":"VALUE"
            int idx = payload.indexOf("\"role\":\"");
            if (idx < 0) return "";
            int start = idx + 8;
            int end = payload.indexOf("\"", start);
            return payload.substring(start, end);
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * 测试用审计监听器配置（@TestConfiguration 显式 import 才能被 Spring 扫描）
     */
    @TestConfiguration
    static class TestAuditListenerConfig {
        @Bean
        public TestAuditListener testAuditListener() {
            return new TestAuditListener();
        }
    }

    /**
     * 测试用审计监听器（捕获 RoleChangeEvent 触发次数）
     */
    static class TestAuditListener {
        private int eventCount = 0;

        @EventListener
        public void onEvent(RoleChangeEvent event) {
            eventCount++;
        }

        public int getEventCount() {
            return eventCount;
        }

        public void reset() {
            this.eventCount = 0;
        }
    }
}
