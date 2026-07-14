package com.liaogang.famou.km;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 辽港伐谋 KM 平台后端应用入口。
 *
 * <p>基于 Spring Boot 3.2 + MyBatis-Plus + MySQL 8 + Flyway + Redis + SpringDoc。
 *
 * <p>v0.32 关键决议：
 * <ul>
 *   <li>OQ-1：项目仅做数据隔离（按 project_id 过滤）</li>
 *   <li>OQ-2：删除业务专家「提案权」</li>
 *   <li>OQ-9：LLM 主动建议（DeepSeek v4）</li>
 *   <li>OQ-12：角色变更下次登录生效</li>
 *   <li>OQ-23：辽港统一认证取代 OIDC（OQ-T02 Sprint 0 启动前收齐）</li>
 * </ul>
 */
@SpringBootApplication
@MapperScan("com.liaogang.famou.km.**.repository")
@EnableAsync
@EnableScheduling
public class KmApplication {

    public static void main(String[] args) {
        SpringApplication.run(KmApplication.class, args);
    }
}
