package com.webank.wecube.platform.core.dto.plugin;

public class UrlToResponseDto {
    private String requestUrl;
    private CommonResponseDto responseDto;

    public UrlToResponseDto(String requestUrl, CommonResponseDto responseDto) {
        this.requestUrl = requestUrl;
        this.responseDto = responseDto;
    }

    public UrlToResponseDto() {
    }

    public String getRequestUrl() {
        return requestUrl;
    }

    public void setRequestUrl(String requestUrl) {
        this.requestUrl = requestUrl;
    }

    public CommonResponseDto getResponseDto() {
        return responseDto;
    }

    public void setResponseDto(CommonResponseDto responseDto) {
        this.responseDto = responseDto;
    }
}
