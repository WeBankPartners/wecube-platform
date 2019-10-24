package com.webank.wecube.platform.core.service.workflow;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.wecube.platform.core.dto.workflow.ProcDefOutlineDto;

@RunWith(SpringRunner.class)
@SpringBootTest
public class WorkflowProcessDefinitionServiceTest {
    
    ObjectMapper objectMapper = new ObjectMapper();
    
    @Autowired
    WorkflowProcessDefinitionService service;

    @Test
    public void testGetProcessDefinitionOutline() throws JsonProcessingException {
//        fail("Not yet implemented");
        String id = "rFO29eUa2Bi";
        ProcDefOutlineDto outline = service.getProcessDefinitionOutline(id);
        
        System.out.println("==========");
        System.out.println(objectMapper.writeValueAsString(outline));
    }

    @Test
    public void testGetProcessDefinition() {
//        fail("Not yet implemented");
    }

    @Test
    public void testGetProcessDefinitions() {
//        fail("Not yet implemented");
    }

    @Test
    public void testDraftProcessDefinition() {
//        fail("Not yet implemented");
    }

    @Test
    public void testDeployProcessDefinition() {
//        fail("Not yet implemented");
    }

}
