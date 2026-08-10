package com.ltv.stat.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SubscribeConfigProductResponseDto {

    private Integer code;
    private String msg;
    private SubscribeProductData data;

    public Integer getCode() { return code; }
    public void setCode(Integer code) { this.code = code; }

    public String getMsg() { return msg; }
    public void setMsg(String msg) { this.msg = msg; }

    public SubscribeProductData getData() { return data; }
    public void setData(SubscribeProductData data) { this.data = data; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SubscribeProductData {
        private Integer current;
        private Integer pages;
        private Integer size;
        private Integer total;
        private List<SubscribeConfigProductRecordDto> records;

        public Integer getCurrent() { return current; }
        public void setCurrent(Integer current) { this.current = current; }

        public Integer getPages() { return pages; }
        public void setPages(Integer pages) { this.pages = pages; }

        public Integer getSize() { return size; }
        public void setSize(Integer size) { this.size = size; }

        public Integer getTotal() { return total; }
        public void setTotal(Integer total) { this.total = total; }

        public List<SubscribeConfigProductRecordDto> getRecords() { return records; }
        public void setRecords(List<SubscribeConfigProductRecordDto> records) { this.records = records; }
    }
}
