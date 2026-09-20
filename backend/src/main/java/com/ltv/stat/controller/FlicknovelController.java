package com.ltv.stat.controller;

import com.ltv.stat.dto.ApiResponseDto;
import com.ltv.stat.dto.TokenInfo;
import com.ltv.stat.service.flicknovel.FlicknovelApiClient;
import com.ltv.stat.service.flicknovel.FlicknovelApiService;
import com.ltv.stat.util.UserContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * 番茄司南系统管理控制器
 * 注：获取订单列表、获取推广链接、获取充值模版 3 个接口仅作为系统底层拉取服务使用，不对外暴露原始数据接口。
 * 此控制器仅保留系统级的数据同步与凭据管理能力。
 */
@RestController
@RequestMapping("/api/flicknovel")
public class FlicknovelController {

    private final FlicknovelApiService flicknovelApiService;
    private final FlicknovelApiClient flicknovelApiClient;

    public FlicknovelController(FlicknovelApiService flicknovelApiService,
                                FlicknovelApiClient flicknovelApiClient) {
        this.flicknovelApiService = flicknovelApiService;
        this.flicknovelApiClient = flicknovelApiClient;
    }

    /**
     * 手动触发番茄司南订单同步到系统 raw_order
     */
    @PostMapping("/sync/orders")
    public ResponseEntity<?> syncOrders(@RequestBody(required = false) Map<String, String> body) {
        LocalDate startDate = null;
        LocalDate endDate = null;
        if (body != null) {
            String startStr = body.get("startDate");
            String endStr = body.get("endDate");
            if (startStr != null && !startStr.trim().isEmpty()) {
                startDate = LocalDate.parse(startStr.trim());
            }
            if (endStr != null && !endStr.trim().isEmpty()) {
                endDate = LocalDate.parse(endStr.trim());
            }
        }

        try {
            int count = flicknovelApiService.syncOrders(startDate, endDate, null);
            Map<String, Object> result = new HashMap<>();
            result.put("syncedCount", count);
            result.put("startDate", startDate);
            result.put("endDate", endDate);
            return ResponseEntity.ok(ApiResponseDto.success("番茄司南订单同步完成，共抓取/入库 " + count + " 条订单", result));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponseDto.error(500, "订单同步失败: " + e.getMessage()));
        }
    }

    /**
     * 手动触发番茄司南染色归因记录同步到系统 flicknovel_relation
     */
    @PostMapping("/sync/relations")
    public ResponseEntity<?> syncRelations(@RequestBody(required = false) Map<String, String> body) {
        LocalDate startDate = null;
        LocalDate endDate = null;
        if (body != null) {
            String startStr = body.get("startDate");
            String endStr = body.get("endDate");
            if (startStr != null && !startStr.trim().isEmpty()) {
                startDate = LocalDate.parse(startStr.trim());
            }
            if (endStr != null && !endStr.trim().isEmpty()) {
                endDate = LocalDate.parse(endStr.trim());
            }
        }

        try {
            int count = flicknovelApiService.syncRelations(startDate, endDate);
            Map<String, Object> result = new HashMap<>();
            result.put("syncedCount", count);
            result.put("startDate", startDate != null ? startDate : LocalDate.now().minusDays(2));
            result.put("endDate", endDate != null ? endDate : LocalDate.now());
            return ResponseEntity.ok(ApiResponseDto.success("番茄司南染色归因记录同步完成，共抓取/落库 " + count + " 条染色数据", result));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponseDto.error(500, "染色归因记录同步失败: " + e.getMessage()));
        }
    }

    /**
     * 手动触发番茄司南推广链/配置同步
     */
    @PostMapping("/sync/configs")
    public ResponseEntity<?> syncConfigs(@RequestBody(required = false) Map<String, Object> body) {
        String email = null;
        Long distAppId = null;
        if (body != null) {
            if (body.get("email") != null) {
                email = body.get("email").toString();
            }
            if (body.get("distAppId") != null) {
                distAppId = Long.valueOf(body.get("distAppId").toString());
            }
        }

        try {
            int count = flicknovelApiService.syncPromotionsAndConfigs(email, distAppId);
            Map<String, Object> result = new HashMap<>();
            result.put("syncedConfigs", count);
            return ResponseEntity.ok(ApiResponseDto.success("番茄司南推广链接与配置同步完成，共保存 " + count + " 条记录", result));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponseDto.error(500, "推广链同步失败: " + e.getMessage()));
        }
    }

    /**
     * 手动触发番茄司南推广链接与充值模板 v2 全量拉取入库并刷新内存字典
     */
    @PostMapping("/sync/promotions-and-templates")
    public ResponseEntity<?> syncPromotionsAndTemplates() {
        try {
            flicknovelApiService.syncPromotionsAndTemplates(true);
            Map<String, Object> result = new HashMap<>();
            result.put("promotionsInCache", flicknovelApiService.getPromotionPriceTypeCache().size());
            result.put("templatesInCache", flicknovelApiService.getTemplatePriceTypeCache().size());
            return ResponseEntity.ok(ApiResponseDto.success("推广链接与充值模板(v2)同步入库完成，内存字典已刷新", result));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponseDto.error(500, "同步推广与模板失败: " + e.getMessage()));
        }
    }

    /**
     * 查看推广与充值模板内存字典状态
     */
    @GetMapping("/cache/stats")
    public ResponseEntity<?> getCacheStats() {
        Map<String, Object> result = new HashMap<>();
        result.put("promotionsInCache", flicknovelApiService.getPromotionPriceTypeCache().size());
        result.put("templatesInCache", flicknovelApiService.getTemplatePriceTypeCache().size());
        result.put("promotionsDetail", flicknovelApiService.getPromotionPriceTypeCache());
        return ResponseEntity.ok(ApiResponseDto.success(result));
    }

    /**
     * 查询当前番茄配置状态（脱敏展示）
     */
    @GetMapping("/config")
    public ResponseEntity<?> getConfig() {
        String companyId = flicknovelApiClient.getActiveCompanyId();
        String privateKey = flicknovelApiClient.getActivePrivateKey();
        String maskedKey = (privateKey != null && privateKey.length() > 10)
                ? privateKey.substring(0, 6) + "******" + privateKey.substring(privateKey.length() - 4)
                : "未配置";

        Map<String, Object> data = new HashMap<>();
        data.put("baseUrl", flicknovelApiClient.getBaseUrl());
        data.put("companyId", companyId);
        data.put("privateKeyMasked", maskedKey);
        data.put("defaultEmail", flicknovelApiClient.getDefaultEmail());
        data.put("configured", privateKey != null && !privateKey.trim().isEmpty());

        return ResponseEntity.ok(ApiResponseDto.success(data));
    }

    /**
     * 更新番茄配置（限超级管理员）
     */
    @PostMapping("/config/update")
    public ResponseEntity<?> updateConfig(@RequestBody Map<String, String> body) {
        TokenInfo currentUser = UserContext.getCurrentUser();
        if (currentUser != null && !currentUser.isSuperAdmin()) {
            return ResponseEntity.status(403).body(ApiResponseDto.error(403, "仅超级管理员可修改番茄司南配置"));
        }

        String companyId = body != null ? body.get("companyId") : null;
        String privateKey = body != null ? body.get("privateKey") : null;
        String email = body != null ? body.get("defaultEmail") : null;

        flicknovelApiClient.updateCredentials(companyId, privateKey, email);
        return ResponseEntity.ok(ApiResponseDto.success("番茄司南配置已成功更新！", null));
    }
}
