package com.webank.wecube.platform.core.domain.workflow;

import java.util.ArrayList;
import java.util.List;

public class ProcessDefinitionVO {
	private String processName;
	private String definitionId;
	private String definitionVersion;
	private String definitionBizKey;

	private String definitionText;
	
	private Integer rootCiTypeId;
	
	private List<ServiceTaskBindInfoVO> serviceTaskBindInfos = new ArrayList<ServiceTaskBindInfoVO>();

	public String getProcessName() {
		return processName;
	}

	public void setProcessName(String processName) {
		this.processName = processName;
	}

	public String getDefinitionId() {
		return definitionId;
	}

	public void setDefinitionId(String definitionId) {
		this.definitionId = definitionId;
	}

	public String getDefinitionVersion() {
		return definitionVersion;
	}

	public void setDefinitionVersion(String definitionVersion) {
		this.definitionVersion = definitionVersion;
	}

	public String getDefinitionBizKey() {
		return definitionBizKey;
	}

	public void setDefinitionBizKey(String definitionBizKey) {
		this.definitionBizKey = definitionBizKey;
	}

	public String getDefinitionText() {
		return definitionText;
	}

	public void setDefinitionText(String definitionText) {
		this.definitionText = definitionText;
	}

	public List<ServiceTaskBindInfoVO> getServiceTaskBindInfos() {
		return serviceTaskBindInfos;
	}

	public void setServiceTaskBindInfos(List<ServiceTaskBindInfoVO> serviceTaskBindInfos) {
		this.serviceTaskBindInfos = serviceTaskBindInfos;
	}

	public Integer getRootCiTypeId() {
		return rootCiTypeId;
	}

	public void setRootCiTypeId(Integer rootCiTypeId) {
		this.rootCiTypeId = rootCiTypeId;
	}
	
	public void addServiceTaskBindInfo(ServiceTaskBindInfoVO serviceTaskBindInfo) {
		this.serviceTaskBindInfos.add(serviceTaskBindInfo);
	}
}
