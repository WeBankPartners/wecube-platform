package com.webank.wecube.platform.auth.server.service.impl;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.junit4.SpringRunner;

import com.webank.wecube.platform.auth.server.service.LocalUserDetailsService;

@Ignore
@RunWith(SpringRunner.class)
@SpringBootTest
public class LocalUserDetailsServiceImplTest {
    
    @Autowired
    LocalUserDetailsService localUserDetailsService;

    @Test
    public void testLoadUserByUsername() {
        String username = "umadmin";
        
        UserDetails user = localUserDetailsService.loadUserByUsername(username);
        
        Assert.assertNotNull(user);
    }

}
