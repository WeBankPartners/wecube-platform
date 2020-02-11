package com.webank.wecube.platform.core.support.authserver;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author howechen
 */
public class AsRoleAuthoritiesDto implements Serializable {


    private static final long serialVersionUID = 7055553276940588424L;
    private String roleId;
    private String roleName;

    private List<AsAuthorityDto> authorities = new ArrayList<>();

    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public List<AsAuthorityDto> getAuthorities() {
        return authorities;
    }

    public void setAuthorities(List<AsAuthorityDto> authorities) {
        this.authorities = authorities;
    }
}
