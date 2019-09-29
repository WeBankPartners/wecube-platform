package com.webank.wecube.platform.auth.server.repository;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.webank.wecube.platform.auth.server.entity.SysRoleEntity;
import com.webank.wecube.platform.auth.server.entity.SysSubSystemEntity;
import com.webank.wecube.platform.auth.server.entity.SysUserEntity;

@Ignore
@ActiveProfiles({"test"})
@RunWith(SpringRunner.class)
@SpringBootTest
public class RoleRepositoryTest {
    
    @Autowired
    RoleRepository roleRepo;
    
    @Autowired
    UserRepository userRepo;
    
    @Autowired
    PasswordEncoder passwordEncoder;
    
    @Autowired
    SubSystemRepository subSystemRepo;
    
    @Test
    public void testAddSubSystem(){
        SysSubSystemEntity entity = new SysSubSystemEntity();
        entity.setSystemCode("WECUBE-CORE");
        entity.setName("wecube core");
        
        
        subSystemRepo.save(entity);
    }

    @Test
    public void testSaveUserWithUserAndAdminRoles() {
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
    
    @Test
    public void testSaveUserWithUserRoles() {
        SysRoleEntity userRole = new SysRoleEntity();
        userRole.setCreatedBy("test");
        userRole.setName("USER");
        
        userRole = roleRepo.save(userRole);
        
//        SysRoleEntity adminRole =  new SysRoleEntity();
//        adminRole.setName("ADMIN");
//        
//        adminRole = roleRepo.save(adminRole);
        
        SysUserEntity user = new SysUserEntity();
        user.setUsername("umuser");
        user.setPassword(passwordEncoder.encode("123456"));
        
        user.getRoles().add(userRole);
//        user.getRoles().add(adminRole);
        
        userRepo.save(user);
    }
    
    @Test
    public void testFindUser() {
    	SysUserEntity user = userRepo.findOneByUsername("umuser");
    	System.out.println("USER:" + user);
    	Assert.assertNotNull(user);
    }

}
