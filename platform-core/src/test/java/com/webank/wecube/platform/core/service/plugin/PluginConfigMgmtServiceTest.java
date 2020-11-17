package com.webank.wecube.platform.core.service.plugin;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.webank.wecube.platform.core.commons.AuthenticationContextHolder;
import com.webank.wecube.platform.core.commons.AuthenticationContextHolder.AuthenticatedUser;
import com.webank.wecube.platform.core.dto.plugin.PluginConfigInterfaceDto;

@RunWith(SpringRunner.class)
@SpringBootTest
public class PluginConfigMgmtServiceTest {

    @Autowired
    PluginConfigMgmtService service;
    
    @Before
    public void setUp(){
        List<String> roleNames = new ArrayList<>();
        roleNames.add("SUPER_ADMIN");
        AuthenticatedUser user = new AuthenticatedUser("test", "test-token", roleNames);
        AuthenticationContextHolder.setAuthenticatedUser(user);
    }

    @Test
    public void testQueryAllEnabledPluginConfigInterfaceForEntity() {

        String targetPackage = "wecmdb";
        String targetEntity = "data_center";

        List<PluginConfigInterfaceDto> resultDtos = service.queryAllEnabledPluginConfigInterfaceForEntity(targetPackage,
                targetEntity, null);
        
        System.out.println(resultDtos.size());
        System.out.println(resultDtos);
    }

}
