package com.liaogang.famou.km;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Spring Boot 启动测试。
 *
 * <p>验证 KmApplication 能在 dev profile 下成功启动 ApplicationContext。
 *
 * <p>注意：此测试不连接真实数据库。如需在 CI 环境运行 Flyway 迁移，
 * 需配合 testcontainers 或 H2 内存数据库。
 */
@SpringBootTest
@ActiveProfiles("test")
class KmApplicationTests {

    @Test
    void contextLoads() {
        // 仅验证 ApplicationContext 启动不抛异常
        // 数据源 / Redis / 外部依赖在 test profile 下应被排除或替换
    }
}
