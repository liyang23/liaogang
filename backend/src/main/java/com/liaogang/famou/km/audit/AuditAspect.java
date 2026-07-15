package com.liaogang.famou.km.audit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * AuditLog AOP 拦截器（v0.32 PRD §5.2.6）。
 *
 * <p>拦截 @AuditLog 注解的方法，自动写审计日志（含操作人 sub / 操作类型 / 详情）。
 * <p>OQ-5 修订：3 秒撤销仅前端 UI（OQ-5 后端不主动失效 Redis 中的旧 JWT，不写 USER_ROLE_REVERT 审计）。
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AuditAspect {

    private final AuditLogService auditLogService;
    private final ExpressionParser parser = new SpelExpressionParser();

    @Around("@annotation(com.liaogang.famou.km.audit.AuditLog)")
    public Object audit(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        AuditLog auditAnnotation = method.getAnnotation(AuditLog.class);

        // 1. 解析 SpEL 表达式（detail 可含方法参数引用）
        String detail = auditAnnotation.remark();
        if (detail != null && !detail.isEmpty() && detail.contains("#")) {
            try {
                EvaluationContext context = new StandardEvaluationContext();
                Object[] args = joinPoint.getArgs();
                for (int i = 0; i < args.length; i++) {
                    context.setVariable("p" + i, args[i]);
                    context.setVariable("a" + i, args[i]);
                }
                Expression expression = parser.parseExpression(detail);
                detail = String.valueOf(expression.getValue(context));
            } catch (Exception e) {
                log.debug("SpEL 表达式解析失败: {}", e.getMessage());
            }
        }

        // 2. 获取当前用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userSub = authentication != null ? String.valueOf(authentication.getPrincipal()) : "anonymous";

        // 3. 收集方法参数（简化版：toString 全部参数）
        Object[] args = joinPoint.getArgs();
        String argsStr = args != null && args.length > 0
            ? java.util.Arrays.stream(args).map(Object::toString).collect(java.util.stream.Collectors.joining(", "))
            : "";
        Map<String, Object> detailMap = new HashMap<>();
        detailMap.put("remark", detail);
        detailMap.put("args", argsStr);

        // 4. 执行方法 + 失败/成功都写审计（F-12 修复）
        Object result;
        boolean succeeded = false;
        try {
            result = joinPoint.proceed();
            succeeded = true;
            return result;
        } finally {
            // 5. 写审计日志（无论成功/失败 + 异步不阻塞业务主流程）
            String finalAction = succeeded ? auditAnnotation.action() : auditAnnotation.action() + "_FAILED";
            Map<String, Object> finalDetail = new HashMap<>(detailMap);
            finalDetail.put("result", succeeded ? "SUCCESS" : "FAILURE");
            AuditLogEntity auditLog = AuditLogEntity.builder()
                .action(finalAction)
                .userId(userSub)
                .detail(finalDetail.toString())
                .reason(detail)
                .build();
            auditLogService.recordAsync(auditLog);
        }
    }
}
