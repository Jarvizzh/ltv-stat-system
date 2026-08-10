package com.ltv.stat.service;

import com.ltv.stat.entity.LtvDailyStat;
import com.ltv.stat.entity.RawOrder;

import java.util.List;
import java.util.Map;

/**
 * 用户 LTV 计算上下文共享对象 (方案二：Context Sharing)
 * 在单次 /list 请求链路中，全量订单与时区映射仅提取解析 1 次，后续各子计算方法复用共享。
 */
public class UserCalculationContext {
    public final Long userId;
    public final List<LtvDailyStat> allStats;
    public final List<RawOrder> userOrders;
    public final Map<String, String> tzMap;

    public UserCalculationContext(Long userId, List<LtvDailyStat> allStats, List<RawOrder> userOrders, Map<String, String> tzMap) {
        this.userId = userId;
        this.allStats = allStats;
        this.userOrders = userOrders;
        this.tzMap = tzMap;
    }
}
