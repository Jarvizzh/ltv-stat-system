package com.ltv.stat.dto;

/**
 * 通用 API 响应 DTO 包装类
 */
public class ApiResponseDto<T> {
    private int code;
    private String msg;
    private T data;

    public ApiResponseDto() {
        this(0, "success", null);
    }

    public ApiResponseDto(int code, String msg) {
        this(code, msg, null);
    }

    public ApiResponseDto(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static <T> ApiResponseDto<T> success(T data) {
        return new ApiResponseDto<>(0, "success", data);
    }

    public static <T> ApiResponseDto<T> success(String msg, T data) {
        return new ApiResponseDto<>(0, msg, data);
    }

    public static <T> ApiResponseDto<T> error(int code, String msg) {
        return new ApiResponseDto<>(code, msg, null);
    }

    public int getCode() { return code; }
    public void setCode(int code) { this.code = code; }

    public String getMsg() { return msg; }
    public void setMsg(String msg) { this.msg = msg; }

    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
}
