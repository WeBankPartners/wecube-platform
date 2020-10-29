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
    
    protected static List<String> statelessNodeTypes = Arrays.asList("startEvent", "endEvent", "exclusiveGateway",
            "parallelGateway");
    
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
        DateFormat df = new SimpleDateFormat(PROC_DATETIME_PATTERN);
        return df.format(date);
    }
    
    
}
