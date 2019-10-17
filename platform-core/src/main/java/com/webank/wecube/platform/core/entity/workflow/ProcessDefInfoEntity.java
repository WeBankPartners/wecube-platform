package com.webank.wecube.platform.core.entity.workflow;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "CORE_RE_PROC_DEF_INFO")
public class ProcessDefInfoEntity extends BaseStatusFeaturedEntity {

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

    @Column(name = "PROC_DEF_ID")
    private String procDefId;

    @Column(name = "PROC_DEF_NAME")
    private String procDefName;

    @Column(name = "KERNEL_ID")
    private String kernelProcDefId;

    @Column(name = "PROC_DEF_VER")
    private Integer procDefVersion;

    @Column(name = "ROOT_ENTITY")
    private String rootEntity;

    @Column(name = "PROC_DEF_DATA")
    private String procDefData;

    @Column(name = "PROC_DEF_DATA_FMT")
    private String procDefDataFormat = PROC_DATA_FORMAT_XML;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProcDefId() {
        return procDefId;
    }

    public void setProcDefId(String procDefId) {
        this.procDefId = procDefId;
    }

    public String getRootEntity() {
        return rootEntity;
    }

    public void setRootEntity(String rootEntity) {
        this.rootEntity = rootEntity;
    }

    public String getProcDefName() {
        return procDefName;
    }

    public void setProcDefName(String procDefName) {
        this.procDefName = procDefName;
    }

    public Integer getProcDefVersion() {
        return procDefVersion;
    }

    public void setProcDefVersion(Integer procDefVersion) {
        this.procDefVersion = procDefVersion;
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

}
