package com.webank.wecube.platform.core.service.batchjob;

import lombok.Data;

import java.util.List;

@Data
public class GetContextRequest {
    private String host;
    private Integer  batchId;
    private Integer lineNumber;
    private Integer offSet;
}
