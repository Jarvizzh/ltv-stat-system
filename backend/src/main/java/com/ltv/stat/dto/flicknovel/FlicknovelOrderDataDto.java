package com.ltv.stat.dto.flicknovel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FlicknovelOrderDataDto {

    @JsonProperty("orders")
    private List<FlicknovelOrderDto> orders;

    public List<FlicknovelOrderDto> getOrders() {
        return orders != null ? orders : Collections.emptyList();
    }

    public void setOrders(List<FlicknovelOrderDto> orders) {
        this.orders = orders;
    }
}
