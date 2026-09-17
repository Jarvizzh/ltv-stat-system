package com.ltv.stat.dto.flicknovel;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FlicknovelPromotionDataDto {

    @JsonProperty("Promotions")
    @JsonAlias({"promotions"})
    private List<FlicknovelPromotionDto> promotions;

    public List<FlicknovelPromotionDto> getPromotions() {
        return promotions != null ? promotions : Collections.emptyList();
    }

    public void setPromotions(List<FlicknovelPromotionDto> promotions) {
        this.promotions = promotions;
    }
}
