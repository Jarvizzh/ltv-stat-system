package com.ltv.stat.adapter;

import com.ltv.stat.entity.PlatformConfig;
import com.ltv.stat.enums.PlatformEnum;

import java.time.LocalDate;

/**
 * 平台同步适配器统一接口规范
 * 强制绑定强类型 PlatformEnum，彻底淘汰魔术字符串
 */
public interface PlatformSyncAdapter {

    /**
     * 绑定的平台枚举
     */
    PlatformEnum getPlatform();

    /**
     * 按起止日期区间同步订单（写入时就清洗为统一的 RawOrder 实体）
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param config 平台配置与凭证
     * @return 实际保存/更新的订单数量
     */
    int syncOrders(LocalDate startDate, LocalDate endDate, PlatformConfig config);

    /**
     * 按落地页 ID 维度同步订单
     * @param landingPageId 落地页/渠道代码
     * @param config 平台配置与凭证
     * @return 实际保存/更新的订单数量
     */
    int syncOrdersForLandingPage(String landingPageId, PlatformConfig config);

    /**
     * 同步渠道/落地页及订阅配置产品版本快照
     * @param config 平台配置与凭证
     * @return 保存/更新的版本记录数
     */
    int syncConfigs(PlatformConfig config);
}
