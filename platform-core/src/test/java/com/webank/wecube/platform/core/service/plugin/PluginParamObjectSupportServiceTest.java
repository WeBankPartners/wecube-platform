package com.webank.wecube.platform.core.service.plugin;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.wecube.platform.core.entity.plugin.CoreObjectMeta;
import com.webank.wecube.platform.core.entity.plugin.CoreObjectVar;

@RunWith(SpringRunner.class)
@SpringBootTest
public class PluginParamObjectSupportServiceTest {
    
    @Autowired
    PluginParamObjectSupportService pluginParamObjectSupportService;
    
    @Autowired
    PluginParamObjectVarCalculationService pluginParamObjectVarCalculationService;
    
    ObjectMapper objectMapper = new ObjectMapper();

    @Ignore
    @Test
    public void testFetchAssembledCoreObjectMeta() {
        String packageName = "wecmdb";
        String objectName = "k8sObjB";
        CoreObjectMeta objectMeta = pluginParamObjectSupportService.fetchAssembledCoreObjectMeta(packageName, objectName);
        
        Assert.assertNotNull(objectMeta);
    }
    
    @Ignore
    @Test
    public void testCalculateCoreObjectVar() throws IOException{
        
        String packageName = "wecmdb";
        String objectName = "k8sObjD";
        CoreObjectMeta objectMeta = pluginParamObjectSupportService.fetchAssembledCoreObjectMeta(packageName, objectName);
        CoreObjectVarCalculationContext ctx = null;
        CoreObjectVar resultVar = pluginParamObjectVarCalculationService.calculateCoreObjectVar(objectMeta, ctx);
        
        Assert.assertNotNull(resultVar);
        
        PluginParamObject paramObject = pluginParamObjectVarCalculationService.assemblePluginParamObject(resultVar, ctx);
        
        String json = objectMapper.writeValueAsString(paramObject);
        System.out.println(json);
        
        PluginParamObject paramObjectFromJson = objectMapper.readValue(json, PluginParamObject.class);
        
        
        System.out.println("===============");
        System.out.println(paramObjectFromJson);
    }
    
    @Test
    public void testCalculateCoreObjectVarWhenListProperty() throws JsonProcessingException{
        
        String packageName = "wecmdb";
        String objectName = "k8sObjC";
        CoreObjectMeta objectMeta = pluginParamObjectSupportService.fetchAssembledCoreObjectMeta(packageName, objectName);
        CoreObjectVarCalculationContext ctx = null;
        CoreObjectVar resultVar = pluginParamObjectVarCalculationService.calculateCoreObjectVar(objectMeta, ctx);
        
        Assert.assertNotNull(resultVar);
        
        PluginParamObject paramObject = pluginParamObjectVarCalculationService.assemblePluginParamObject(resultVar, ctx);
        
        String json = objectMapper.writeValueAsString(paramObject);
        
        System.out.println(json);
    }

}
