package com.webank.wecube.platform.core.dto.workflow;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author howechen
 */
public class ProcRoleOverviewDto {
    @JsonProperty(value = "processId")
    private String processId;
    @JsonProperty(value = "MGMT")
    private List<String> mgmtRoleList = new ArrayList<>();
    @JsonProperty(value = "USE")
    private List<String> useRoleList = new ArrayList<>();

    

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public List<String> getMgmtRoleList() {
        return mgmtRoleList;
    }

    public void setMgmtRoleList(List<String> mgmtRoleList) {
        this.mgmtRoleList = mgmtRoleList;
    }

    public List<String> getUseRoleList() {
        return useRoleList;
    }

    public void setUseRoleList(List<String> useRoleList) {
        this.useRoleList = useRoleList;
    }

    
}
