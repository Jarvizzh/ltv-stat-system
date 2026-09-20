package com.ltv.stat.service.flicknovel;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ltv.stat.dto.flicknovel.*;
import com.ltv.stat.entity.PlatformConfig;
import com.ltv.stat.repository.PlatformConfigRepository;
import com.ltv.stat.util.FlicknovelSignUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;

/**
 * 番茄司南 OpenAPI 客户端组件
 * 封装 Ed25519 签名、通信、重试与各业务接口
 */
@Component
public class FlicknovelApiClient {

    private static final Logger log = LoggerFactory.getLogger(FlicknovelApiClient.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final PlatformConfigRepository platformConfigRepository;

    @Value("${flicknovel.api.base-url:https://openapi.sinan-partner.com}")
    private String configuredBaseUrl;

    @Value("${flicknovel.api.company-id:355549587538358272}")
    private String configuredCompanyId;

    @Value("${flicknovel.api.private-key:ymcPnTqpiQOAtROHJoeegoovJxS7wv6t0HLDUv5q3/G4qry6yKcvjYwhrBqwuEIMjfXMIIqDe0YUPu9JaPofMQ==}")
    private String configuredPrivateKey;

    @Value("${flicknovel.api.default-email:charles_z0@163.com}")
    private String defaultEmail;

    @Value("${flicknovel.api.default-dist-app-id:2000019}")
    private Long defaultDistAppId;

    public FlicknovelApiClient(RestTemplate restTemplate,
                              ObjectMapper objectMapper,
                              PlatformConfigRepository platformConfigRepository) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.platformConfigRepository = platformConfigRepository;
    }

    public String getBaseUrl() {
        return (configuredBaseUrl != null && !configuredBaseUrl.trim().isEmpty())
                ? configuredBaseUrl.trim()
                : "https://openapi.sinan-partner.com";
    }

    public String getActiveCompanyId() {
        if (platformConfigRepository != null) {
            try {
                PlatformConfig config = platformConfigRepository.findByPlatformCode("flicknovel").orElse(null);
                if (config != null && config.getAuthCredentials() != null && !config.getAuthCredentials().trim().isEmpty()) {
                    Map<?, ?> map = objectMapper.readValue(config.getAuthCredentials(), Map.class);
                    if (map.containsKey("companyId") && map.get("companyId") != null) {
                        return String.valueOf(map.get("companyId")).trim();
                    }
                }
            } catch (Exception ignored) {}
        }
        return (configuredCompanyId != null && !configuredCompanyId.trim().isEmpty())
                ? configuredCompanyId.trim()
                : "355549587538358272";
    }

    public String getActivePrivateKey() {
        if (platformConfigRepository != null) {
            try {
                PlatformConfig config = platformConfigRepository.findByPlatformCode("flicknovel").orElse(null);
                if (config != null && config.getAuthCredentials() != null && !config.getAuthCredentials().trim().isEmpty()) {
                    Map<?, ?> map = objectMapper.readValue(config.getAuthCredentials(), Map.class);
                    if (map.containsKey("privateKey") && map.get("privateKey") != null) {
                        return String.valueOf(map.get("privateKey")).trim();
                    }
                }
            } catch (Exception ignored) {}
        }
        return (configuredPrivateKey != null && !configuredPrivateKey.trim().isEmpty())
                ? configuredPrivateKey.trim()
                : "ymcPnTqpiQOAtROHJoeegoovJxS7wv6t0HLDUv5q3/G4qry6yKcvjYwhrBqwuEIMjfXMIIqDe0YUPu9JaPofMQ==";
    }

    public String getDefaultEmail() {
        return (defaultEmail != null && !defaultEmail.trim().isEmpty())
                ? defaultEmail.trim()
                : "charles_z0@163.com";
    }

    public Long getDefaultDistAppId() {
        return defaultDistAppId != null ? defaultDistAppId : 2000019L;
    }

    /**
     * 更新凭据配置
     */
    public void updateCredentials(String companyId, String privateKey, String defaultEmail) {
        PlatformConfig config = platformConfigRepository.findByPlatformCode("flicknovel").orElseGet(() -> {
            PlatformConfig p = new PlatformConfig();
            p.setPlatformCode("flicknovel");
            p.setPlatformName("番茄司南");
            p.setAuthType("ED25519_KEY");
            p.setStatus(1);
            return p;
        });

        Map<String, String> map = new HashMap<>();
        map.put("companyId", companyId != null ? companyId.trim() : getActiveCompanyId());
        map.put("privateKey", privateKey != null ? privateKey.trim() : getActivePrivateKey());
        if (defaultEmail != null) {
            map.put("defaultEmail", defaultEmail.trim());
        }

        try {
            config.setAuthCredentials(objectMapper.writeValueAsString(map));
            platformConfigRepository.save(config);
            log.info("Successfully updated Flicknovel credentials in database.");
        } catch (Exception e) {
            throw new RuntimeException("Failed to save Flicknovel credentials: " + e.getMessage(), e);
        }
    }

    /**
     * 1. 获取订单记录列表
     * Path: /get_order_list/v1
     */
    public FlicknovelOrderResponse getOrderList(FlicknovelOrderQueryRequest request) {
        String path = "/get_order_list/v1";
        return executePost(path, request, FlicknovelOrderResponse.class);
    }

    /**
     * 获取染色归因记录列表 (用于辅助推导用户精准注册时间)
     * Path: /get_relation_list/v1
     */
    public FlicknovelRelationResponse getRelationList(FlicknovelOrderQueryRequest request) {
        String path = "/get_relation_list/v1";
        return executePost(path, request, FlicknovelRelationResponse.class);
    }

    /**
     * 2. 获取推广链列表
     * Path: /open/promotion/query/v1
     */
    public FlicknovelPromotionResponse getPromotionList(FlicknovelPromotionQueryRequest request) {
        String path = "/open/promotion/query/v1";
        return executePost(path, request, FlicknovelPromotionResponse.class);
    }

    /**
     * 3. 获取充值模版列表
     * Path: /open/recharge_template/query/v1
     */
    public FlicknovelRechargeTemplateResponse getRechargeTemplateList(FlicknovelRechargeTemplateQueryRequest request) {
        String path = "/open/recharge_template/query/v1";
        return executePost(path, request, FlicknovelRechargeTemplateResponse.class);
    }

    /**
     * 获取短篇充值模版列表 (v1 返回 JsonNode 结构，兼容历史模板)
     * Path: /open/recharge_template/query/v1
     */
    public com.fasterxml.jackson.databind.JsonNode getRechargeTemplateV1Json(FlicknovelRechargeTemplateQueryRequest request) {
        String path = "/open/recharge_template/query/v1";
        return executePost(path, request, com.fasterxml.jackson.databind.JsonNode.class);
    }

    /**
     * 3.2 获取短篇充值模版列表 (v2)
     * Path: /open/recharge_template/query/v2
     */
    public com.fasterxml.jackson.databind.JsonNode getRechargeTemplateV2List(FlicknovelRechargeTemplateV2QueryRequest request) {
        String path = "/open/recharge_template/query/v2";
        return executePost(path, request, com.fasterxml.jackson.databind.JsonNode.class);
    }

    /**
     * 执行带 Ed25519 鉴权的 POST 请求
     */
    public <T> T executePost(String path, Object requestBody, Class<T> responseClass) {
        String companyId = getActiveCompanyId();
        String privateKey = getActivePrivateKey();
        String baseUrl = getBaseUrl();

        if (companyId == null || companyId.trim().isEmpty()) {
            throw new IllegalStateException("番茄司南 Company ID 未配置！");
        }
        if (privateKey == null || privateKey.trim().isEmpty()) {
            throw new IllegalStateException("番茄司南 API 密钥 (Private Key) 未配置！");
        }

        try {
            // 序列化请求体 JSON (与 Go json.Marshal / Python separators=(',', ':') 保持紧凑一致)
            String bodyJson = requestBody != null ? objectMapper.writeValueAsString(requestBody) : "{}";

            long nowSeconds = Instant.now().getEpochSecond();
            String nonce = UUID.randomUUID().toString().replace("-", "");
            if (nonce.length() > 32) {
                nonce = nonce.substring(0, 32);
            }

            Map<String, String> queryParams = new HashMap<>();
            queryParams.put("company_id", companyId);
            queryParams.put("timestamp", String.valueOf(nowSeconds));
            queryParams.put("nonce", nonce);

            // 计算 Ed25519 签名
            String sign = FlicknovelSignUtil.generateSign(queryParams, bodyJson, privateKey);
            queryParams.put("sign", sign);

            // 拼接完整 URL
            StringBuilder urlBuilder = new StringBuilder(baseUrl);
            if (!path.startsWith("/")) {
                urlBuilder.append("/");
            }
            urlBuilder.append(path).append("?");

            boolean first = true;
            for (Map.Entry<String, String> entry : queryParams.entrySet()) {
                if (!first) {
                    urlBuilder.append("&");
                }
                urlBuilder.append(URLEncoder.encode(entry.getKey(), "UTF-8"))
                        .append("=")
                        .append(URLEncoder.encode(entry.getValue(), "UTF-8"));
                first = false;
            }

            String fullUrl = urlBuilder.toString();
            java.net.URI uri = java.net.URI.create(fullUrl);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

            HttpEntity<String> entity = new HttpEntity<>(bodyJson, headers);

            long start = System.currentTimeMillis();
            ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.POST, entity, String.class);
            long cost = System.currentTimeMillis() - start;

            log.info("[FlicknovelApiClient] POST {} cost: {}ms, status: {}", path, cost, response.getStatusCode());

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new RuntimeException("番茄司南接口响应异常: HTTP " + response.getStatusCodeValue() + ", body: " + response.getBody());
            }

            return objectMapper.readValue(response.getBody(), responseClass);

        } catch (HttpStatusCodeException hse) {
            String respBody = hse.getResponseBodyAsString();
            log.error("[FlicknovelApiClient] HTTP Error {} on {}: {}", hse.getRawStatusCode(), path, respBody);
            throw new RuntimeException("番茄司南接口请求错误 (HTTP " + hse.getRawStatusCode() + "): " + respBody, hse);
        } catch (Exception e) {
            log.error("[FlicknovelApiClient] Failed to execute POST on {}: {}", path, e.getMessage(), e);
            throw new RuntimeException("调用番茄司南接口异常: " + e.getMessage(), e);
        }
    }
}
