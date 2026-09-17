package com.ltv.stat.service.flicknovel;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ltv.stat.dto.flicknovel.*;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;

public class FlicknovelIntegrationTest {

    @Test
    public void testLiveApiCall() {
        RestTemplate restTemplate = new RestTemplate();
        ObjectMapper objectMapper = new ObjectMapper();

        // 直接构造 client，不需要启动整套 Spring Context
        FlicknovelApiClient client = new FlicknovelApiClient(restTemplate, objectMapper, null);

        System.out.println("=== Testing Flicknovel OpenAPI Live Connection ===");

        // 1. 测试获取订单列表接口
        long now = Instant.now().getEpochSecond();
        long beginTs = now - 7 * 86400; // 最近7天
        FlicknovelOrderQueryRequest orderReq = new FlicknovelOrderQueryRequest(beginTs, now, 1L, 100L);
        try {
            FlicknovelOrderResponse orderResp = client.getOrderList(orderReq);
            System.out.println("[Order API Response] code=" + (orderResp != null ? orderResp.getCode() : null)
                    + ", message=" + (orderResp != null ? orderResp.getMessage() : null)
                    + ", data.orders.size=" + (orderResp != null && orderResp.getData() != null && orderResp.getData().getOrders() != null ? orderResp.getData().getOrders().size() : 0));
        } catch (Exception e) {
            System.err.println("[Order API Error]: ");
            e.printStackTrace();
        }

        // 2. 测试获取推广链列表接口 (用测试邮箱或空邮箱)
        FlicknovelPromotionQueryRequest prmtReq = new FlicknovelPromotionQueryRequest("test@sinan-partner.com", 1L, 10L);
        try {
            FlicknovelPromotionResponse prmtResp = client.getPromotionList(prmtReq);
            System.out.println("[Promotion API Response] code=" + (prmtResp != null ? prmtResp.getCode() : null)
                    + ", message=" + (prmtResp != null ? prmtResp.getMessage() : null)
                    + ", data.promotions.size=" + (prmtResp != null && prmtResp.getData() != null && prmtResp.getData().getPromotions() != null ? prmtResp.getData().getPromotions().size() : 0));
        } catch (Exception e) {
            System.err.println("[Promotion API Error]: ");
            e.printStackTrace();
        }

        // 3. 测试获取充值模板接口
        FlicknovelRechargeTemplateQueryRequest rchgReq = new FlicknovelRechargeTemplateQueryRequest(984582L, "test@sinan-partner.com", 1L, 10L);
        try {
            FlicknovelRechargeTemplateResponse rchgResp = client.getRechargeTemplateList(rchgReq);
            System.out.println("[Recharge Template Response] code=" + (rchgResp != null ? rchgResp.getCode() : null)
                    + ", message=" + (rchgResp != null ? rchgResp.getMessage() : null)
                    + ", data.templates.size=" + (rchgResp != null && rchgResp.getData() != null && rchgResp.getData().getRechargeTemplates() != null ? rchgResp.getData().getRechargeTemplates().size() : 0));
        } catch (Exception e) {
            System.err.println("[Recharge Template Error]: ");
            e.printStackTrace();
        }
    }
}
