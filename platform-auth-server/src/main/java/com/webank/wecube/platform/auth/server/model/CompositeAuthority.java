package com.webank.wecube.platform.auth.server.model;

import org.springframework.security.core.GrantedAuthority;

public class CompositeAuthority implements GrantedAuthority {

    /**
     * 
     */
    private static final long serialVersionUID = 7622659105716440063L;
    
    public static final String AUTHORITY_TYPE_ROLE = "ROLE";
    public static final String AUTHORITY_TYPE_PERMISSION = "AUTHORITY";
    
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((authority == null) ? 0 : authority.hashCode());
        result = prime * result + ((authorityType == null) ? 0 : authorityType.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CompositeAuthority other = (CompositeAuthority) obj;
        if (authority == null) {
            if (other.authority != null)
                return false;
        } else if (!authority.equals(other.authority))
            return false;
        if (authorityType == null) {
            if (other.authorityType != null)
                return false;
        } else if (!authorityType.equals(other.authorityType))
            return false;
        return true;
    }
    
    

}
