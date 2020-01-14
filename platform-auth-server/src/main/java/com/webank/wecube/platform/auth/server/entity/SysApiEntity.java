package com.webank.wecube.platform.auth.server.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "AUTH_SYS_API")
public class SysApiEntity extends BaseUUIDFeaturedEntity {

    @Column(name = "NAME")
    private String name;

    @Column(name = "API_URL")
    private String apiUrl;

    @Column(name = "HTTP_METHOD")
    private String httpMethod;

    @Column(name = "SYSTEM_ID")
    private Long systemId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getApiUrl() {
        return apiUrl;
    }

    public void setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
    }

    public Long getSystemId() {
        return systemId;
    }

    public void setSystemId(Long systemId) {
        this.systemId = systemId;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }
}
