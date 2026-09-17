package com.ltv.stat.service.flicknovel;

import com.ltv.stat.dto.flicknovel.FlicknovelOrderDto;
import com.ltv.stat.entity.RawOrder;
import com.ltv.stat.enums.PlatformEnum;
import com.ltv.stat.repository.RawOrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class FlicknovelOrderCleanTest {

    @Autowired
    private FlicknovelApiService flicknovelApiService;

    @Autowired
    private RawOrderRepository rawOrderRepository;

    @Test
    @Transactional
    public void testCleanOrdersRenewTypeAndRegistrationDate() {
        String testMemberId = "test_dev_user_" + System.currentTimeMillis();

        // 订单1: 2026-09-01 10:00:00 (时间戳 1788228000) 首充 19.99
        long firstPayTs = 1788228000L;
        FlicknovelOrderDto o1 = new FlicknovelOrderDto();
        o1.setOrderId("test_ord_001_" + System.currentTimeMillis());
        o1.setDeviceId(testMemberId);
        o1.setPromotionId("prmt_page_100");
        o1.setCreatedAt(String.valueOf(firstPayTs));
        o1.setCompletedAt(String.valueOf(firstPayTs));
        o1.setUsPrice("19.99");

        // 订单2: 2026-09-03 10:00:00 (后两天时间戳 1788228000 + 86400 * 2) 复充 39.99
        long secondPayTs = firstPayTs + 86400 * 2;
        FlicknovelOrderDto o2 = new FlicknovelOrderDto();
        o2.setOrderId("test_ord_002_" + System.currentTimeMillis());
        o2.setDeviceId(testMemberId);
        o2.setPromotionId(""); // 推广链为空，测试自动继承
        o2.setCreatedAt(String.valueOf(secondPayTs));
        o2.setCompletedAt(String.valueOf(secondPayTs));
        o2.setUsPrice("39.99");

        // 批量执行清洗落库
        int cleaned = flicknovelApiService.batchCleanAndSaveOrders(Arrays.asList(o1, o2), Collections.emptyMap());
        assertEquals(2, cleaned, "应成功清洗入库 2 笔订单");

        // 从数据库查询两笔订单
        RawOrder ro1 = rawOrderRepository.findByPlatformCodeAndOrderId(PlatformEnum.FLICKNOVEL.getCode(), o1.getOrderId()).orElse(null);
        RawOrder ro2 = rawOrderRepository.findByPlatformCodeAndOrderId(PlatformEnum.FLICKNOVEL.getCode(), o2.getOrderId()).orElse(null);

        assertNotNull(ro1);
        assertNotNull(ro2);

        // 验证 1: 订单1 为首单 (renew_type = 1)，订单2 为复充 (renew_type = 2)
        assertEquals(1, ro1.getRenewType(), "订单1必须为首充 (renew_type=1)");
        assertEquals(2, ro2.getRenewType(), "订单2必须为复充 (renew_type=2)");

        // 验证 2: 订单1 与 订单2 的注册日期一致 (均继承最初注册归因日期)
        assertEquals(ro1.getRegisterDateEt(), ro2.getRegisterDateEt(), "老用户复充订单必须继承首单的注册日期");

        // 验证 3: 订单2 与 订单1 的支付日期相差 2 天 (Day 3 充值)
        long daysDiff = ChronoUnit.DAYS.between(ro2.getRegisterDateEt(), ro2.getPayDateEt());
        assertEquals(2, daysDiff, "第3天产生的复充相差应为 2 天 (进入 Day 3 LTV)");

        // 验证 4: 金额转换
        assertEquals(1999, ro1.getOrderAmountCent());
        assertEquals(new BigDecimal("19.99"), ro1.getOrderAmountUsd());
        assertEquals(3999, ro2.getOrderAmountCent());
        assertEquals(new BigDecimal("39.99"), ro2.getOrderAmountUsd());

        // 验证 5: 渠道继承
        assertEquals("prmt_page_100", ro2.getLandingPageId(), "第二单推广链为空时应自动继承首单渠道");

        System.out.println("=== 订单清洗逻辑测试全部通过！===");
    }
}
