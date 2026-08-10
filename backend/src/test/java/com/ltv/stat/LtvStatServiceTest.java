package com.ltv.stat;

import com.ltv.stat.dto.DailyDistributionSummaryDto;
import com.ltv.stat.entity.LtvDailyStat;
import com.ltv.stat.entity.LtvDailyStatId;
import com.ltv.stat.entity.RawOrder;
import com.ltv.stat.repository.LtvDailyStatRepository;
import com.ltv.stat.repository.LtvLaunchConfigRepository;
import com.ltv.stat.repository.RawOrderRepository;
import com.ltv.stat.service.LtvStatService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
public class LtvStatServiceTest {

    @Autowired
    private RawOrderRepository rawOrderRepository;

    @Autowired
    private LtvLaunchConfigRepository ltvLaunchConfigRepository;

    @Autowired
    private LtvStatService ltvStatService;

    @Autowired
    private com.ltv.stat.service.DailyRechargeStatService dailyRechargeStatService;

    @Autowired
    private LtvDailyStatRepository ltvDailyStatRepository;

    @Autowired
    private com.ltv.stat.repository.UserLandingPageRepository userLandingPageRepository;

    @Test
    public void testLtvCalculationEngine() {
        LocalDate testCohortDate = LocalDate.of(2026, 7, 12);

        // 清理原有数据
        rawOrderRepository.deleteAll();
        ltvDailyStatRepository.deleteAll();
        userLandingPageRepository.deleteAll();

        // 绑定落地页给用户 1L
        com.ltv.stat.entity.UserLandingPage ulp = new com.ltv.stat.entity.UserLandingPage();
        ulp.setUserId(1L);
        ulp.setLandingPageId("lp_999");
        ulp.setTimezone("BJ");
        userLandingPageRepository.save(ulp);

        // 创建模版模拟订单
        RawOrder order1 = new RawOrder();
        order1.setOrderId("ord_1001_1");
        order1.setMemberId("mem_1001");
        order1.setLandingPageId("lp_999");
        order1.setRegisterTimeBj(LocalDateTime.of(2026, 7, 12, 12, 0, 0));
        order1.setRegisterTimeEt(LocalDateTime.of(2026, 7, 12, 0, 0, 0));
        order1.setRegisterDateEt(testCohortDate);
        order1.setPayTimeBj(LocalDateTime.of(2026, 7, 12, 14, 0, 0));
        order1.setPayTimeEt(LocalDateTime.of(2026, 7, 12, 2, 0, 0));
        order1.setPayDateEt(testCohortDate);
        order1.setOrderAmountCent(1000);
        order1.setOrderAmountUsd(new BigDecimal("10.00"));
        order1.setIsSubs(1);
        order1.setPayState(1);
        rawOrderRepository.save(order1);

        RawOrder order2 = new RawOrder();
        order2.setOrderId("ord_1001_2");
        order2.setMemberId("mem_1001");
        order2.setLandingPageId("lp_999");
        order2.setRegisterTimeBj(LocalDateTime.of(2026, 7, 12, 12, 0, 0));
        order2.setRegisterTimeEt(LocalDateTime.of(2026, 7, 12, 0, 0, 0));
        order2.setRegisterDateEt(testCohortDate);
        order2.setPayTimeBj(LocalDateTime.of(2026, 7, 13, 14, 0, 0));
        order2.setPayTimeEt(LocalDateTime.of(2026, 7, 13, 2, 0, 0));
        order2.setPayDateEt(testCohortDate.plusDays(1));
        order2.setOrderAmountCent(2000);
        order2.setOrderAmountUsd(new BigDecimal("20.00"));
        order2.setIsSubs(1);
        order2.setPayState(1);
        rawOrderRepository.save(order2);

        // 设置消耗 $100
        ltvStatService.saveLaunchConfig(testCohortDate, new BigDecimal("100.00"), "模拟测试");
        ltvStatService.calculateLtvStatsForUser(1L);

        List<LtvDailyStat> stats = ltvDailyStatRepository.findAll();
        assertFalse(stats.isEmpty());

        LtvDailyStat targetStat = ltvDailyStatRepository.findById(new LtvDailyStatId(1L, testCohortDate)).orElse(null);
        assertNotNull(targetStat);

        // 校验总充值 $30.00
        assertEquals(0, new BigDecimal("30.00").compareTo(targetStat.getTotalRecharge()));
        // 校验总盈亏 = $30.00 - $100.00 = -$70.00
        assertEquals(0, new BigDecimal("-70.00").compareTo(targetStat.getTotalProfit()));
        // 校验去重订阅用户数 1 人
        assertEquals(1, targetStat.getSubUserCount());
        // 校验订阅用户成本 = $100 / 1 = $100.00
        assertEquals(0, new BigDecimal("100.00").compareTo(targetStat.getSubUserCost()));

        // 校验 Day 1 累计充值 $10.00，Day 1 ROI = 10.00 / 100.00 = 0.1000 (10%)
        assertEquals(0, new BigDecimal("10.00").compareTo(targetStat.getDay1Recharge()));
        assertEquals(0, new BigDecimal("0.1000").compareTo(targetStat.getDay1Roi()));

        // 校验 Day 2 累计充值 $30.00，Day 2 ROI = 30.00 / 100.00 = 0.3000 (30%)
        assertEquals(0, new BigDecimal("30.00").compareTo(targetStat.getDay2Recharge()));
        assertEquals(0, new BigDecimal("0.3000").compareTo(targetStat.getDay2Roi()));

        // 重算测试：连续再次执行重算（旧数据已存在于数据库），验证是否报错 StaleStateException / ObjectOptimisticLockingFailureException
        assertDoesNotThrow(() -> {
            ltvStatService.calculateLtvStatsForUser(1L);
            dailyRechargeStatService.calculateDailyDistributionStatsForUser(1L);
        });
    }

    @Test
    public void testRefundStatusAndLastMonthRefundSummary() {
        rawOrderRepository.deleteAll();
        LocalDate today = LocalDate.now();
        LocalDate lastMonthDate = today.minusMonths(1).withDayOfMonth(15);

        RawOrder refundOrder = new RawOrder();
        refundOrder.setOrderId("ord_refund_1");
        refundOrder.setMemberId("mem_refund_1");
        refundOrder.setLandingPageId("lp_999");
        refundOrder.setRegisterTimeBj(lastMonthDate.atTime(10, 0));
        refundOrder.setRegisterTimeEt(lastMonthDate.atTime(0, 0));
        refundOrder.setRegisterDateEt(lastMonthDate);
        refundOrder.setPayTimeBj(lastMonthDate.atTime(10, 30));
        refundOrder.setPayTimeEt(lastMonthDate.atTime(0, 30));
        refundOrder.setPayDateEt(lastMonthDate);
        refundOrder.setOrderAmountCent(5000);
        refundOrder.setOrderAmountUsd(new BigDecimal("50.00"));
        refundOrder.setIsSubs(1);
        refundOrder.setPayState(1);
        refundOrder.setRefundStatus(2); // 已退款
        rawOrderRepository.save(refundOrder);

        RawOrder thisMonthRefundOrder = new RawOrder();
        thisMonthRefundOrder.setOrderId("ord_refund_this_month");
        thisMonthRefundOrder.setMemberId("mem_refund_2");
        thisMonthRefundOrder.setLandingPageId("lp_999");
        thisMonthRefundOrder.setRegisterTimeBj(today.atTime(10, 0));
        thisMonthRefundOrder.setRegisterTimeEt(today.atTime(0, 0));
        thisMonthRefundOrder.setRegisterDateEt(today);
        thisMonthRefundOrder.setPayTimeBj(today.atTime(10, 30));
        thisMonthRefundOrder.setPayTimeEt(today.atTime(0, 30));
        thisMonthRefundOrder.setPayDateEt(today);
        thisMonthRefundOrder.setOrderAmountCent(3000);
        thisMonthRefundOrder.setOrderAmountUsd(new BigDecimal("30.00"));
        thisMonthRefundOrder.setIsSubs(1);
        thisMonthRefundOrder.setPayState(1);
        thisMonthRefundOrder.setRefundStatus(2); // 已退款
        rawOrderRepository.save(thisMonthRefundOrder);

        List<RawOrder> orders = rawOrderRepository.findAll();
        DailyDistributionSummaryDto summary = dailyRechargeStatService.calculateDistributionSummaryFromOrders(orders);

        assertNotNull(summary.getLastMonthRefund());
        assertEquals(0, new BigDecimal("50.00").compareTo(summary.getLastMonthRefund()));

        assertNotNull(summary.getThisMonthRefund());
        assertEquals(0, new BigDecimal("30.00").compareTo(summary.getThisMonthRefund()));
    }
}
