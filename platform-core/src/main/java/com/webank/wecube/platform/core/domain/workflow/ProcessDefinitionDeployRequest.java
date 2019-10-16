package com.webank.wecube.platform.core.domain.workflow;

import java.util.ArrayList;
import java.util.List;

public class ProcessDefinitionDeployRequest {
	private String processName;
	private String processData;
	
	private Integer rootCiTypeId;

	private List<ServiceTaskBindInfoVO> serviceTaskBindInfos = new ArrayList<ServiceTaskBindInfoVO>();

	public String getProcessName() {
		return processName;
	}

	public void setProcessName(String processName) {
		this.processName = processName;
	}

	public String getProcessData() {
		return processData;
	}

	public void setProcessData(String processData) {
		this.processData = processData;
	}
	
	public Integer getRootCiTypeId() {
		return rootCiTypeId;
	}

	public void setRootCiTypeId(Integer rootCiTypeId) {
		this.rootCiTypeId = rootCiTypeId;
	}

	public List<ServiceTaskBindInfoVO> getServiceTaskBindInfos() {
		return serviceTaskBindInfos;
	}

	public void setServiceTaskBindInfos(List<ServiceTaskBindInfoVO> serviceTaskBindInfos) {
		this.serviceTaskBindInfos = serviceTaskBindInfos;
	}
	
	public void addServiceTaskBindInfo(ServiceTaskBindInfoVO serviceTaskBindInfo) {
		this.serviceTaskBindInfos.add(serviceTaskBindInfo);
	}

	@Override
	public String toString() {
		return "ProcessDefinitionDeployRequest [processName=" + processName + ", processData=" + processData + "]";
	}
}
