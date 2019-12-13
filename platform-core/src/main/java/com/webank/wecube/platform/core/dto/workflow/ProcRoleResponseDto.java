package com.webank.wecube.platform.core.dto.workflow;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.webank.wecube.platform.core.entity.workflow.ProcRoleBindingEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * @author howechen
 */
public class ProcRoleResponseDto {
    @JsonProperty(value = "processId")
    private String processId;
    @JsonProperty(value = "mgmt")
    private List<Long> mgmtRoleIdList;
    @JsonProperty(value = "use")
    private List<Long> useRoleIdList;

    public ProcRoleResponseDto(String processId, List<Long> mgmtRoleIdList, List<Long> useRoleIdList) {
        this.processId = processId;
        this.mgmtRoleIdList = mgmtRoleIdList;
        this.useRoleIdList = useRoleIdList;
    }

    public ProcRoleResponseDto() {
        this.mgmtRoleIdList = new ArrayList<>();
        this.useRoleIdList = new ArrayList<>();
    }

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public List<Long> getMgmtRoleIdList() {
        return mgmtRoleIdList;
    }

    public void setMgmtRoleIdList(List<Long> mgmtRoleIdList) {
        this.mgmtRoleIdList = mgmtRoleIdList;
    }

    public List<Long> getUseRoleIdList() {
        return useRoleIdList;
    }

    public void setUseRoleIdList(List<Long> useRoleIdList) {
        this.useRoleIdList = useRoleIdList;
    }
}
