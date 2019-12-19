package com.webank.wecube.platform.auth.server.dto;

import java.util.ArrayList;
import java.util.List;

public class ScopedAuthoritiesDto {
    private String scope;
    private String alg;
    private List<String> authorities = new ArrayList<String>();

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getAlg() {
        return alg;
    }

    public void setAlg(String alg) {
        this.alg = alg;
    }

    public List<String> getAuthorities() {
        return authorities;
    }

    public void setAuthorities(List<String> authorities) {
        this.authorities = authorities;
    }

    public void addAuthority(String authority) {
        this.authorities.add(authority);
    }

    @Override
    public String toString() {
        return "ScopedAuthoritiesDto [scope=" + scope + ", alg=" + alg + ", authorities=" + authorities + "]";
    }
    
    
}
