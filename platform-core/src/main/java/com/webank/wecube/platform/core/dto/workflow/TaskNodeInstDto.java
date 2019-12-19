package com.webank.wecube.platform.core.dto.workflow;

public class TaskNodeInstDto extends FlowNodeInstDto{

    private Integer id;
    
    private String status; // Completed,Faulted,InProgress,Timeouted,NotStarted

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

   
    

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
