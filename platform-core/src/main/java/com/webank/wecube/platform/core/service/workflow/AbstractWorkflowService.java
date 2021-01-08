package com.webank.wecube.platform.core.service.workflow;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

/**
 * 
 * @author gavin
 *
 */
public abstract class AbstractWorkflowService {
    public static final String NODE_IDS_DELIMITER = ",";
    
    public static final String PROC_DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    
    public static final String EMPTY_ERROR_MSG = "";
    
    public static final String IS_SENSITIVE_ATTR = "Y";
    
    public static final String NODE_SUB_PROCESS = "subProcess";
    
    public static final String NODE_START_EVENT = "startEvent";
    public static final String NODE_END_EVENT = "endEvent";
    public static final String NODE_EXCLUSIVE_GATEWAY = "exclusiveGateway";
    public static final String NODE_PARALLEL_GATEWAY = "parallelGateway";
    
    protected static List<String> statelessNodeTypes = Arrays.asList(NODE_START_EVENT, NODE_END_EVENT, NODE_EXCLUSIVE_GATEWAY,
            NODE_PARALLEL_GATEWAY);
    
    protected List<String> unmarshalNodeIds(String nodeIdsAsString) {
        List<String> nodeIds = new ArrayList<>();
        if (StringUtils.isBlank(nodeIdsAsString)) {
            return nodeIds;
        }

        String[] parts = nodeIdsAsString.split(NODE_IDS_DELIMITER);

        for (int i = 0; i < parts.length; i++) {
            nodeIds.add(parts[i]);
        }

        return nodeIds;
    }
    
    protected String formatDate(Date date){
        if(date == null){
            return null;
        }
        DateFormat df = new SimpleDateFormat(PROC_DATETIME_PATTERN);
        return df.format(date);
    }
    
    
}
