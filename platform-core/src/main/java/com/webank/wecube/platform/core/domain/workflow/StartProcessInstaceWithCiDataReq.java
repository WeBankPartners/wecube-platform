package com.webank.wecube.platform.core.domain.workflow;

public class StartProcessInstaceWithCiDataReq {

	private String ciTypeId;
	private String ciDataId;

//	private String processDefinitionId;
	private int processDefinitionKey;

	public String getCiTypeId() {
		return ciTypeId;
	}

	public void setCiTypeId(String ciTypeId) {
		this.ciTypeId = ciTypeId;
	}

	public String getCiDataId() {
		return ciDataId;
	}

	public void setCiDataId(String ciDataId) {
		this.ciDataId = ciDataId;
	}

    public int getProcessDefinitionKey() {
        return processDefinitionKey;
    }

    public void setProcessDefinitionKey(int processDefinitionKey) {
        this.processDefinitionKey = processDefinitionKey;
    }

	

//	public String getProcessDefinitionId() {
//		return processDefinitionId;
//	}
//
//	public void setProcessDefinitionId(String processDefinitionId) {
//		this.processDefinitionId = processDefinitionId;
//	}
}
