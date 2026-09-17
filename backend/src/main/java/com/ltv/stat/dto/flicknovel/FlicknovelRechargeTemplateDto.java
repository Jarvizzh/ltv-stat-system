package com.ltv.stat.dto.flicknovel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

/**
 * 充值模板 DTO
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class FlicknovelRechargeTemplateDto {

    @JsonProperty("recharge_template_id")
    private String rechargeTemplateId;

    @JsonProperty("name")
    private String name;

    @JsonProperty("detail")
    private Map<String, TemplatePlatformDetail> detail;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TemplatePlatformDetail {
        @JsonProperty("recharge")
        private ProductGroup recharge;

        @JsonProperty("subscribe")
        private ProductGroup subscribe;

        public ProductGroup getRecharge() { return recharge; }
        public void setRecharge(ProductGroup recharge) { this.recharge = recharge; }

        public ProductGroup getSubscribe() { return subscribe; }
        public void setSubscribe(ProductGroup subscribe) { this.subscribe = subscribe; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ProductGroup {
        @JsonProperty("first_products")
        private List<ProductItemWrapper> firstProducts;

        @JsonProperty("nofirst_products")
        private List<ProductItemWrapper> nofirstProducts;

        public List<ProductItemWrapper> getFirstProducts() { return firstProducts; }
        public void setFirstProducts(List<ProductItemWrapper> firstProducts) { this.firstProducts = firstProducts; }

        public List<ProductItemWrapper> getNofirstProducts() { return nofirstProducts; }
        public void setNofirstProducts(List<ProductItemWrapper> nofirstProducts) { this.nofirstProducts = nofirstProducts; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ProductItemWrapper {
        @JsonProperty("product")
        private ProductDetail product;

        @JsonProperty("vouchers")
        private Integer vouchers;

        @JsonProperty("is_recommend")
        private Boolean isRecommend;

        public ProductDetail getProduct() { return product; }
        public void setProduct(ProductDetail product) { this.product = product; }

        public Integer getVouchers() { return vouchers; }
        public void setVouchers(Integer vouchers) { this.vouchers = vouchers; }

        public Boolean getIsRecommend() { return isRecommend; }
        public void setIsRecommend(Boolean isRecommend) { this.isRecommend = isRecommend; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ProductDetail {
        @JsonProperty("product_id")
        private String productId;

        @JsonProperty("third_product_id")
        private String thirdProductId;

        @JsonProperty("name")
        private String name;

        @JsonProperty("benefit_type")
        private Integer benefitType; // 1-代币充值, 2-时长订阅

        @JsonProperty("client_os")
        private Integer clientOs; // 1-android, 2-ios, 3-web

        @JsonProperty("price_cents")
        private Integer priceCents;

        @JsonProperty("period")
        private Integer period; // 1-周, 2-月, 3-年, 4-日卡

        @JsonProperty("coins")
        private Integer coins;

        @JsonProperty("publish_status")
        private Integer publishStatus;

        @JsonProperty("dist_app_id")
        private Long distAppId;

        @JsonProperty("discount_period_num")
        private Integer discountPeriodNum;

        @JsonProperty("discount_price_cents")
        private Integer discountPriceCents;

        @JsonProperty("created_at")
        private Long createdAt;

        @JsonProperty("updated_at")
        private Long updatedAt;

        public String getProductId() { return productId; }
        public void setProductId(String productId) { this.productId = productId; }

        public String getThirdProductId() { return thirdProductId; }
        public void setThirdProductId(String thirdProductId) { this.thirdProductId = thirdProductId; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public Integer getBenefitType() { return benefitType; }
        public void setBenefitType(Integer benefitType) { this.benefitType = benefitType; }

        public Integer getClientOs() { return clientOs; }
        public void setClientOs(Integer clientOs) { this.clientOs = clientOs; }

        public Integer getPriceCents() { return priceCents; }
        public void setPriceCents(Integer priceCents) { this.priceCents = priceCents; }

        public Integer getPeriod() { return period; }
        public void setPeriod(Integer period) { this.period = period; }

        public Integer getCoins() { return coins; }
        public void setCoins(Integer coins) { this.coins = coins; }

        public Integer getPublishStatus() { return publishStatus; }
        public void setPublishStatus(Integer publishStatus) { this.publishStatus = publishStatus; }

        public Long getDistAppId() { return distAppId; }
        public void setDistAppId(Long distAppId) { this.distAppId = distAppId; }

        public Integer getDiscountPeriodNum() { return discountPeriodNum; }
        public void setDiscountPeriodNum(Integer discountPeriodNum) { this.discountPeriodNum = discountPeriodNum; }

        public Integer getDiscountPriceCents() { return discountPriceCents; }
        public void setDiscountPriceCents(Integer discountPriceCents) { this.discountPriceCents = discountPriceCents; }

        public Long getCreatedAt() { return createdAt; }
        public void setCreatedAt(Long createdAt) { this.createdAt = createdAt; }

        public Long getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(Long updatedAt) { this.updatedAt = updatedAt; }
    }

    public String getRechargeTemplateId() { return rechargeTemplateId; }
    public void setRechargeTemplateId(String rechargeTemplateId) { this.rechargeTemplateId = rechargeTemplateId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Map<String, TemplatePlatformDetail> getDetail() { return detail; }
    public void setDetail(Map<String, TemplatePlatformDetail> detail) { this.detail = detail; }
}
