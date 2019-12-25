package com.webank.wecube.platform.core.entity.workflow;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

@Entity
@Table(name = "CORE_RE_PROC_DEF_INFO")
public class ProcDefInfoEntity extends BaseStatusFeaturedEntity {

    public static final String DRAFT_STATUS = "draft";
    public static final String DEPLOYED_STATUS = "deployed";
    public static final String DELETED_STATUS = "deleted";
    public static final String TEMPLATE_STATUS = "template";
    public static final String PREDEPLOY_STATUS = "predeploy";

    public static final String PROC_DATA_FORMAT_XML = "xml";
    public static final String PROC_DATA_FORMAT_JSON = "json";

    @Id
    @Column(name = "ID")
    private String id;

    @Column(name = "PROC_DEF_KEY")
    private String procDefKey;

    @Column(name = "PROC_DEF_NAME")
    private String procDefName;

    @Column(name = "PROC_DEF_KERNEL_ID")
    private String procDefKernelId;

    @Column(name = "PROC_DEF_VER")
    private Integer procDefVersion;

    @Column(name = "ROOT_ENTITY")
    private String rootEntity;
    
//    @Column(name = "ROOT_ENTITY_NAME")
//    private String rootEntityName;

    @Lob
    @Basic(fetch=FetchType.EAGER)
    @Column(name = "PROC_DEF_DATA",columnDefinition="text")
    private String procDefData;

    @Column(name = "PROC_DEF_DATA_FMT")
    private String procDefDataFormat = PROC_DATA_FORMAT_XML;
    
    @Column(name = "OWNER")
    private String owner;
    
    @Column(name = "OWNER_GRP")
    private String ownerGroup;
    
    @Column(name = "IS_DELETED")
    private boolean deleted = false;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProcDefKey() {
        return procDefKey;
    }

    public void setProcDefKey(String procDefKey) {
        this.procDefKey = procDefKey;
    }

    public String getProcDefName() {
        return procDefName;
    }

    public void setProcDefName(String procDefName) {
        this.procDefName = procDefName;
    }

    public String getProcDefKernelId() {
        return procDefKernelId;
    }

    public void setProcDefKernelId(String procDefKernelId) {
        this.procDefKernelId = procDefKernelId;
    }

    public Integer getProcDefVersion() {
        return procDefVersion;
    }

    public void setProcDefVersion(Integer procDefVersion) {
        this.procDefVersion = procDefVersion;
    }

    public String getRootEntity() {
        return rootEntity;
    }

    public void setRootEntity(String rootEntity) {
        this.rootEntity = rootEntity;
    }

    public String getProcDefData() {
        return procDefData;
    }

    public void setProcDefData(String procDefData) {
        this.procDefData = procDefData;
    }

    public String getProcDefDataFormat() {
        return procDefDataFormat;
    }

    public void setProcDefDataFormat(String procDefDataFormat) {
        this.procDefDataFormat = procDefDataFormat;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getOwnerGroup() {
        return ownerGroup;
    }

    public void setOwnerGroup(String ownerGroup) {
        this.ownerGroup = ownerGroup;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

//    public String getRootEntityName() {
//        return rootEntityName;
//    }
//
//    public void setRootEntityName(String rootEntityName) {
//        this.rootEntityName = rootEntityName;
//    }
}
