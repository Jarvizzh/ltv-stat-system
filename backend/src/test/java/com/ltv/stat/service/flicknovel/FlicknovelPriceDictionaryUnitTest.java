package com.ltv.stat.service.flicknovel;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class FlicknovelPriceDictionaryUnitTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void testParsePriceTypeMapFromJsonString() throws Exception {
        FlicknovelOrderTypeResolver resolver = new FlicknovelOrderTypeResolver();
        FlicknovelApiService service = new FlicknovelApiService(null, null, null, null, null, null, resolver, objectMapper);

        // 构造一个典型的包含日卡、周订、年卡和代币的充值模板 v2 JSON
        String json = "{\n" +
                "  \"recharge_template_id\": \"tpl_001\",\n" +
                "  \"name\": \"测试复合模板\",\n" +
                "  \"detail\": {\n" +
                "    \"h5\": {\n" +
                "      \"first_products\": [\n" +
                "        {\"product\": {\"benefit_type\": 1, \"price_cents\": 1199}},\n" +
                "        {\"product\": {\"benefit_type\": 1, \"price_cents\": 3999}},\n" +
                "        {\"product\": {\"benefit_type\": 2, \"price_cents\": 3999, \"discount_price_cents\": 1999}}\n" +
                "      ],\n" +
                "      \"first_top_products\": [\n" +
                "        {\"product\": {\"benefit_type\": 2, \"price_cents\": 999}},\n" +
                "        {\"product\": {\"benefit_type\": 2, \"price_cents\": 19899}}\n" +
                "      ]\n" +
                "    }\n" +
                "  }\n" +
                "}";

        JsonNode tplNode = objectMapper.readTree(json);
        FlicknovelApiService.TemplatePriceDetail detail = service.parsePriceTypeDetail(tplNode);

        assertNotNull(detail);
        Map<Integer, Integer> priceMap = detail.getPriceMap();

        // 验证各档位解析
        assertEquals(1, priceMap.get(999), "日卡 999 美分应为订阅 (1)");
        assertEquals(1, priceMap.get(1999), "首购特惠 1999 美分应为订阅 (1)");
        assertEquals(1, priceMap.get(19899), "年卡 19899 美分应为订阅 (1)");
        assertEquals(0, priceMap.get(1199), "代币 1199 美分应为单充 (0)");

        // 验证冲突价格识别与特惠标志
        assertTrue(detail.getAmbiguousPrices().contains(3999), "3999 既是代币又是周订原价，必须识别为冲突金额");
        assertTrue(detail.isHasIntroOffer(), "存在 1999 首购特惠，hasIntroOffer 必须为 true");

        System.out.println("=== testParsePriceTypeMapFromJsonString PASSED ===");
    }
}
