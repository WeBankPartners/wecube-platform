package com.webank.wecube.platform.core.dto.workflow;

public class ProcDefInfoExportImportDto {
    private String procDefKey;
    private String procDefName;
    private String procDefVersion;
    private String procDefData;
    private String procDefDataFmt;

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

    public String getProcDefVersion() {
        return procDefVersion;
    }

    public void setProcDefVersion(String procDefVersion) {
        this.procDefVersion = procDefVersion;
    }

    public String getProcDefData() {
        return procDefData;
    }

    public void setProcDefData(String procDefData) {
        this.procDefData = procDefData;
    }

    public String getProcDefDataFmt() {
        return procDefDataFmt;
    }

    public void setProcDefDataFmt(String procDefDataFmt) {
        this.procDefDataFmt = procDefDataFmt;
    }
}
