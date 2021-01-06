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
    
    String token = "Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ1bWFkbWluIiwiaWF0IjoxNjA4Mjc1NzM1LCJ0eXBlIjoiYWNjZXNzVG9rZW4iLCJjbGllbnRUeXBlIjoiVVNFUiIsImV4cCI6MTYwODI3NjkzNSwiYXV0aG9yaXR5IjoiW1NVUEVSX0FETUlOLElNUExFTUVOVEFUSU9OX1dPUktGTE9XX0VYRUNVVElPTixJTVBMRU1FTlRBVElPTl9CQVRDSF9FWEVDVVRJT04sSU1QTEVNRU5UQVRJT05fQVJUSUZBQ1RfTUFOQUdFTUVOVCxNT05JVE9SX01BSU5fREFTSEJPQVJELE1PTklUT1JfTUVUUklDX0NPTkZJRyxNT05JVE9SX0NVU1RPTV9EQVNIQk9BUkQsTU9OSVRPUl9BTEFSTV9DT05GSUcsTU9OSVRPUl9BTEFSTV9NQU5BR0VNRU5ULENPTExBQk9SQVRJT05fUExVR0lOX01BTkFHRU1FTlQsQ09MTEFCT1JBVElPTl9XT1JLRkxPV19PUkNIRVNUUkFUSU9OLEFETUlOX1NZU1RFTV9QQVJBTVMsQURNSU5fUkVTT1VSQ0VTX01BTkFHRU1FTlQsQURNSU5fVVNFUl9ST0xFX01BTkFHRU1FTlQsQURNSU5fQ01EQl9NT0RFTF9NQU5BR0VNRU5ULENNREJfQURNSU5fQkFTRV9EQVRBX01BTkFHRU1FTlQsQURNSU5fUVVFUllfTE9HLE1FTlVfQURNSU5fUEVSTUlTU0lPTl9NQU5BR0VNRU5ULE1FTlVfREVTSUdOSU5HX0NJX0RBVEFfRU5RVUlSWSxNRU5VX0RFU0lHTklOR19DSV9JTlRFR1JBVEVEX1FVRVJZX0VYRUNVVElPTixNRU5VX0RFU0lHTklOR19DSV9EQVRBX01BTkFHRU1FTlQsTUVOVV9ERVNJR05JTkdfQ0lfSU5URUdSQVRFRF9RVUVSWV9NQU5BR0VNRU5ULE1FTlVfSURDX1BMQU5OSU5HX0RFU0lHTixNRU5VX0lEQ19SRVNPVVJDRV9QTEFOTklORyxNRU5VX0NNREJfQURNSU5fQkFTRV9EQVRBX01BTkFHRU1FTlQsTUVOVV9BRE1JTl9RVUVSWV9MT0csTUVOVV9BUFBMSUNBVElPTl9BUkNISVRFQ1RVUkVfREVTSUdOLE1FTlVfQVBQTElDQVRJT05fQVJDSElURUNUVVJFX1FVRVJZLE1FTlVfQVBQTElDQVRJT05fREVQTE9ZTUVOVF9ERVNJR04sTUVOVV9BRE1JTl9DTURCX01PREVMX01BTkFHRU1FTlQsSk9CU19UQVNLX01BTkFHRU1FTlQsSk9CU19TRVJWSUNFX0NBVEFMT0dfTUFOQUdFTUVOVCxKT0JTX1RFTVBMQVRFX0dST1VQX01BTkFHRU1FTlQsSk9CU19URU1QTEFURV9NQU5BR0VNRU5UXSJ9.znDjJru90TJB8rk8e--GDz2O5XO669PjNrUcdwz6Z9n0gEFCfT_f4CN_HJUqxpohoD9v0blRNzqGN4IrYxqsew";
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
