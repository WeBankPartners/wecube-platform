package com.webank.wecube.platform.core.service.workflow;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.webank.wecube.platform.core.dto.plugin.QueryResponse;
import com.webank.wecube.platform.core.dto.plugin.SortingDto;
import com.webank.wecube.platform.core.dto.workflow.WorkflowExecutionOverviewDto;
import com.webank.wecube.platform.core.dto.workflow.WorkflowExecutionOverviewsQueryDto;
import com.webank.wecube.platform.core.dto.workflow.WorkflowExecutionReportItemDto;
import com.webank.wecube.platform.core.dto.workflow.WorkflowExecutionReportQueryDto;

@Ignore
@RunWith(SpringRunner.class)
@SpringBootTest
public class WorkflowStatisticsServiceTest {

    @Autowired
    WorkflowStatisticsService service;

    @Test
    public void testFetchWorkflowExecutionTasknodeReports() {

        WorkflowExecutionReportQueryDto queryDto = new WorkflowExecutionReportQueryDto();
        List<String> taskNodeIds = new ArrayList<>();

        taskNodeIds.add("sDdmTr9U3DB3");
        taskNodeIds.add("sGpesYw12BUn");

        queryDto.setTaskNodeIds(taskNodeIds);
        
        SortingDto sort = new SortingDto();
        sort.setField("successCount");
        sort.setAsc(true);
        
        queryDto.setSorting(sort);

        QueryResponse<WorkflowExecutionReportItemDto> respResult = service
                .fetchWorkflowExecutionTasknodeReports(queryDto);
        Assert.assertNotNull(respResult);

        List<WorkflowExecutionReportItemDto> contents = respResult.getContents();

        Assert.assertEquals(5, contents.size());

        Assert.assertEquals(0, contents.get(0).getFailureCount());

        Assert.assertEquals(1, contents.get(0).getSuccessCount());

    }

    @Test
    public void testFetchWorkflowExecutionPluginReports() {
        WorkflowExecutionReportQueryDto queryDto = new WorkflowExecutionReportQueryDto();
        List<String> serviceIds = new ArrayList<>();

        serviceIds.add("terraform/az(aliyun)/apply");

        queryDto.setServiceIds(serviceIds);

        QueryResponse<WorkflowExecutionReportItemDto> respResult = service
                .fetchWorkflowExecutionPluginReports(queryDto);
        Assert.assertNotNull(respResult);

        List<WorkflowExecutionReportItemDto> contents = respResult.getContents();

        Assert.assertEquals(3, contents.size());

        Assert.assertEquals(0, contents.get(0).getFailureCount());

        Assert.assertEquals(1, contents.get(0).getSuccessCount());
    }

    @Test
    public void testFetchWorkflowExecutionOverviews() {
        WorkflowExecutionOverviewsQueryDto queryDto = new WorkflowExecutionOverviewsQueryDto();
        List<WorkflowExecutionOverviewDto> contents = service.fetchWorkflowExecutionOverviews(queryDto);
        Assert.assertEquals(14, contents.size());
        
        WorkflowExecutionOverviewDto procDef = null;
        for(WorkflowExecutionOverviewDto c : contents) {
            if("sGpesY012BPZ".equals(c.getProcDefId())) {
                procDef = c;
                break;
            }
        }
        
        Assert.assertNotNull(procDef);
        Assert.assertEquals(15, procDef.getTotalInstances());
        Assert.assertEquals(0, procDef.getTotalInProgressInstances());
        Assert.assertEquals(15, procDef.getTotalCompletedInstances());
        Assert.assertEquals(0, procDef.getTotalFaultedInstances());
        
        contents.forEach(System.out::println);
    }

}
