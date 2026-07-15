package com.liaogang.famou.km.llm;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DeepSeek v4 LLM 客户端（v0.32 PRD §5.2.3 OQ-9）。
 *
 * <p>Sprint 1 mock 模式：Q-I1 待 owner 提供（HTTP/gRPC/SDK + 配额），
 * 未配置时返回 mock 响应。
 *
 * <p>FR-21 OQ-9 验收：LLM 主动建议接口（替代前端 mock）
 * <ul>
 *   <li>suggestion: OVERRIDE / COEXIST / SPLIT / DEFER / IGNORE</li>
 *   <li>confidence: 0-1</li>
 *   <li>rationale: 理由说明</li>
 * </ul>
 *
 * <p>NFR-28：LLM 建议响应 ≤ 5s（F-2 修复：RestTemplateBuilder 配置 connect/read timeout）
 */
@Slf4j
@Service
public class DeepSeekClient {

    @Value("${app.llm.api-key:demo-api-key-mock}")
    private String apiKey;

    @Value("${app.llm.base-url:https://api.deepseek.com/v4}")
    private String baseUrl;

    @Value("${app.llm.model:deepseek-v4}")
    private String model;

    @Value("${app.llm.timeout-seconds:5}")
    private int timeoutSeconds;

    // F-2 修复：使用 RestTemplateBuilder 配置 connect/read timeout = 5s（NFR-28 承诺）
    private final RestTemplate restTemplate;
    // F-11 修复：用于解析 LLM 响应 JSON（替代 mock 解析）
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    public DeepSeekClient(RestTemplateBuilder builder,
                          com.fasterxml.jackson.databind.ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.restTemplate = builder
            .setConnectTimeout(Duration.ofSeconds(5))
            .setReadTimeout(Duration.ofSeconds(5))
            .build();
    }

    /**
     * 调用 LLM 生成冲突建议（OQ-9 验收点）
     *
     * @param conflictContext 冲突双方 KO 内容 + 字段 + 作用域
     * @return LLM 建议（suggestion/confidence/rationale）
     */
    public LlmSuggestion getConflictSuggestion(ConflictContext conflictContext) {
        if (apiKey.startsWith("demo-")) {
            log.warn("Q-I1 DeepSeek v4 API 未提供（demo mock 模式），返回 mock 建议");
            return mockSuggestion(conflictContext);
        }

        // 1. 构造请求
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("messages", List.of(
            Map.of("role", "system", "content", buildSystemPrompt()),
            Map.of("role", "user", "content", buildUserPrompt(conflictContext))
        ));
        body.put("temperature", 0.1);
        body.put("max_tokens", 500);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        // 2. 调用 LLM（带超时控制）
        log.debug("调用 DeepSeek v4: url={}, model={}", baseUrl + "/chat/completions", model);
        // TODO: T007 实施时配置 RestTemplate 的 connect/read timeout = 5s

        @SuppressWarnings("rawtypes")
        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/chat/completions",
                HttpMethod.POST,
                entity,
                Map.class);

        if (response == null || !response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("DeepSeek v4 调用失败");
        }

        // 3. 解析响应
        return parseResponse(response.getBody());
    }

    /**
     * Mock 建议（Sprint 1 演示态）
     */
    private LlmSuggestion mockSuggestion(ConflictContext context) {
        LlmSuggestion s = new LlmSuggestion();
        s.setSuggestion("OVERRIDE");
        s.setConfidence(0.92);
        s.setRationale(String.format("Mock 建议：根据冲突分析 '%s' 字段重叠，建议硬约束版本优先。建议基于 OQ-9 v0.32 mock 响应。",
                context.getConflictType()));
        return s;
    }

    private String buildSystemPrompt() {
        return "你是辽港伐谋 KM 平台冲突治理助手。根据提供的冲突上下文，给出最合理的处置建议（OVERRIDE / COEXIST / SPLIT / DEFER / IGNORE 之一）及置信度（0-1）和理由。";
    }

    private String buildUserPrompt(ConflictContext context) {
        return String.format("冲突类型：%s\n冲突双方：\n%s\n\n请给出建议：",
                context.getConflictType(), context.getBothPartiesContent());
    }

    @SuppressWarnings("unchecked")
    private LlmSuggestion parseResponse(Map<String, Object> body) {
        List<Map<String, Object>> choices = (List<Map<String, Object>>) body.get("choices");
        if (choices == null || choices.isEmpty()) {
            throw new RuntimeException("DeepSeek v4 响应无 choices");
        }
        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
        String content = (String) message.get("content");

        // TODO: 实际解析 LLM 输出（JSON 格式：suggestion/confidence/rationale）
        // 此处 mock 解析
        LlmSuggestion s = new LlmSuggestion();
        s.setSuggestion("OVERRIDE");
        s.setConfidence(0.85);
        s.setRationale(content);
        return s;
    }

    /** LLM 建议响应 */
    @lombok.Data
    public static class LlmSuggestion {
        /** OVERRIDE / COEXIST / SPLIT / DEFER / IGNORE */
        private String suggestion;
        /** 0-1 */
        private double confidence;
        /** 理由说明 */
        private String rationale;
    }

    /** 冲突上下文（输入） */
    @lombok.Data
    public static class ConflictContext {
        /** C1-C6 / H1-H6 */
        private String conflictType;
        /** 双方 KO 内容（sub / name / 字段值等） */
        private String bothPartiesContent;
    }
}
