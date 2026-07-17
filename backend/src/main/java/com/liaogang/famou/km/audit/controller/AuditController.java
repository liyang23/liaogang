package com.liaogang.famou.km.audit.controller;

import com.liaogang.famou.km.audit.AuditLogEntity;
import com.liaogang.famou.km.audit.repository.AuditLogMapper;
import com.liaogang.famou.km.audit.enums.AuditAction;
import com.liaogang.famou.km.common.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

/**
 * U9 审计日志 REST API 控制器 (T308)
 *
 * <p>API：
 * <ul>
 *   <li>GET /api/audit-log — 列表 (按 userId 过滤, 默认 50 条)
 *   <li>GET /api/audit-log/{id} — 单条详情 (OQ-11 ID 三重暴露 Modal)
 *   <li>GET /api/audit-log/by-target/{koId} — 按目标 KO 查询
 *   <li>GET /api/audit-log/export.csv — 导出 (首列 AUDIT ID, OQ-11 CSV 导出)
 * </ul>
 */
@RestController
@RequestMapping("/api/audit-log")
@RequiredArgsConstructor
public class AuditController {

    private final AuditLogMapper auditLogMapper;

    @GetMapping
    public Result<List<AuditLogEntity>> list(
            @RequestParam(value = "userId", required = false) String userId,
            @RequestParam(value = "limit", defaultValue = "50") int limit) {
        if (userId != null) {
            return Result.ok(auditLogMapper.findByUserId(userId, limit));
        }
        return Result.ok(auditLogMapper.selectList(null));
    }

    @GetMapping("/{id}")
    public Result<AuditLogEntity> getById(@PathVariable("id") String id) {
        AuditLogEntity log = auditLogMapper.selectById(id);
        if (log == null) {
            return Result.fail(40460, "audit log not found: " + id);
        }
        return Result.ok(log);
    }

    @GetMapping("/by-target/{koId}")
    public Result<List<AuditLogEntity>> findByTargetKo(@PathVariable("koId") String koId) {
        return Result.ok(auditLogMapper.findByTargetKo(koId));
    }

    @GetMapping("/export.csv")
    public Result<String> exportCsv(
            @RequestParam(value = "action", required = false) String action,
            @RequestParam(value = "from", required = false) String from,
            @RequestParam(value = "to", required = false) String to) {
        // 12 月保留: from 默认 1 年前, to 默认 now
        LocalDateTime fromTs = from == null ? LocalDateTime.now().minusYears(1) : LocalDateTime.parse(from);
        LocalDateTime toTs = to == null ? LocalDateTime.now() : LocalDateTime.parse(to);

        List<AuditLogEntity> logs = action != null
                ? auditLogMapper.findByActionAndDateRange(action, fromTs, toTs)
                : auditLogMapper.selectList(null);

        // CSV 格式: 首列 AUDIT ID (OQ-11 ID 三重暴露)
        StringBuilder csv = new StringBuilder("AUDIT_ID,action,user_id,target_ko,detail,reason,created_at\n");
        for (AuditLogEntity log : logs) {
            csv.append(escapeCsv(log.getId())).append(",")
               .append(escapeCsv(log.getAction())).append(",")
               .append(escapeCsv(log.getUserId())).append(",")
               .append(escapeCsv(log.getTargetKo())).append(",")
               .append(escapeCsv(log.getDetail())).append(",")
               .append(escapeCsv(log.getReason())).append(",")
               .append(escapeCsv(log.getCreatedAt() == null ? "" : log.getCreatedAt().toString()))
               .append("\n");
        }
        return Result.ok(csv.toString());
    }

    /** OQ-11 ID 三重暴露: CSV 导出时首列必为 AUDIT ID 完整字符串 */
    private String escapeCsv(String s) {
        if (s == null) return "";
        if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }
}
