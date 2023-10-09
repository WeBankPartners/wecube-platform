package com.webank.wecube.platform.core.service.workflow;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.webank.wecube.platform.core.dto.plugin.QueryResponse;
import com.webank.wecube.platform.core.dto.workflow.ProcInstInfoDto;
import com.webank.wecube.platform.core.dto.workflow.WorkflowInstQueryDto;
import com.webank.wecube.platform.core.entity.workflow.ProcInstInfoQueryEntity;
import com.webank.wecube.platform.core.repository.workflow.ProcInstInfoMapper;

@Ignore
@RunWith(SpringRunner.class)
@SpringBootTest
public class WorkflowProcInstServiceTest {

    @Autowired
    WorkflowProcInstService service;

    @Autowired
    ProcInstInfoMapper mapper;

    @Test
    public void testGetPageableProcessInstances() {

        WorkflowInstQueryDto queryDto = new WorkflowInstQueryDto();
        QueryResponse<ProcInstInfoDto> resp = service.getPageableProcessInstances(queryDto);
    }
    
    @Test
    public void testPageQuery(){
        List<String> roleNames = new ArrayList<>();
        roleNames.add("ADMIN");
        WorkflowInstQueryDto queryDto = new WorkflowInstQueryDto();
        queryDto.setEndTime("2023-01-01 12:12:11");
        queryDto.setStartTime("2023-01-01 12:12:11");
        queryDto.setOperator("111");
        queryDto.setProcInstName("aaa");
        queryDto.setStatus("IN");
        queryDto.setEntityDisplayName("jjj");
        
        
        com.github.pagehelper.PageInfo<ProcInstInfoQueryEntity> ret = service.queryPageableProcInstInfoByRoleNames(roleNames, queryDto);
        
        System.out.println(ret.getPageNum());
    }

    @Test
    public void testQuery() {
        List<String> roleNames = new ArrayList<>();
        roleNames.add("ADMIN");
        Date startTime = new Date();
        Date endTime = new Date();
        String entityDataName = "111";
        String procInstName = "111";
        String operator = "aa";
        String status = "IN";
        Integer id = 123;
        List<ProcInstInfoQueryEntity> entities = mapper.selectAllProcInstInfoByCriteria(roleNames, startTime, endTime,
                entityDataName, procInstName, operator, status, id);

    }

}
