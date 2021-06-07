package com.webank.wecube.platform.core.service;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.webank.wecube.platform.core.service.dme.EntityQueryExprNodeInfo;
import com.webank.wecube.platform.core.service.dme.EntityQueryExpressionParser;

@RunWith(SpringRunner.class)
@SpringBootTest
public class RootTests {

    @Autowired
    protected EntityQueryExpressionParser entityQueryExpressionParser;

    @Test
    public void test() {
        String currTaskNodeRoutineExp = "wecmdb:data_center{data_center_type eq 'GLOBAL'}~(global_dc)wecmdb:data_center{data_center_type eq 'REGION'}~(data_center)wecmdb:network_zone{network_zone_type eq 'VPC'}~(network_zone)wecmdb:route_table";
        String bindTaskNodeRoutineExp = "wecmdb:data_center{data_center_type eq 'GLOBAL'}~(global_dc)wecmdb:data_center{data_center_type eq 'REGION'}~(data_center)wecmdb:network_zone{network_zone_type eq 'VPC'}";
        String currFullEntityDataId = "data_center_60b9b66dbdb047a7::data_center_60b9b6b752cca4a8::network_zone_60b9ca7c60b15196::route_table_60b9cac46c3f5c5b";
        String lastFullEntityDataId = "data_center_60b9b66dbdb047a7::data_center_60b9b6b752cca4a8::network_zone_60b9ca52e4bdffd0";
        
        boolean ret = checkIfNeedPickoutFromContext( currTaskNodeRoutineExp,  bindTaskNodeRoutineExp,
                 currFullEntityDataId,  lastFullEntityDataId);
        System.out.println(ret);
    }

    private boolean checkIfNeedPickoutFromContext(String currTaskNodeRoutineExp, String bindTaskNodeRoutineExp,
            String currFullEntityDataId, String lastFullEntityDataId) {

        if (StringUtils.isBlank(currTaskNodeRoutineExp) || StringUtils.isBlank(bindTaskNodeRoutineExp)
                || StringUtils.isBlank(currFullEntityDataId) || StringUtils.isBlank(lastFullEntityDataId)) {
            return true;
        }

        List<EntityQueryExprNodeInfo> currExprNodeInfos = this.entityQueryExpressionParser
                .parse(currTaskNodeRoutineExp);
        List<EntityQueryExprNodeInfo> lastExprNodeInfos = this.entityQueryExpressionParser
                .parse(bindTaskNodeRoutineExp);

        if (currExprNodeInfos == null || currExprNodeInfos.isEmpty()) {
            return true;
        }

        if (lastExprNodeInfos == null || lastExprNodeInfos.isEmpty()) {
            return true;
        }

        int currExprNodeInfoIndex = -1;
        int lastExprNodeInfoIndex = -1;

        int currExprNodeInfoSize = currExprNodeInfos.size();
        int lastExprNodeInfoSize = lastExprNodeInfos.size();

        for (int currIndex = (currExprNodeInfoSize - 1); currIndex >= 0; currIndex--) {
            EntityQueryExprNodeInfo currNode = currExprNodeInfos.get(currIndex);
            boolean match = false;
            for (int lastIndex = (lastExprNodeInfoSize - 1); lastIndex >= 0; lastIndex--) {
                EntityQueryExprNodeInfo lastNode = lastExprNodeInfos.get(lastIndex);
                if (currNode.getPackageName().equals(lastNode.getPackageName())
                        && currNode.getEntityName().equals(lastNode.getEntityName())) {
                    match = true;
                    lastExprNodeInfoIndex = lastIndex;
                    break;
                }
            }

            if (match) {
                currExprNodeInfoIndex = currIndex;
                break;
            }
        }

        if ((currExprNodeInfoIndex < 0) || (lastExprNodeInfoIndex < 0)) {
            return true;
        }

        String[] currFullEntityDataIdParts = currFullEntityDataId.split("::");
        if (currFullEntityDataIdParts.length != currExprNodeInfoSize) {
            return true;
        }

        String[] lastFullEntityDataIdParts = lastFullEntityDataId.split("::");
        if (lastFullEntityDataIdParts.length != lastExprNodeInfoSize) {
            return true;
        }

        String currEntityDataId = currFullEntityDataIdParts[currExprNodeInfoIndex];
        String lastEntityDataId = lastFullEntityDataIdParts[lastExprNodeInfoIndex];

        if (!currEntityDataId.equals(lastEntityDataId)) {
            return false;
        }

        return true;

    }
}
