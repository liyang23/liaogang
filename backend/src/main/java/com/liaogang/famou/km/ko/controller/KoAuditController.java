package com.liaogang.famou.km.ko.controller;

import com.liaogang.famou.km.common.Result;
import com.liaogang.famou.km.ko.model.KoEntity;
import com.liaogang.famou.km.ko.service.KoAuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * KO 审核流 REST API 控制器（T204）。
 *
 * <p>端点：
 * <ul>
 *   <li>POST /api/ko/{id}/submit - 提交审核（业务专家 / 算法工程师）</li>
 *   <li>POST /api/ko/{id}/approve - 审核通过（合规审核员，禁止自审）</li>
 *   <li>POST /api/ko/{id}/reject - 审核驳回（合规审核员，附原因）</li>
 *   <li>POST /api/ko/{id}/publish - 发布（系统管理员，Approved → Published → Active）</li>
 *   <li>GET /api/ko/{id}/status - 查询状态</li>
 * </ul>
 *
 * <p>角色校验（OQ-12 + v0.32 §4.1.2 权限矩阵）：由 U5 实施时在 T205+ 加 @PreAuthorize
 * <p>当前仅按状态机守卫（不依赖 Spring Security 角色注解）
 */
@RestController
@RequestMapping("/api/ko")
@RequiredArgsConstructor
public class KoAuditController {

    private final KoAuditService koAuditService;

    /**
     * 提交审核
     * POST /api/ko/{id}/submit
     * Headers: X-User-Sub（提交人 sub，用于未来审计日志）
     */
    @PostMapping("/{id}/submit")
    public Result<KoEntity> submit(
            @PathVariable String id,
            @RequestHeader(value = "X-User-Sub", required = false) String userSub) {
        return Result.ok(koAuditService.submitForReview(id, userSub));
    }

    /**
     * 审核通过
     * POST /api/ko/{id}/approve
     * Headers: X-User-Sub（审核人 sub，禁止自审）
     */
    @PostMapping("/{id}/approve")
    public Result<KoEntity> approve(
            @PathVariable String id,
            @RequestHeader(value = "X-User-Sub", required = false) String koCreatorSub,
            @RequestHeader(value = "X-Current-User-Sub", required = false) String currentUserSub) {
        return Result.ok(koAuditService.approve(id, koCreatorSub, currentUserSub));
    }

    /**
     * 审核驳回
     * POST /api/ko/{id}/reject
     * Body: { "reason": "..." }
     * Headers: X-Current-User-Sub
     */
    @PostMapping("/{id}/reject")
    public Result<KoEntity> reject(
            @PathVariable String id,
            @RequestBody Map<String, String> body,
            @RequestHeader(value = "X-Current-User-Sub", required = false) String currentUserSub) {
        String reason = body.getOrDefault("reason", "");
        return Result.ok(koAuditService.reject(id, reason, currentUserSub));
    }

    /**
     * 发布（系统管理员）
     * POST /api/ko/{id}/publish
     * Headers: X-Current-User-Sub
     */
    @PostMapping("/{id}/publish")
    public Result<KoEntity> publish(
            @PathVariable String id,
            @RequestHeader(value = "X-Current-User-Sub", required = false) String currentUserSub) {
        return Result.ok(koAuditService.publish(id, currentUserSub));
    }

    /**
     * 查询状态
     * GET /api/ko/{id}/status
     */
    @GetMapping("/{id}/status")
    public Result<Map<String, Object>> status(@PathVariable String id) {
        String status = koAuditService.getStatus(id);
        int inFlight = koAuditService.countInFlightVersions(id);
        return Result.ok(Map.of(
            "status", status,
            "inFlightVersions", inFlight
        ));
    }
}