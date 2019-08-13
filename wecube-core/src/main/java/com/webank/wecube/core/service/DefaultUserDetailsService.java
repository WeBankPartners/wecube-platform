package com.webank.wecube.core.service;

import static com.webank.wecube.core.domain.MenuItem.ROLE_PREFIX;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.webank.wecube.core.interceptor.UsernameStorage;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service("userDetailsService")
public class DefaultUserDetailsService implements UserDetailsService {

    @Autowired
    UserManagerService userManagerService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UsernameStorage.getIntance().set(username);

        List<String> menuItems = userManagerService.getMenuItemCodesByUsername(username);
        if (isNotEmpty(menuItems)) {
            List<GrantedAuthority> authorities = menuItems.stream()
                    .map(menuItem -> new SimpleGrantedAuthority(ROLE_PREFIX + menuItem))
                    .collect(toList());
            log.info("Menu permissions {} found for user {}", authorities, username);
            return new User(username, "", authorities);
        } else {
            log.warn("No accessible menu found for user {}", username);
            return new User(username, "", emptyList());
        }
    }
}
