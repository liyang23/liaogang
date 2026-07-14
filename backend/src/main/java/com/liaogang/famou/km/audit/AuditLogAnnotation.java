package com.liaogang.famou.km.audit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 审计日志注解（v0.32 PRD §5.2.6 OQ-5 简化）。
 *
 * <p>标注了 @AuditLog 的方法执行时会自动写审计日志（含操作人 sub / 操作类型 / 详情）。
 * <p>OQ-5 修订：3 秒撤销仅前端 UI（OQ-5 后端不主动失效 Redis 中的旧 JWT，不写 USER_ROLE_REVERT 审计）。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuditLog {

    /** 操作类型（必填） */
    String action();

    /** 备注（可选） */
    String remark() default "";
}
