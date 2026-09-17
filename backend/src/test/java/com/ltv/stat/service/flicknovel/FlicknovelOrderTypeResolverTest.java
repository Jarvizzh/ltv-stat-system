package com.ltv.stat.service.flicknovel;

import com.ltv.stat.dto.flicknovel.FlicknovelOrderDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class FlicknovelOrderTypeResolverTest {

    private FlicknovelOrderTypeResolver resolver;

    @BeforeEach
    public void setUp() {
        resolver = new FlicknovelOrderTypeResolver();
    }

    /**
     * 测试第一优先级: 零耦合 OpenAPI 显式字段透出
     * 当番茄 OpenAPI 未来返回 benefit_type 或 product_id 时，直接命中生效
     */
    @Test
    public void testPriority1_ExplicitOpenApiFields() {
        FlicknovelOrderTypeResolver.OrderResolveContext ctx = new FlicknovelOrderTypeResolver.OrderResolveContext();
        FlicknovelOrderDto dto = new FlicknovelOrderDto();
        ctx.setDto(dto);
        ctx.setOrderAmountCent(3999);

        // 1. benefit_type 显式指定
        dto.setBenefitType(2); // 订阅
        assertEquals(1, resolver.resolve(ctx), "benefit_type=2 必须直接判定为订阅 (1)");

        dto.setBenefitType(1); // 代币单充
        assertEquals(0, resolver.resolve(ctx), "benefit_type=1 必须直接判定为代币单充 (0)");

        // 2. product_id 显式指定
        dto.setBenefitType(null);
        dto.setProductId("novel.h5.sub.week.4000");
        assertEquals(1, resolver.resolve(ctx), "productId 包含 sub 必须直接判定为订阅 (1)");

        dto.setProductId("novel.h5.coins.4000");
        assertEquals(0, resolver.resolve(ctx), "productId 包含 coins 必须直接判定为代币单充 (0)");
    }

    /**
     * 测试第二优先级: 模板无歧义金额直接映射
     */
    @Test
    public void testPriority2_UnambiguousPrices() {
        FlicknovelOrderTypeResolver.OrderResolveContext ctx = new FlicknovelOrderTypeResolver.OrderResolveContext();
        ctx.setDto(new FlicknovelOrderDto());

        Map<Integer, Integer> priceMap = new HashMap<>();
        priceMap.put(1199, 0); // 代币单充
        priceMap.put(19899, 1); // 年卡订阅
        ctx.setTemplatePriceMap(priceMap);
        ctx.setAmbiguousPrices(Collections.singleton(3999)); // 3999 为冲突金额

        // 1199 -> 0
        ctx.setOrderAmountCent(1199);
        assertEquals(0, resolver.resolve(ctx), "1199 无歧义直接映射为单充 (0)");

        // 19899 -> 1
        ctx.setOrderAmountCent(19899);
        assertEquals(1, resolver.resolve(ctx), "19899 无歧义直接映射为订阅 (1)");
    }

    /**
     * 测试第三优先级: 冲突金额时序消歧 (以 39.99 为例)
     */
    @Test
    public void testPriority3_AmbiguousPrice_TemporalResolution() {
        FlicknovelOrderTypeResolver.OrderResolveContext ctx = new FlicknovelOrderTypeResolver.OrderResolveContext();
        ctx.setDto(new FlicknovelOrderDto());
        ctx.setOrderAmountCent(3999);
        ctx.setAmbiguousPrices(Collections.singleton(3999)); // 3999 属于冲突金额
        ctx.setTemplateHasIntroOffer(true); // 模板有首购优惠 (如 19.99)

        // 场景 A: 首单充值 39.99
        // 模板有 $19.99 首购特惠，用户支付了 $39.99 原价，说明没选订阅优惠，而是主动充值了代币
        ctx.setRenewType(1); // 首充
        ctx.setHasSubscribed(false);
        int resultFirstPay = resolver.resolve(ctx);
        assertEquals(0, resultFirstPay, "首单支付冲突原价 3999 且存在首充优惠时，必须判定为单充代币 (0)");

        // 场景 B: 老用户复充 39.99，且该用户在 7 天前曾订阅过
        LocalDateTime now = LocalDateTime.of(2026, 9, 18, 10, 0, 0);
        LocalDateTime sevenDaysAgo = now.minusDays(7); // 7天前购买首周

        ctx.setRenewType(2); // 复充
        ctx.setPayTimeBj(now);
        ctx.setHasSubscribed(true);
        ctx.setLatestSubsPayTime(sevenDaysAgo);
        int resultRenewal = resolver.resolve(ctx);
        assertEquals(1, resultRenewal, "距离上次订阅 7 天的复充 3999，必须判定为周订自动续费 (1)");

        // 场景 C: 老用户复充 39.99，但用户此前从无订阅记录
        ctx.setRenewType(2);
        ctx.setHasSubscribed(false);
        ctx.setLatestSubsPayTime(null);
        int resultRepeatCoins = resolver.resolve(ctx);
        assertEquals(0, resultRepeatCoins, "无订阅历史的复充 3999，必须判定为代币单充 (0)");
    }
}
