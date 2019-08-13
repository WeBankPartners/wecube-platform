package com.webank.wecube.core.service.textprocessor;
import java.util.List;
public class SearchTextProcessorRequest {
    private List <String> hosts;
    private Integer  batchId;
    private String pattern;

    public List<String> getHosts() {
        return this.hosts;
    }

    public String getPattern() {
        return this.pattern;
    }

    public Integer getBatchId() {
        return this.batchId;
    }
}
