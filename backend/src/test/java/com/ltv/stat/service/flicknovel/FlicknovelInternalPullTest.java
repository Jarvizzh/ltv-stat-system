package com.ltv.stat.service.flicknovel;

import com.ltv.stat.dto.flicknovel.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 番茄海外系统内部拉取服务验证测试
 */
@SpringBootTest
public class FlicknovelInternalPullTest {

    @Autowired
    private FlicknovelApiService flicknovelApiService;

    @Autowired
    private FlicknovelApiClient flicknovelApiClient;

    @Test
    public void testInternalOrderPull() {
        long now = Instant.now().getEpochSecond();
        FlicknovelOrderQueryRequest req = new FlicknovelOrderQueryRequest(now - 7 * 86400, now, 1L, 100L);

        // 系统内部服务拉取
        FlicknovelOrderResponse response = flicknovelApiService.getOrderList(req);
        assertNotNull(response);
        assertEquals(0, response.getCode(), "网关签名与请求应校验成功");
        assertNotNull(response.getData());
        System.out.println("[Internal Pull Orders] success: " + response.isSuccess() + ", count=" + response.getData().getOrders().size());
    }

    @Test
    public void testInternalPromotionPull() {
        FlicknovelPromotionQueryRequest req = new FlicknovelPromotionQueryRequest("test@sinan-partner.com", 1L, 10L);

        // 系统内部服务拉取
        FlicknovelPromotionResponse response = flicknovelApiService.getPromotionList(req);
        assertNotNull(response);
        // 说明鉴权通过，进入业务参数层（测试邮箱未绑定，提示 email not found）
        System.out.println("[Internal Pull Promotions] response code=" + response.getCode() + ", msg=" + response.getMessage());
    }

    @Test
    public void testInternalRechargeTemplatePull() {
        FlicknovelRechargeTemplateQueryRequest req = new FlicknovelRechargeTemplateQueryRequest(984582L, "test@sinan-partner.com", 1L, 10L);

        // 系统内部服务拉取
        FlicknovelRechargeTemplateResponse response = flicknovelApiService.getRechargeTemplateList(req);
        assertNotNull(response);
        System.out.println("[Internal Pull Recharge Templates] response code=" + response.getCode() + ", msg=" + response.getMessage());
    }

    @Test
    public void testQueryTemplatesRealJson() throws Exception {
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper().enable(com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT);
        String email = flicknovelApiClient.getDefaultEmail();
        Long distAppId = flicknovelApiClient.getDefaultDistAppId(); // 2000019L

        System.out.println("====== [TEST] Query Recharge Templates V2: email=" + email + ", distAppId=" + distAppId + " ======");
        com.fasterxml.jackson.databind.JsonNode respV2 = flicknovelApiClient.getRechargeTemplateV2List(
                new FlicknovelRechargeTemplateV2QueryRequest(email, distAppId, 1L, 50L));
        System.out.println("--- RAW JSON V2 START ---");
        System.out.println(mapper.writeValueAsString(respV2));
        System.out.println("--- RAW JSON V2 END ---");

        System.out.println("====== [TEST] Query Recharge Templates V1: email=" + email + ", distAppId=" + distAppId + " ======");
        com.fasterxml.jackson.databind.JsonNode respV1 = flicknovelApiClient.getRechargeTemplateV1Json(
                new FlicknovelRechargeTemplateQueryRequest(distAppId, email, 1L, 50L));
        System.out.println("--- RAW JSON V1 START ---");
        System.out.println(mapper.writeValueAsString(respV1));
        System.out.println("--- RAW JSON V1 END ---");
    }
}

