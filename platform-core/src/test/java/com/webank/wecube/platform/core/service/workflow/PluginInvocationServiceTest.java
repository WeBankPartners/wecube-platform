package com.webank.wecube.platform.core.service.workflow;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.webank.wecube.platform.core.model.workflow.PluginInvocationCommand;

@RunWith(SpringRunner.class)
@SpringBootTest
public class PluginInvocationServiceTest {
    
    @Autowired
    PluginInvocationService service;

    @Test
    public void testInvokePluginInterface() {
        
        PluginInvocationCommand cmd = new PluginInvocationCommand();
        service.invokePluginInterface(cmd);
    }

}
