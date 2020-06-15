package com.webank.wecube.platform.core.support.plugin.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PluginLoggingInfoRequestParameter {
    @JsonProperty(value = "key_word")
    private String keyWord;
    // @JsonProperty(value="line_number")
    // private Integer lineNumber;

    public String getKeyWord() {
        return keyWord;
    }

    public void setKeyWord(String keyWord) {
        this.keyWord = keyWord;
    }

}
