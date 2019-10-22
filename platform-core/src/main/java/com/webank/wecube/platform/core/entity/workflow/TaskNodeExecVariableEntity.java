package com.webank.wecube.platform.core.entity.workflow;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "CORE_RU_TASK_NODE_EXEC_VAR")
public class TaskNodeExecVariableEntity {
    @Id
    @Column(name = "ID")
    @GeneratedValue
    private Integer id;
    
    @Column(name = "CI_TYPE_ID")
    private Integer ciTypeId;
    
    @Column(name = "CI_GUID")
    private String ciGuid;

    @Column(name = "CONFIRMED")
    private Boolean confirmed;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "EXEC_LOG_ID")
    private TaskNodeExecLogEntity taskNodeExecLog;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getCiTypeId() {
        return ciTypeId;
    }

    public void setCiTypeId(Integer ciTypeId) {
        this.ciTypeId = ciTypeId;
    }

    public String getCiGuid() {
        return ciGuid;
    }

    public void setCiGuid(String ciGuid) {
        this.ciGuid = ciGuid;
    }

    public Boolean getConfirmed() {
        return confirmed;
    }

    public void setConfirmed(Boolean confirmed) {
        this.confirmed = confirmed;
    }

    public TaskNodeExecLogEntity getTaskNodeExecLog() {
        return taskNodeExecLog;
    }

    public void setTaskNodeExecLog(TaskNodeExecLogEntity taskNodeExecLog) {
        this.taskNodeExecLog = taskNodeExecLog;
    }

}
