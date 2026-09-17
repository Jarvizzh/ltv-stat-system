package com.ltv.stat.service.flicknovel;

import com.ltv.stat.dto.flicknovel.FlicknovelOrderDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

/**
 * 番茄海外订单充值类型解析策略组件
 * 
 * 架构设计原则: 高内聚、低耦合。
 * 1. 一旦番茄 OpenAPI 未来在报文中透出 benefit_type 或 product_id 等显式字段，
 *    第一优先级立即无缝生效，无需下游修改任何代码。
 * 2. 在字段未透出的过渡期，依托模板价格字典与用户时序窗口（首购特惠 vs 7天周订自动续费）启发式精准消歧。
 */
@Component
public class FlicknovelOrderTypeResolver {

    private static final Logger log = LoggerFactory.getLogger(FlicknovelOrderTypeResolver.class);

    /**
     * 判定上下文对象
     */
    public static class OrderResolveContext {
        private FlicknovelOrderDto dto;
        private String promotionId;
        private int orderAmountCent;
        private int renewType; // 1=首单, 2=老用户复充
        private LocalDateTime payTimeBj;
        private boolean hasSubscribed; // 用户历史是否有过订阅记录
        private LocalDateTime latestSubsPayTime; // 用户最近一次订阅支付时间
        private Map<Integer, Integer> templatePriceMap; // 该推广模板解析出的价格映射
        private Set<Integer> ambiguousPrices; // 该模板中同时存在单充与订阅的冲突价格集合 (如 3999)
        private boolean templateHasIntroOffer; // 该模板是否存在低于原价的订阅首充优惠 (如 1999, 2999)

        public FlicknovelOrderDto getDto() { return dto; }
        public void setDto(FlicknovelOrderDto dto) { this.dto = dto; }

        public String getPromotionId() { return promotionId; }
        public void setPromotionId(String promotionId) { this.promotionId = promotionId; }

        public int getOrderAmountCent() { return orderAmountCent; }
        public void setOrderAmountCent(int orderAmountCent) { this.orderAmountCent = orderAmountCent; }

        public int getRenewType() { return renewType; }
        public void setRenewType(int renewType) { this.renewType = renewType; }

        public LocalDateTime getPayTimeBj() { return payTimeBj; }
        public void setPayTimeBj(LocalDateTime payTimeBj) { this.payTimeBj = payTimeBj; }

        public boolean isHasSubscribed() { return hasSubscribed; }
        public void setHasSubscribed(boolean hasSubscribed) { this.hasSubscribed = hasSubscribed; }

        public LocalDateTime getLatestSubsPayTime() { return latestSubsPayTime; }
        public void setLatestSubsPayTime(LocalDateTime latestSubsPayTime) { this.latestSubsPayTime = latestSubsPayTime; }

        public Map<Integer, Integer> getTemplatePriceMap() { return templatePriceMap; }
        public void setTemplatePriceMap(Map<Integer, Integer> templatePriceMap) { this.templatePriceMap = templatePriceMap; }

        public Set<Integer> getAmbiguousPrices() { return ambiguousPrices; }
        public void setAmbiguousPrices(Set<Integer> ambiguousPrices) { this.ambiguousPrices = ambiguousPrices; }

        public boolean isTemplateHasIntroOffer() { return templateHasIntroOffer; }
        public void setTemplateHasIntroOffer(boolean templateHasIntroOffer) { this.templateHasIntroOffer = templateHasIntroOffer; }
    }

    /**
     * 核心判定方法
     * 
     * @param ctx 订单上下文
     * @return 0=单充/代币, 1=时长订阅
     */
    public int resolve(OrderResolveContext ctx) {
        if (ctx == null) {
            return 0;
        }

        // =========================================================================
        // 【第一优先级: OpenAPI 显式字段检查 (Zero-Coupling)】
        // 若番茄官方未来在接口返回 benefit_type 或 product_id，直接无缝生效！
        // =========================================================================
        if (ctx.getDto() != null) {
            if (ctx.getDto().getBenefitType() != null) {
                int explicitType = ctx.getDto().getBenefitType();
                log.debug("[OrderTypeResolver] Matched explicit benefit_type: {}", explicitType);
                return explicitType == 2 ? 1 : 0;
            }
            if (ctx.getDto().getProductId() != null && !ctx.getDto().getProductId().trim().isEmpty()) {
                String pLower = ctx.getDto().getProductId().toLowerCase();
                if (pLower.contains("sub") || pLower.contains("vip") || pLower.contains("week")
                        || pLower.contains("month") || pLower.contains("year") || pLower.contains("day")) {
                    log.debug("[OrderTypeResolver] Matched explicit subscription productId: {}", ctx.getDto().getProductId());
                    return 1;
                }
                if (pLower.contains("coin") || pLower.contains("token") || pLower.contains("recharge")) {
                    log.debug("[OrderTypeResolver] Matched explicit coins productId: {}", ctx.getDto().getProductId());
                    return 0;
                }
            }
        }

        int amountCent = ctx.getOrderAmountCent();
        Map<Integer, Integer> priceMap = ctx.getTemplatePriceMap();
        Set<Integer> ambiguousPrices = ctx.getAmbiguousPrices();

        // =========================================================================
        // 【第二优先级: 冲突金额时序消歧 (Heuristic Temporal Resolution)】
        // 当金额同时存在于单充和订阅 (例如 3999 既是周订又是代币)，进行生命周期消歧
        // =========================================================================
        boolean isAmbiguous = (ambiguousPrices != null && ambiguousPrices.contains(amountCent));
        if (isAmbiguous) {
            return resolveAmbiguousPrice(ctx);
        }

        // =========================================================================
        // 【第三优先级: 模板无冲突价格直接映射】
        // =========================================================================
        if (priceMap != null && priceMap.containsKey(amountCent)) {
            return priceMap.get(amountCent);
        }

        // 默认兜底为代币单充 (0)
        return 0;
    }

    /**
     * 针对单充与订阅同金额（如 39.99 / 3999 美分）的时序生命周期消歧
     */
    private int resolveAmbiguousPrice(OrderResolveContext ctx) {
        int renewType = ctx.getRenewType();
        int amountCent = ctx.getOrderAmountCent();

        // 1. 首单逻辑 (renew_type == 1):
        // 如果模板中配有低价首购优惠 (例如 $19.99 或 $29.99 的 Intro Price)，
        // 用户首次充值如果付的是 $39.99，说明根本没选置顶的优惠订阅，而是主动选择了 $39.99 代币单充档位！
        if (renewType == 1) {
            if (ctx.isTemplateHasIntroOffer()) {
                log.info("[OrderTypeResolver] First purchase at full price {} when intro offer exists -> Treated as Coins Recharge (0)", amountCent);
                return 0;
            } else {
                // 若模板完全没有首充优惠（纯原价模板），根据置顶偏好优先判定为订阅
                return 1;
            }
        }

        // 2. 复充逻辑 (renew_type == 2):
        // 检查该用户此前是否有过订阅记录
        if (ctx.isHasSubscribed() && ctx.getLatestSubsPayTime() != null && ctx.getPayTimeBj() != null) {
            long daysDiff = Duration.between(ctx.getLatestSubsPayTime(), ctx.getPayTimeBj()).toDays();
            // 周订续费窗口：距离上一次订阅订单大约 7 天 (允许 5 ~ 9 天网络或系统宽限期，或者隔周 12 ~ 16 天)
            if ((daysDiff >= 5 && daysDiff <= 9) || (daysDiff >= 12 && daysDiff <= 16)) {
                log.info("[OrderTypeResolver] Repeat purchase {} occurs {} days after previous subscription -> Treated as Weekly Renewal (1)",
                        amountCent, daysDiff);
                return 1;
            }
            // 月订续费窗口：约 30 天 (27 ~ 33 天)
            if (daysDiff >= 27 && daysDiff <= 33) {
                log.info("[OrderTypeResolver] Repeat purchase {} occurs {} days after previous subscription -> Treated as Monthly Renewal (1)",
                        amountCent, daysDiff);
                return 1;
            }
        }

        // 用户此前无订阅记录，或复充时间完全不符合订阅续订周期律 -> 判定为代币单充
        log.info("[OrderTypeResolver] Repeat purchase {} does not match subscription cycle -> Treated as Coins Recharge (0)", amountCent);
        return 0;
    }
}
