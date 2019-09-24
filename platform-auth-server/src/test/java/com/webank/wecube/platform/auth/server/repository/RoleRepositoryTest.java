package com.webank.wecube.platform.auth.server.repository;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;

import com.webank.wecube.platform.auth.server.entity.SysRoleEntity;
import com.webank.wecube.platform.auth.server.entity.SysUserEntity;

@Ignore
@RunWith(SpringRunner.class)
@SpringBootTest
public class RoleRepositoryTest {
    
    @Autowired
    RoleRepository roleRepo;
    
    @Autowired
    UserRepository userRepo;
    
    @Autowired
    PasswordEncoder passwordEncoder;

    @Test
    public void testSave() {
        SysRoleEntity userRole = new SysRoleEntity();
        userRole.setCreatedBy("test");
        userRole.setName("USER");
        
        userRole = roleRepo.save(userRole);
        
        SysRoleEntity adminRole =  new SysRoleEntity();
        adminRole.setName("ADMIN");
        
        adminRole = roleRepo.save(adminRole);
        
        SysUserEntity user = new SysUserEntity();
        user.setUsername("umadmin");
        user.setPassword(passwordEncoder.encode("123456"));
        
        user.getRoles().add(userRole);
        user.getRoles().add(adminRole);
        
        userRepo.save(user);
    }

}
