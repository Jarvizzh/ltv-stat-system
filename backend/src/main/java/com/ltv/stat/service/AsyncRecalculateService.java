package com.ltv.stat.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 异步重算服务：用于子账号消耗/落地页发生变更时，在后台线程中异步触发主账号的数据聚合与报表重算，
 * 避免阻塞前端请求造成长时间等待。
 */
@Service
public class AsyncRecalculateService {

    private static final Logger log = LoggerFactory.getLogger(AsyncRecalculateService.class);

    private final LtvStatService ltvStatService;
    private final DailyRechargeStatService dailyRechargeStatService;
    private final UserService userService;

    public AsyncRecalculateService(@Lazy LtvStatService ltvStatService,
                                   @Lazy DailyRechargeStatService dailyRechargeStatService,
                                   UserService userService) {
        this.ltvStatService = ltvStatService;
        this.dailyRechargeStatService = dailyRechargeStatService;
        this.userService = userService;
    }

    /**
     * 异步触发指定子账号关联的所有父级主账号进行报表重算 (LTV 报表 + 充值分布报表)
     */
    @Async
    public void asyncRecalculateMastersForSubUser(Long subUserId) {
        if (subUserId == null) return;
        try {
            List<Long> parentMasterIds = userService.getMasterUserIdsForSub(subUserId);
            if (parentMasterIds.isEmpty()) return;

            log.info("【异步重算】开始异步触发子账号 [{}] 关联的 {} 个主账号数据汇总重算...", subUserId, parentMasterIds.size());
            for (Long masterId : parentMasterIds) {
                try {
                    ltvStatService.calculateLtvStatsForUserDirect(masterId);
                    dailyRechargeStatService.calculateDailyDistributionStatsForUserDirect(masterId);
                    ltvStatService.invalidateUserCache(masterId);
                    log.info("【异步重算】主账号 [{}] 报表异步重算完成", masterId);
                } catch (Exception e) {
                    log.error("【异步重算】主账号 [" + masterId + "] 异步重算失败", e);
                }
            }
        } catch (Exception e) {
            log.error("【异步重算】子账号 [" + subUserId + "] 触发主账号重算异常", e);
        }
    }
}
