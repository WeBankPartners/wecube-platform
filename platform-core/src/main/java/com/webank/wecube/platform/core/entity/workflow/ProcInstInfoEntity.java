package com.webank.wecube.platform.core.entity.workflow;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "CORE_RU_PROC_INST_INFO")
public class ProcInstInfoEntity extends AbstractInstanceStatusEntity {

    @Column(name = "PROC_INST_KEY")
    private String procInstKey;

    @Column(name = "PROC_INST_KERNEL_ID")
    private String procInstKernelId;

    @Column(name = "PROC_DEF_ID")
    private String procDefId;

    @Column(name = "PROC_DEF_KEY")
    private String procDefKey;

    @Column(name = "PROC_DEF_NAME")
    private String procDefName;

    public String getProcInstKey() {
        return procInstKey;
    }

    public void setProcInstKey(String procInstKey) {
        this.procInstKey = procInstKey;
    }

    public String getProcInstKernelId() {
        return procInstKernelId;
    }

    public void setProcInstKernelId(String procInstKernelId) {
        this.procInstKernelId = procInstKernelId;
    }

    public String getProcDefId() {
        return procDefId;
    }

    public void setProcDefId(String procDefId) {
        this.procDefId = procDefId;
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

}
