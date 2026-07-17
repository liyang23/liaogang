package com.liaogang.famou.km.governance.controller;

import com.liaogang.famou.km.common.Result;
import com.liaogang.famou.km.governance.model.ConflictEntity;
import com.liaogang.famou.km.governance.repository.ConflictMapper;
import com.liaogang.famou.km.governance.service.ConflictArbitrator;
import com.liaogang.famou.km.governance.service.ConflictDetector;
import com.liaogang.famou.km.governance.service.LlmSuggestionService;
import com.liaogang.famou.km.llm.DeepSeekClient.LlmSuggestion;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * U7 知识治理 REST API 控制器（T302）
 *
 * <p>API：
 * <ul>
 *   <li>POST /api/governance/conflict/{id}/suggest — 调用 DeepSeek v4 获取 LLM 建议 (OQ-9, ≤5s NFR-28)
 *   <li>POST /api/governance/conflict/{id}/arbitrate — 仲裁快路径 (OQ-8 4 状态一次性完成)
 *   <li>GET /api/governance/conflict?status=pending — 治理工作台查询
 *   <li>POST /api/governance/conflict/detect — 写时检测入口 (U4 KO 提交时调用)
 *   <li>GET /api/governance/report — 治理报告导出 (CSV 格式)
 * </ul>
 */
@RestController
@RequestMapping("/api/governance")
@RequiredArgsConstructor
public class GovernanceController {

    private final ConflictMapper conflictMapper;
    private final ConflictDetector conflictDetector;
    private final LlmSuggestionService llmSuggestionService;
    private final ConflictArbitrator conflictArbitrator;

    @GetMapping("/conflict")
    public Result<List<ConflictEntity>> listConflicts(
            @RequestParam(value = "status", required = false) String status) {
        List<ConflictEntity> conflicts = status == null
                ? conflictMapper.selectList(null)
                : conflictMapper.findByStatus(status);
        return Result.ok(conflicts);
    }

    @PostMapping("/conflict/{id}/suggest")
    public Result<LlmSuggestion> getSuggestion(
            @PathVariable("id") Long conflictId,
            @RequestParam("userSub") String userSub) {
        ConflictEntity conflict = conflictMapper.selectById(conflictId);
        if (conflict == null) {
            return Result.fail(40440, "conflict not found: " + conflictId);
        }
        LlmSuggestion suggestion = llmSuggestionService.suggestForConflict(conflict, userSub);
        if (suggestion == null) {
            return Result.fail(42901, "LLM quota exhausted or call failed");
        }
        return Result.ok(suggestion);
    }

    @PostMapping("/conflict/{id}/arbitrate")
    public Result<ConflictEntity> arbitrate(
            @PathVariable("id") Long conflictId,
            @RequestParam("userSub") String userSub) {
        ConflictEntity conflict = conflictMapper.selectById(conflictId);
        if (conflict == null) {
            return Result.fail(40440, "conflict not found: " + conflictId);
        }
        return Result.ok(conflictArbitrator.arbitrateAndPublish(conflict, userSub));
    }

    @PostMapping("/conflict/detect")
    public Result<List<ConflictEntity>> detectOnWrite(
            @RequestParam("koId") String koId,
            @RequestParam("userSub") String userSub) {
        // placeholder: 写时检测入口. 实际由 U4 KO 提交时调用 (T303 / U4 §5.2.3.1)
        // 当前实现: 列出该 KO 关联的所有冲突 (placeholder; 真实检测逻辑由 U4 接入)
        List<ConflictEntity> conflicts = conflictMapper.findByKoId(koId);
        return Result.ok(conflicts);
    }

    @GetMapping("/report")
    public Result<Map<String, Object>> generateReport() {
        // placeholder: 治理报告导出 (CSV 格式) - 由 T304 集成测试验证 + T303 治理工作台集成
        return Result.ok(Map.of("status", "ok", "format", "csv", "rows", conflictMapper.selectList(null).size()));
    }
}
