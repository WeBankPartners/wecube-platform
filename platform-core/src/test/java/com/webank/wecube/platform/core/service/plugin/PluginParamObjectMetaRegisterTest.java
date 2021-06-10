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
public class PluginParamObjectMetaRegisterTest {

    @Autowired
    PluginParamObjectMetaRegister pluginParamObjectSupportService;

    @Autowired
    PluginParamObjectVarCalculator pluginParamObjectVarCalculationService;

    @Autowired
    PluginParamObjectVarMarshaller pluginParamObjectVarAssembleService;

    @Autowired
    PluginParamObjectMetaStorage pluginParamObjectMetaStorage;

    @Autowired
    PluginParamObjectVarStorage pluginParamObjectVarStorage;

    ObjectMapper objectMapper = new ObjectMapper();

    @Ignore
    @Test
    public void testFetchAssembledCoreObjectMeta() {
        String packageName = "wecmdb";
        String objectName = "k8sObjB";
        String configId = "1623308026320";
        CoreObjectMeta objectMeta = pluginParamObjectMetaStorage.fetchAssembledCoreObjectMeta(packageName, objectName, configId);

        Assert.assertNotNull(objectMeta);
    }

    @Ignore
    @Test
    public void testCalculateCoreObjectVar() throws IOException {

        String packageName = "wecmdb";
        String objectName = "k8sObjD";
        String configId = "1";
        CoreObjectMeta objectMeta = pluginParamObjectMetaStorage.fetchAssembledCoreObjectMeta(packageName, objectName, configId);
        Assert.assertNotNull(objectMeta);
        CoreObjectVarCalculationContext ctx = new CoreObjectVarCalculationContext();
        ctx.setExternalCacheMap(null);
        ctx.setRootEntityDataId("0003_11110000");
        CoreObjectVar resultVar = pluginParamObjectVarCalculationService.calculateCoreObjectVar(objectMeta, ctx);

        Assert.assertNotNull(resultVar);

        PluginParamObject paramObject = pluginParamObjectVarAssembleService.marshalPluginParamObject(resultVar, ctx);

        String json = objectMapper.writeValueAsString(paramObject);
        System.out.println(json);

        PluginParamObject paramObjectFromJson = objectMapper.readValue(json, PluginParamObject.class);

        System.out.println("===============");
        System.out.println(paramObjectFromJson);

        System.out.println("============ unmarshal=====");
        CoreObjectVar objectVar = pluginParamObjectVarAssembleService.unmarshalPluginParamObject(paramObjectFromJson,
                objectMeta, ctx);

        PluginParamObject paramObject1 = pluginParamObjectVarAssembleService.marshalPluginParamObject(objectVar, ctx);
        json = objectMapper.writeValueAsString(paramObject1);
        System.out.println(json);

        System.out.println("=========  fetch =========");
        CoreObjectVar storedObjectVar = pluginParamObjectVarStorage.fetchCoreObjectVar(resultVar.getId());
        PluginParamObject paramObject2 = pluginParamObjectVarAssembleService.marshalPluginParamObject(storedObjectVar,
                ctx);
        json = objectMapper.writeValueAsString(paramObject2);
        System.out.println(json);

    }

    @Ignore
    @Test
    public void testCalculateCoreObjectVarWhenListProperty() throws JsonProcessingException {

        String packageName = "wecmdb";
        String objectName = "k8sObjC";
        String configId = "1";
        CoreObjectMeta objectMeta = pluginParamObjectMetaStorage.fetchAssembledCoreObjectMeta(packageName, objectName, configId);
        CoreObjectVarCalculationContext ctx = null;
        CoreObjectVar resultVar = pluginParamObjectVarCalculationService.calculateCoreObjectVar(objectMeta, ctx);

        Assert.assertNotNull(resultVar);

        PluginParamObject paramObject = pluginParamObjectVarAssembleService.marshalPluginParamObject(resultVar, ctx);

        String json = objectMapper.writeValueAsString(paramObject);

        System.out.println(json);
    }

}
