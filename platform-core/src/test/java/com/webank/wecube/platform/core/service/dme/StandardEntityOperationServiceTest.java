package com.webank.wecube.platform.core.service.dme;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.webank.wecube.platform.core.commons.AuthenticationContextHolder;
import com.webank.wecube.platform.core.commons.AuthenticationContextHolder.AuthenticatedUser;
import com.webank.wecube.platform.core.http.UserJwtSsoTokenRestTemplate;

public class StandardEntityOperationServiceTest {
    
    UserJwtSsoTokenRestTemplate restTemplate = new UserJwtSsoTokenRestTemplate();
    
    ***REMOVED***
    String username = "umadmin";
    
    @Before
    public void setUp(){
        AuthenticatedUser u = new AuthenticatedUser(username, token);
        AuthenticationContextHolder.setAuthenticatedUser(u);
    }
    

    @Ignore
    @Test
    public void testCreate() {
        StandardEntityOperationRestClient client = new StandardEntityOperationRestClient(restTemplate);
        
        EntityRouteDescription entityDef = new EntityRouteDescription();
        entityDef.setPackageName("wecmdb");
        entityDef.setEntityName("app_instance");
        entityDef.setHttpHost("106.52.160.142");
        entityDef.setHttpPort("29110");
        entityDef.setHttpScheme("http");
        
        Map<String,Object> dataMap = new HashMap<>();
        dataMap.put("code", "aaa");
        dataMap.put("cpu", "1");
        dataMap.put("deploy_user", "umadmin");
        dataMap.put("app_log_files", "aaaa");
        dataMap.put("deploy_package_url", "aaaa");
        dataMap.put("deploy_script", "aaaa");
        dataMap.put("deploy_user", "umadmin");
        dataMap.put("deploy_user_password", "aaaa");
        dataMap.put("name", "aaaa");
        dataMap.put("port", "10088");
        dataMap.put("key_name", "aaaa");
        dataMap.put("monitor_key_name", "aaaa");
        dataMap.put("variable_values", "aaa");
        dataMap.put("stop_script", "aaaa");
        dataMap.put("memory", "16");
        dataMap.put("description", "aaa");
        dataMap.put("monitor_port", "10089");
        dataMap.put("storage", "120");
        dataMap.put("unit", "0048_0000000025");
        
        
        List<EntityDataRecord> recordsToCreate = convert(dataMap);
        
        StandardEntityOperationResponseDto responseDto = client.create(entityDef, recordsToCreate);
        
        System.out.println(responseDto);
        
        if (StandardEntityOperationResponseDto.STATUS_OK.equalsIgnoreCase(responseDto.getStatus())) {
            
            
        } else {
            Assert.fail(responseDto.getStatus());
        }
        
    }
    
    private List<EntityDataRecord> convert(Map<String,Object> dataMap){
        EntityDataRecord newEntityDataRecord = new EntityDataRecord();
        for(Map.Entry<String, Object> attrEntry : dataMap.entrySet()){
            EntityDataAttr attr = new EntityDataAttr();
            attr.setAttrName(attrEntry.getKey());
            attr.setAttrValue(attrEntry.getValue());
            
            newEntityDataRecord.addAttrs(attr);
        }
        
        List<EntityDataRecord> recordsToCreate = new ArrayList<>();
        recordsToCreate.add(newEntityDataRecord);
        
        return recordsToCreate;
    }

}
