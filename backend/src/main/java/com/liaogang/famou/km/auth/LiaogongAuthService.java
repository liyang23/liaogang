package com.liaogang.famou.km.auth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * 辽港统一认证 Service（v0.32 OQ-23：取代原 OIDC）。
 *
 * <p>负责调用招商云 PAAS getUserInfoByCode 端点，获取 sub / preferred_username / name / orgCode 等用户信息。
 *
 * <p>Q-I2 待 owner 提供：APIKEY + 招商云 PAAS 订阅地址（需 IT/安全 + 慧应用项目组）。
 * <p>未提供时启用 mock 模式：返回固定 mock 用户信息。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LiaogongAuthService {

    private final JwtAuthFilter jwtAuthFilter;

    @Value("${app.liaogong-auth.api-key:}")
    private String apiKey;

    @Value("${app.liaogong-auth.paas-base-url:https://appstore.cmft.com/paas-market}")
    private String paasBaseUrl;

    @Value("${app.liaogong-auth.endpoint:/api/v1/system/getUserInfoByCode}")
    private String endpoint;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 慧应用 code + codeVerifier 换取本地 JWT
     */
    public LiaogongAuthController.TokenResponse exchangeToken(
            LiaogongAuthController.LiaogongTokenRequest request, boolean mockMode) {
        if (mockMode || apiKey == null || apiKey.isEmpty()) {
            log.warn("Q-I2 辽港慧应用 APIKEY + 招商云 PAAS 订阅地址未提供，返回 mock 响应");
            return mockUserInfoResponse();
        }

        // 1. 调用招商云 PAAS getUserInfoByCode
        LiaogongUserInfo userInfo = callGetUserInfoByCode(request.getCode(), request.getCodeVerifier());

        // 2. 基于 sub 创建/查找本地 user + user_role（由 T006 UserService 实现）
        // 此处仅占位，T006 实施时接入 user 表
        // userService.findOrCreateBySub(userInfo.getSub(), userInfo.getPreferredUsername(), userInfo.getOrganizationCode());

        // 3. 查询 5 预置角色之一（OQ-23 决议：角色从本地 user_role 表获取，不在 sub claim 中）
        String role = "ROLE-0001"; // mock 默认系统管理员，T006 实施时改为 userService.getPrimaryRole(sub)

        // 4. 签发本地 JWT
        String token = jwtAuthFilter.issueToken(
                userInfo.getSub(), userInfo.getPreferredUsername(), role);

        // 5. 构造响应
        LiaogongAuthController.TokenResponse response = new LiaogongAuthController.TokenResponse();
        response.setToken(token);
        response.setExpiresIn(3600);
        response.setRoles(java.util.List.of(role));

        LiaogongAuthController.TokenResponse.UserInfo user = new LiaogongAuthController.TokenResponse.UserInfo();
        user.setSub(userInfo.getSub());
        user.setPreferredUsername(userInfo.getPreferredUsername());
        user.setDisplayName(userInfo.getName());
        user.setOrganizationCode(userInfo.getOrganizationCode());
        response.setUser(user);

        return response;
    }

    /**
     * Sprint 1 mock 模式：以指定角色直接签发 JWT
     */
    public LiaogongAuthController.TokenResponse mockIssueToken(String role) {
        if (!role.startsWith("ROLE-")) {
            role = "ROLE-0001"; // fallback 系统管理员
        }
        String mockSub = "mock-sub-" + role.toLowerCase();
        String mockUsername = "mock_" + role.toLowerCase();

        String token = jwtAuthFilter.issueToken(mockSub, mockUsername, role);

        LiaogongAuthController.TokenResponse response = new LiaogongAuthController.TokenResponse();
        response.setToken(token);
        response.setExpiresIn(3600);
        response.setRoles(java.util.List.of(role));

        LiaogongAuthController.TokenResponse.UserInfo user = new LiaogongAuthController.TokenResponse.UserInfo();
        user.setSub(mockSub);
        user.setPreferredUsername(mockUsername);
        user.setDisplayName("Mock " + role);
        user.setOrganizationCode("MOCK-ORG");
        response.setUser(user);

        return response;
    }

    /**
     * Mock 用户信息响应
     */
    private LiaogongAuthController.TokenResponse mockUserInfoResponse() {
        return mockIssueToken("ROLE-0001");
    }

    /**
     * 调用招商云 PAAS getUserInfoByCode
     *
     * <p>接口：POST {paasBaseUrl}{endpoint}
     * <p>鉴权：APIKEY
     * <p>入参：code + code_verifier（注意下划线）
     */
    private LiaogongUserInfo callGetUserInfoByCode(String code, String codeVerifier) {
        String url = paasBaseUrl + endpoint;

        // 设置请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", "APIKEY " + apiKey);

        // 设置 form 参数（注意：招商云 PAAS 参数名带下划线 code_verifier）
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", code);
        params.add("code_verifier", codeVerifier);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(params, headers);

        log.debug("调用招商云 PAAS getUserInfoByCode: url={}", url);

        @SuppressWarnings("rawtypes")
        ResponseEntity<Map> responseEntity = restTemplate.exchange(
                url, HttpMethod.POST, entity, Map.class);

        if (responseEntity == null || !responseEntity.getStatusCode().is2xxSuccessful()) {
            log.error("调用招商云 PAAS getUserInfoByCode 失败: status={}",
                    responseEntity != null ? responseEntity.getStatusCode() : "null");
            throw new RuntimeException("getUserInfoByCode 调用失败");
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) responseEntity.getBody().get("data");
        if (data == null) {
            throw new RuntimeException("未获取到用户信息");
        }

        LiaogongUserInfo userInfo = new LiaogongUserInfo();
        userInfo.setSub((String) data.get("sub"));
        userInfo.setPreferredUsername((String) data.get("preferred_username"));
        userInfo.setName((String) data.get("name"));
        userInfo.setOrganizationCode((String) data.get("orgCode"));
        return userInfo;
    }

    /**
     * 招商云 PAAS getUserInfoByCode 返回的 data 子对象
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LiaogongUserInfo {
        private String sub; // 用户唯一标识（UUID）
        @JsonProperty("preferred_username")
        private String preferredUsername; // 工号（业务主键）
        private String name; // 用户姓名
        @JsonProperty("orgCode")
        private String organizationCode; // 组织机构编码
        @JsonProperty("phoneNumber")
        private String phoneNumber; // 手机号
        private String email;
        @JsonProperty("authTypeFa2")
        private String authTypeFa2; // 双因素认证方式
    }
}
