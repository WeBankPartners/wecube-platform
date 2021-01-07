package com.webank.wecube.platform.core.service.plugin;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.webank.wecube.platform.core.dto.plugin.DataModelEntityDto;

@RunWith(SpringRunner.class)
@SpringBootTest
public class PluginPackageDataModelServiceTest {
    
    @Autowired
    PluginPackageDataModelService service;

    @Ignore
    @Test
    public void testGetEntityByPackageNameAndName() {
        String packageName = "wecmdb";
        String entityName = "data_center";
        DataModelEntityDto entityDto = service.getEntityByPackageNameAndName(packageName, entityName);
        
        System.out.println(entityDto);
    }

}
