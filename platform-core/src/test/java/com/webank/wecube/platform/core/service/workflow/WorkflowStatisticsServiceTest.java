package com.webank.wecube.platform.core.service.workflow;

import java.util.ArrayList;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.webank.wecube.platform.core.dto.plugin.PageableDto;
import com.webank.wecube.platform.core.dto.plugin.QueryResponse;
import com.webank.wecube.platform.core.dto.workflow.WorkflowExecutionReportItemDto;
import com.webank.wecube.platform.core.dto.workflow.WorkflowExecutionReportQueryDto;

@RunWith(SpringRunner.class)
@SpringBootTest
public class WorkflowStatisticsServiceTest {
    
    @Autowired
    WorkflowStatisticsService service;

    @Ignore
    @Test
    public void testDoFetchPageableProcExecBindingTasknodeStatistics() {
        
        WorkflowExecutionReportQueryDto queryDto = new WorkflowExecutionReportQueryDto();
        PageableDto pageable = new PageableDto();
        pageable.setStartIndex(0);
        pageable.setPageSize(10);
        
        queryDto.setPageable(pageable);
        
        List<String> taskNodeIds = new ArrayList<>();
        taskNodeIds.add("sCya6axe2EqH");
        taskNodeIds.add("sCx5br6B3mIM");
        taskNodeIds.add("sCx5dtuB3n3D");
        
        queryDto.setTaskNodeIds(taskNodeIds);
        
        String startDate = "20210906";
        queryDto.setStartDate(startDate);
        
        QueryResponse<WorkflowExecutionReportItemDto> queryResponseDto  = service.fetchWorkflowExecutionTasknodeReports(queryDto);
        System.out.println(queryResponseDto.getPageInfo().getTotalRows());
        System.out.println(queryResponseDto.getContents().size());
        System.out.println(queryResponseDto.getContents());
    }

}
