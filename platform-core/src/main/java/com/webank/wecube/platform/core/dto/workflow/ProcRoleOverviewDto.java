package com.webank.wecube.platform.core.dto.workflow;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.webank.wecube.platform.core.entity.workflow.ProcRoleBindingEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * @author howechen
 */
public class ProcRoleOverviewDto {
    @JsonProperty(value = "processId")
    private String processId;
    @JsonProperty(value = "MGMT")
    private List<String> mgmtRoleIdList;
    @JsonProperty(value = "USE")
    private List<String> useRoleIdList;

    public ProcRoleOverviewDto(String processId, List<String> mgmtRoleIdList, List<String> useRoleIdList) {
        this.processId = processId;
        this.mgmtRoleIdList = mgmtRoleIdList;
        this.useRoleIdList = useRoleIdList;
    }

    public ProcRoleOverviewDto() {
        this.mgmtRoleIdList = new ArrayList<>();
        this.useRoleIdList = new ArrayList<>();
    }

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public List<String> getMgmtRoleIdList() {
        return mgmtRoleIdList;
    }

    public void setMgmtRoleIdList(List<String> mgmtRoleIdList) {
        this.mgmtRoleIdList = mgmtRoleIdList;
    }

    public List<String> getUseRoleIdList() {
        return useRoleIdList;
    }

    public void setUseRoleIdList(List<String> useRoleIdList) {
        this.useRoleIdList = useRoleIdList;
    }
}
