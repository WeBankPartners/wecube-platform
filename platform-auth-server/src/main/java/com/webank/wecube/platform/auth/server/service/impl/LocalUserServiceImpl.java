package com.webank.wecube.platform.auth.server.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.webank.wecube.platform.auth.server.entity.SysUserEntity;
import com.webank.wecube.platform.auth.server.model.CompositeAuthority;
import com.webank.wecube.platform.auth.server.model.SysUser;
import com.webank.wecube.platform.auth.server.repository.UserRepository;
import com.webank.wecube.platform.auth.server.service.LocalUserService;

@Service("localUserService")
public class LocalUserServiceImpl implements LocalUserService {

    @Autowired
    private UserRepository userRepo;

    @Override
    public SysUser loadUserByUsername(String username) {
        SysUserEntity userEntity = userRepo.findOneByUsername(username);
        if (userEntity == null) {
            return null;
        }

        SysUser user = new SysUser();
        user.setUsername(userEntity.getUsername());
        user.setPassword(userEntity.getPassword());

        userEntity.getRoles().forEach(r -> {
            CompositeAuthority c = new CompositeAuthority();
            c.setAuthority(r.getName());
            c.setAuthorityType("role");
            user.addCompositeAuthority(c);
        });
        return user;
    }

}
