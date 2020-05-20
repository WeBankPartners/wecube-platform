package com.webank.wecube.platform.auth.server.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class SysUser implements UserDetails {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private String username;

    private String password;

    private List<CompositeAuthority> compositeAuthorities = new ArrayList<>();

    private String authSource;

    private String authContext;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return getCompositeAuthorities();
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public List<CompositeAuthority> getCompositeAuthorities() {
        return compositeAuthorities;
    }

    public void setCompositeAuthorities(List<CompositeAuthority> compositeAuthorities) {
        this.compositeAuthorities = compositeAuthorities;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public SysUser addCompositeAuthority(CompositeAuthority compositeAuthority) {
        if (compositeAuthority == null) {
            return this;
        }

        if (contains(compositeAuthority)) {
            return this;
        }

        this.compositeAuthorities.add(compositeAuthority);
        return this;
    }

    private boolean contains(CompositeAuthority compositeAuthority) {
        for (CompositeAuthority a : this.compositeAuthorities) {
            if ((a.equals(compositeAuthority))) {
                return true;
            }
        }

        return false;
    }

    public String getAuthSource() {
        return authSource;
    }

    public void setAuthSource(String authSource) {
        this.authSource = authSource;
    }

    public String getAuthContext() {
        return authContext;
    }

    public void setAuthContext(String authContext) {
        this.authContext = authContext;
    }
}
