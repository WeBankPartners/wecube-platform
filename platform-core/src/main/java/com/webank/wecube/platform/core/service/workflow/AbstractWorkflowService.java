package com.webank.wecube.platform.core.service.workflow;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public abstract class AbstractWorkflowService {
    public static final String NODE_IDS_DELIMITER = ",";
    
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
}
