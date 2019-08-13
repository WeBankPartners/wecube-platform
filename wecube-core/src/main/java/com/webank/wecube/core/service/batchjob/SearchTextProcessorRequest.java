package com.webank.wecube.core.service.batchjob;

import lombok.Data;

import java.util.List;

@Data
public class SearchTextProcessorRequest {
        private List<String> hosts;
        private Integer  batchId;
        private String pattern;
}
