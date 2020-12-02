package com.webank.wecube.platform.core.service.plugin;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.webank.wecube.platform.core.dto.PageableDto;
import com.webank.wecube.platform.core.dto.QueryRequestDto;
import com.webank.wecube.platform.core.dto.QueryResponse;
import com.webank.wecube.platform.core.dto.SystemVariableDto;

@Ignore
@RunWith(SpringRunner.class)
@SpringBootTest
public class SystemVariableServiceTest {
    
    @Autowired
    SystemVariableService service;

    @Test
    public void testRetrieveSystemVariables() {
        QueryRequestDto queryRequest = new QueryRequestDto();
        
        QueryResponse<SystemVariableDto> result = service.retrieveSystemVariables(queryRequest);
        
        Assert.assertEquals(436, result.getContents().size());
        Assert.assertEquals(10000,result.getPageInfo().getPageSize());
        Assert.assertEquals(0,result.getPageInfo().getStartIndex());
        Assert.assertEquals(436,result.getPageInfo().getTotalRows());
    }
    
    @Test
    public void testRetrieveSystemVariablesWithPageable() {
        QueryRequestDto queryRequest = new QueryRequestDto();
        PageableDto pageableDto = new PageableDto();
        pageableDto.setPageSize(5);
        pageableDto.setStartIndex(0);
        
        queryRequest.setPageable(pageableDto);
        queryRequest.setPaging(true);
        
        QueryResponse<SystemVariableDto> result = service.retrieveSystemVariables(queryRequest);
        
        Assert.assertEquals(5, result.getContents().size());
        Assert.assertEquals(5,result.getPageInfo().getPageSize());
        Assert.assertEquals(0,result.getPageInfo().getStartIndex());
        Assert.assertEquals(436,result.getPageInfo().getTotalRows());
    }
    
    @Test
    public void testRetrieveSystemVariablesWithFilters() {
        QueryRequestDto queryRequest = new QueryRequestDto();
        queryRequest.addEqualsFilter("defaultValue", "egress");
        
        QueryResponse<SystemVariableDto> result = service.retrieveSystemVariables(queryRequest);
        
        Assert.assertEquals(2, result.getContents().size());
        Assert.assertEquals(10000,result.getPageInfo().getPageSize());
        Assert.assertEquals(0,result.getPageInfo().getStartIndex());
        Assert.assertEquals(2,result.getPageInfo().getTotalRows());
    }

}
