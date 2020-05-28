package com.webank.wecube.platform.auth.server.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.webank.wecube.platform.auth.server.service.LocalUserDetailsService;
import com.webank.wecube.platform.auth.server.service.LocalUserService;

@Service("localUserDetailsService")
public class LocalUserDetailsServiceImpl implements LocalUserDetailsService {
    
    private static final Logger log = LoggerFactory.getLogger(LocalUserDetailsServiceImpl.class);
    
    @Autowired
    private LocalUserService userService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("load user details for {}", username);
        UserDetails user = userService.loadUserByUsername(username);
        if(user == null){
            throw new UsernameNotFoundException(String.format("%s does not exist", username));
        }
        return user;
    }

}
