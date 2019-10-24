package com.webank.wecube.platform.core.service.workflow;

import java.io.InputStream;

import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.impl.util.IoUtil;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.webank.wecube.platform.workflow.model.ProcDefOutline;

@RunWith(SpringRunner.class)
@SpringBootTest
public class WorkflowEngineServiceTest {
    
    @Autowired
    RepositoryService repositoryService;
    
    @Autowired
    WorkflowEngineService service;
    
    String procDefId = "wecube1567416016648:2:f908bfa6-d903-11e9-bb36-b28dc93f91e2";

    @Test
    public void testGetProcDefOutline() {
        ProcessDefinition pd = service.retrieveProcessDefinition(procDefId);
        
        Assert.assertNotNull(pd);
        
        ProcDefOutline outline = service.getProcDefOutline(pd);
        
        Assert.assertNotNull(outline);
        
        System.out.println(outline);
        
        String xml = null;
        InputStream processModelIn = null;

        try {
            processModelIn = repositoryService.getProcessModel(procDefId);
            byte[] processModel = IoUtil.readInputStream(processModelIn, "processModelBpmn20Xml");
            xml = new String(processModel, "UTF-8");
        } catch (Exception e) {
            xml = null;
        } finally {
            IoUtil.closeSilently(processModelIn);
        }

        System.out.println(xml);
    }

}
