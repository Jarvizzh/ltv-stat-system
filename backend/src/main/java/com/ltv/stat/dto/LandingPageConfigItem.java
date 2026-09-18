package com.ltv.stat.dto;

public class LandingPageConfigItem {
    private String platformCode = "rocnovel";
    private String landingPageId;
    private String timezone; // "CST" (北京时间), "UTC" (世界协调时), "ET" (美东时区)

    public LandingPageConfigItem() {}

    public LandingPageConfigItem(String landingPageId, String timezone) {
        this("rocnovel", landingPageId, timezone);
    }

    public LandingPageConfigItem(String platformCode, String landingPageId, String timezone) {
        this.platformCode = (platformCode != null && !platformCode.trim().isEmpty()) ? platformCode.trim() : "rocnovel";
        this.landingPageId = landingPageId;
        this.timezone = timezone;
    }

    public String getPlatformCode() {
        return platformCode != null ? platformCode : "rocnovel";
    }

    public void setPlatformCode(String platformCode) {
        this.platformCode = platformCode;
    }

    public String getLandingPageId() {
        return landingPageId;
    }

    public void setLandingPageId(String landingPageId) {
        this.landingPageId = landingPageId;
    }

    public String getTimezone() {
        if (timezone == null || timezone.trim().isEmpty() || "BJ".equalsIgnoreCase(timezone.trim())) {
            return "flicknovel".equalsIgnoreCase(platformCode) ? "UTC" : "CST";
        }
        return timezone.toUpperCase();
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }
}
