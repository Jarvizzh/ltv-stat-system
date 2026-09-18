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

    @Test
    public void testParseIndependentFirstAndNoFirstDictionaries() throws Exception {
        FlicknovelOrderTypeResolver resolver = new FlicknovelOrderTypeResolver();
        FlicknovelApiService service = new FlicknovelApiService(null, null, null, null, null, null, resolver, objectMapper);

        // 构造一个在首充池和非首充池有不同产品设定的模板：
        // 首充池:
        //   - 代币 999
        //   - 订阅特惠 1999，订阅原价 2999
        // 非首充池:
        //   - 代币 2999 (注意：2999 在首充池是订阅原价，在非首充池是纯代币！)
        //   - 订阅续费 3999
        String json = "{\n" +
                "  \"recharge_template_id\": \"tpl_dual_pool\",\n" +
                "  \"name\": \"双池独立测试模板\",\n" +
                "  \"detail\": {\n" +
                "    \"h5\": {\n" +
                "      \"first_products\": [\n" +
                "        {\"product\": {\"benefit_type\": 1, \"price_cents\": 999}},\n" +
                "        {\"product\": {\"benefit_type\": 2, \"price_cents\": 2999, \"discount_price_cents\": 1999}}\n" +
                "      ],\n" +
                "      \"nofirst_products\": [\n" +
                "        {\"product\": {\"benefit_type\": 1, \"price_cents\": 2999}},\n" +
                "        {\"product\": {\"benefit_type\": 2, \"price_cents\": 3999}}\n" +
                "      ]\n" +
                "    }\n" +
                "  }\n" +
                "}";

        JsonNode tplNode = objectMapper.readTree(json);
        FlicknovelApiService.TemplatePriceDetail detail = service.parsePriceTypeDetail(tplNode);

        assertNotNull(detail);
        Map<Integer, Integer> firstMap = detail.getFirstPriceMap();
        Map<Integer, Integer> noFirstMap = detail.getNoFirstPriceMap();

        // 验证首充池
        assertEquals(0, firstMap.get(999), "首充池 999 应为代币 (0)");
        assertEquals(1, firstMap.get(1999), "首充池 1999 应为订阅 (1)");
        assertEquals(1, firstMap.get(2999), "首充池 2999 应为订阅 (1)");
        assertNull(firstMap.get(3999), "首充池未配置 3999");

        // 验证非首充池
        assertNull(noFirstMap.get(999), "非首充池未配置 999");
        assertNull(noFirstMap.get(1999), "非首充池未配置 1999");
        assertEquals(0, noFirstMap.get(2999), "非首充池 2999 应为代币 (0)");
        assertEquals(1, noFirstMap.get(3999), "非首充池 3999 应为订阅 (1)");

        // 验证全局合并池
        Map<Integer, Integer> allMap = detail.getPriceMap();
        assertEquals(0, allMap.get(999));
        assertEquals(1, allMap.get(1999));
        assertEquals(1, allMap.get(3999));

        // 2999 在首充是订阅(1)，在非首充是代币(0)，在全局合并池中应被标记为冲突金额
        assertTrue(detail.getAmbiguousPrices().contains(2999), "2999 在全量池中跨池冲突");
        // 但在首充池内部和非首充池内部各自并无冲突
        assertFalse(detail.getFirstAmbiguousPrices().contains(2999), "首充池内 2999 无冲突");
        assertFalse(detail.getNoFirstAmbiguousPrices().contains(2999), "非首充池内 2999 无冲突");

        System.out.println("=== testParseIndependentFirstAndNoFirstDictionaries PASSED ===");
    }
}
