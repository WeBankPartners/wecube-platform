package com.webank.wecube.platform.auth.server.model;

import org.springframework.security.core.GrantedAuthority;

public class CompositeAuthority implements GrantedAuthority {

    /**
     * 
     */
    private static final long serialVersionUID = 7622659105716440063L;
    
    private String authorityType;
    
    private String authority;

    @Override
    public String getAuthority() {
        return assembleAuthority();
    }
    
    protected String assembleAuthority(){
        return authority;
    }

    public String getAuthorityType() {
        return authorityType;
    }

    public void setAuthorityType(String authorityType) {
        this.authorityType = authorityType;
    }

    public void setAuthority(String authority) {
        this.authority = authority;
    }
    
    

}
