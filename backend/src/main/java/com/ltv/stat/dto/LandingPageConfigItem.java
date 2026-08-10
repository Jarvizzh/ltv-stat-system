package com.ltv.stat.dto;

public class LandingPageConfigItem {
    private String landingPageId;
    private String timezone; // "ET" (美东) or "BJ" (北京)

    public LandingPageConfigItem() {}

    public LandingPageConfigItem(String landingPageId, String timezone) {
        this.landingPageId = landingPageId;
        this.timezone = timezone;
    }

    public String getLandingPageId() {
        return landingPageId;
    }

    public void setLandingPageId(String landingPageId) {
        this.landingPageId = landingPageId;
    }

    public String getTimezone() {
        return timezone != null ? timezone : "ET";
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }
}
