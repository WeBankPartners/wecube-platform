package com.webank.wecube.platform.auth.server.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "AUTH_SYS_SUB_SYSTEM_AUTHORITY")
public class SubSystemAuthorityRsEntity extends BaseStatusFeaturedEntity {

    @Column(name = "SUB_SYSTEM_ID")
    private String subSystemId;
    @Column(name = "SUB_SYSTEM_CODE")
    private String subSystemCode;
    @Column(name = "AUTHORITY_ID")
    private String authorityId;
    @Column(name = "AUTHORITY_CODE")
    private String authorityCode;

    public String getSubSystemId() {
        return subSystemId;
    }

    public void setSubSystemId(String subSystemId) {
        this.subSystemId = subSystemId;
    }

    public String getSubSystemCode() {
        return subSystemCode;
    }

    public void setSubSystemCode(String subSystemCode) {
        this.subSystemCode = subSystemCode;
    }

    public String getAuthorityId() {
        return authorityId;
    }

    public void setAuthorityId(String authorityId) {
        this.authorityId = authorityId;
    }

    public String getAuthorityCode() {
        return authorityCode;
    }

    public void setAuthorityCode(String authorityCode) {
        this.authorityCode = authorityCode;
    }

}
