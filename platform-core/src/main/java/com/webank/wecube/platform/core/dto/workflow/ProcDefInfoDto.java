package com.webank.wecube.platform.core.dto.workflow;

import java.util.ArrayList;
import java.util.List;

public class ProcDefInfoDto extends BaseProcDefDto{

    private List<TaskNodeDefInfoDto> taskNodeInfos = new ArrayList<>();


    public List<TaskNodeDefInfoDto> getTaskNodeInfos() {
        return taskNodeInfos;
    }

    public void setTaskNodeInfos(List<TaskNodeDefInfoDto> taskNodeInfos) {
        this.taskNodeInfos = taskNodeInfos;
    }
    
    public ProcDefInfoDto addTaskNodeInfo(TaskNodeDefInfoDto taskNodeInfo) {
        if(this.taskNodeInfos == null){
            this.taskNodeInfos = new ArrayList<>();
        }
        
        this.taskNodeInfos.add(taskNodeInfo);
        return this;
    }

}
