//package com.webank.wecube.platform.core.domain;
//
//import java.io.IOException;
//import java.sql.Timestamp;
//import java.util.HashMap;
//import java.util.Map;
//
//import javax.persistence.Column;
//import javax.persistence.Entity;
//import javax.persistence.Id;
//import javax.persistence.JoinColumn;
//import javax.persistence.ManyToOne;
//import javax.persistence.PrePersist;
//import javax.persistence.Table;
//
//import org.apache.commons.lang.builder.ReflectionToStringBuilder;
//
//import com.webank.wecube.platform.core.commons.WecubeCoreException;
//import com.webank.wecube.platform.core.support.DomainIdBuilder;
//import com.webank.wecube.platform.core.utils.JsonUtils;
//
//@Entity
//@Table(name = "resource_item")
//public class ResourceItem {
//    @Id
//    private String id;
//
//    @Column(name = "name")
//    private String name;
//
//    @Column(name = "type")
//    private String type;
//
//    @Column(name = "additional_properties")
//    private String additionalProperties;
//
//    @Column(name = "resource_server_id")
//    private String resourceServerId;
//
//    @ManyToOne
//    @JoinColumn(name = "resource_server_id", insertable = false, updatable = false)
//    private ResourceServerDomain resourceServer;
//
//    @Column(name = "is_allocated")
//    private Integer isAllocated;
//
//    @Column(name = "purpose")
//    private String purpose;
//
//    @Column(name = "status")
//    private String status;
//
//    @Column(name = "created_by")
//    private String createdBy;
//
//    @Column(name = "created_date")
//    private Timestamp createdDate;
//
//    @Column(name = "updated_by")
//    private String updatedBy;
//
//    @Column(name = "updated_date")
//    private Timestamp updatedDate;
//
//    @PrePersist
//    public void initId() {
//        this.id = DomainIdBuilder.buildDomainId(this);
//    }
//
//    public Map<String, String> getAdditionalPropertiesMap() {
//        if (additionalProperties != null) {
//            return convertToMap(additionalProperties);
//        }
//        return new HashMap<String, String>();
//    }
//
//    @SuppressWarnings("unchecked")
//    private Map<String, String> convertToMap(String additionalProperties) {
//        try {
//            return JsonUtils.toObject(additionalProperties, Map.class);
//        } catch (IOException e) {
//            throw new WecubeCoreException(String.format("Failed to parse resource_item.additional_properties [%s] : Invalid json format.", additionalProperties), e);
//        }
//    }
//
//    public ResourceItem() {
//    }
//
//    public ResourceItem(String id, String name, String type, String additionalProperties, String resourceServerId, ResourceServerDomain resourceServer, Integer isAllocated, String purpose, String status, String createdBy, Timestamp createdDate, String updatedBy, Timestamp updatedDate) {
//        this.id = id;
//        this.name = name;
//        this.type = type;
//        this.additionalProperties = additionalProperties;
//        this.resourceServerId = resourceServerId;
//        this.resourceServer = resourceServer;
//        this.isAllocated = isAllocated;
//        this.purpose = purpose;
//        this.status = status;
//        this.createdBy = createdBy;
//        this.createdDate = createdDate;
//        this.updatedBy = updatedBy;
//        this.updatedDate = updatedDate;
//    }
//
//    public String getId() {
//        return id;
//    }
//
//    public void setId(String id) {
//        this.id = id;
//    }
//
//    public String getName() {
//        return name;
//    }
//
//    public void setName(String name) {
//        this.name = name;
//    }
//
//    public String getType() {
//        return type;
//    }
//
//    public void setType(String type) {
//        this.type = type;
//    }
//
//    public String getAdditionalProperties() {
//        return additionalProperties;
//    }
//
//    public void setAdditionalProperties(String additionalProperties) {
//        this.additionalProperties = additionalProperties;
//    }
//
//    public String getResourceServerId() {
//        return resourceServerId;
//    }
//
//    public void setResourceServerId(String resourceServerId) {
//        this.resourceServerId = resourceServerId;
//    }
//
//    public ResourceServerDomain getResourceServer() {
//        return resourceServer;
//    }
//
//    public void setResourceServer(ResourceServerDomain resourceServer) {
//        this.resourceServer = resourceServer;
//    }
//
//    public Integer getIsAllocated() {
//        return isAllocated;
//    }
//
//    public void setIsAllocated(Integer isAllocated) {
//        this.isAllocated = isAllocated;
//    }
//
//    public String getPurpose() {
//        return purpose;
//    }
//
//    public void setPurpose(String purpose) {
//        this.purpose = purpose;
//    }
//
//    public String getStatus() {
//        return status;
//    }
//
//    public void setStatus(String status) {
//        this.status = status;
//    }
//
//    public String getCreatedBy() {
//        return createdBy;
//    }
//
//    public void setCreatedBy(String createdBy) {
//        this.createdBy = createdBy;
//    }
//
//    public Timestamp getCreatedDate() {
//        return createdDate;
//    }
//
//    public void setCreatedDate(Timestamp createdDate) {
//        this.createdDate = createdDate;
//    }
//
//    public String getUpdatedBy() {
//        return updatedBy;
//    }
//
//    public void setUpdatedBy(String updatedBy) {
//        this.updatedBy = updatedBy;
//    }
//
//    public Timestamp getUpdatedDate() {
//        return updatedDate;
//    }
//
//    public void setUpdatedDate(Timestamp updatedDate) {
//        this.updatedDate = updatedDate;
//    }
//
//
//    @Override
//    public String toString() {
//        return ReflectionToStringBuilder.toStringExclude(this, new String[] {"resourceServer"});
//    }
//}
