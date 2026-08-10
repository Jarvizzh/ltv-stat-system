package com.ltv.stat.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LandingPageConfigResponseDto {

    private Integer code;
    private String msg;
    private LandingPageData data;

    public Integer getCode() { return code; }
    public void setCode(Integer code) { this.code = code; }

    public String getMsg() { return msg; }
    public void setMsg(String msg) { this.msg = msg; }

    public LandingPageData getData() { return data; }
    public void setData(LandingPageData data) { this.data = data; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LandingPageData {
        private Integer current;
        private Integer pages;
        private Integer size;
        private Integer total;
        private List<LandingPageRecordDto> records;

        public Integer getCurrent() { return current; }
        public void setCurrent(Integer current) { this.current = current; }

        public Integer getPages() { return pages; }
        public void setPages(Integer pages) { this.pages = pages; }

        public Integer getSize() { return size; }
        public void setSize(Integer size) { this.size = size; }

        public Integer getTotal() { return total; }
        public void setTotal(Integer total) { this.total = total; }

        public List<LandingPageRecordDto> getRecords() { return records; }
        public void setRecords(List<LandingPageRecordDto> records) { this.records = records; }
    }
}
