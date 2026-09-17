package com.ltv.stat.dto.flicknovel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FlicknovelRechargeTemplateDataDto {

    @JsonProperty("recharge_templates")
    private List<FlicknovelRechargeTemplateDto> rechargeTemplates;

    public List<FlicknovelRechargeTemplateDto> getRechargeTemplates() {
        return rechargeTemplates != null ? rechargeTemplates : Collections.emptyList();
    }

    public void setRechargeTemplates(List<FlicknovelRechargeTemplateDto> rechargeTemplates) {
        this.rechargeTemplates = rechargeTemplates;
    }
}
