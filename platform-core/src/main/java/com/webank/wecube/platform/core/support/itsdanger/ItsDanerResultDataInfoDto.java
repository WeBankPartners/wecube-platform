package com.webank.wecube.platform.core.support.itsdanger;

import java.util.List;

public class ItsDanerResultDataInfoDto {

    private String text;

    private List<Object> data;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public List<Object> getData() {
        return data;
    }

    public void setData(List<Object> data) {
        this.data = data;
    }

}
