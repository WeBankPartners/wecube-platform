package com.webank.wecube.platform.core.domain;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;

import com.webank.wecube.platform.core.support.DomainIdBuilder;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;

@Entity
@Table(name = "resource_server")
public class ResourceServerDomain {
    @Id
    private String id;

    @NotBlank
    @Column(name = "name")
    private String name;

    @NotBlank
    @Column(name = "host")
    private String host;

    @NotBlank
    @Column(name = "port")
    private String port;

    @NotBlank
    @Column(name = "login_username")
    private String loginUsername;

    @NotBlank
    @Column(name = "login_password")
    private String loginPassword;

    @NotBlank
    @Column(name = "type")
    private String type;

    @Column(name = "is_allocated")
    private Integer isAllocated;

    @Column(name = "purpose")
    private String purpose;

    @Column(name = "status")
    private String status;

    @OneToMany(mappedBy = "resourceServer", fetch = FetchType.EAGER)
    private List<ResourceItem> resourceItems = new ArrayList<>();

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "created_date")
    private Timestamp createdDate;

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "updated_date")
    private Timestamp updatedDate;

    @PrePersist
    public void initId() {
        this.id = DomainIdBuilder.buildDomainId(this);
    }

    public ResourceServerDomain() {
    }

    public ResourceServerDomain(String id, @NotBlank String name, @NotBlank String host, @NotBlank String port,
            @NotBlank String loginUsername, @NotBlank String loginPassword, @NotBlank String type, Integer isAllocated,
            @NotBlank String purpose, String status, List<ResourceItem> resourceItems, String createdBy,
            Timestamp createdDate, String updatedBy, Timestamp updatedDate) {
        this.id = id;
        this.name = name;
        this.host = host;
        this.port = port;
        this.loginUsername = loginUsername;
        this.loginPassword = loginPassword;
        this.type = type;
        this.isAllocated = isAllocated;
        this.purpose = purpose;
        this.status = status;
        this.resourceItems = resourceItems;
        this.createdBy = createdBy;
        this.createdDate = createdDate;
        this.updatedBy = updatedBy;
        this.updatedDate = updatedDate;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getLoginUsername() {
        return loginUsername;
    }

    public void setLoginUsername(String loginUsername) {
        this.loginUsername = loginUsername;
    }

    public String getLoginPassword() {
        return loginPassword;
    }

    public void setLoginPassword(String loginPassword) {
        this.loginPassword = loginPassword;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getIsAllocated() {
        return isAllocated;
    }

    public void setIsAllocated(Integer isAllocated) {
        this.isAllocated = isAllocated;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<ResourceItem> getResourceItems() {
        return resourceItems;
    }

    public void setResourceItems(List<ResourceItem> resourceItems) {
        this.resourceItems = resourceItems;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Timestamp getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Timestamp createdDate) {
        this.createdDate = createdDate;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Timestamp getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(Timestamp updatedDate) {
        this.updatedDate = updatedDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ResourceServerDomain that = (ResourceServerDomain) o;
        return getId().equals(that.getId()) && getName().equals(that.getName()) && getHost().equals(that.getHost())
                && getPort().equals(that.getPort()) && getLoginUsername().equals(that.getLoginUsername())
                && getLoginPassword().equals(that.getLoginPassword()) && getType().equals(that.getType())
                && Objects.equals(getIsAllocated(), that.getIsAllocated()) && getPurpose().equals(that.getPurpose())
                && Objects.equals(getStatus(), that.getStatus()) && Objects.equals(getCreatedBy(), that.getCreatedBy())
                && Objects.equals(getCreatedDate(), that.getCreatedDate())
                && Objects.equals(getUpdatedBy(), that.getUpdatedBy())
                && Objects.equals(getUpdatedDate(), that.getUpdatedDate());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName(), getHost(), getPort(), getLoginUsername(), getLoginPassword(), getType(),
                getIsAllocated(), getPurpose(), getStatus(), getCreatedBy(), getCreatedDate(), getUpdatedBy(),
                getUpdatedDate());
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toStringExclude(this, new String[] { "resourceItems" });
    }
}
