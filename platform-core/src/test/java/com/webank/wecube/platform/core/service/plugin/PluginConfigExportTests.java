package com.webank.wecube.platform.core.service.plugin;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class PluginConfigExportTests {
    @Autowired
    PluginConfigService service;
    
    @Ignore
    @Test
    public void testExport(){
        String packageId = "alicloud__v1.0.0";
        String xml = service.exportPluginRegistersForOnePackage(packageId);
        
        System.out.println(xml);
    }

}
