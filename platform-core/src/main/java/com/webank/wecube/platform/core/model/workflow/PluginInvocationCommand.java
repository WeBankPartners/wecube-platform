package com.webank.wecube.platform.core.model.workflow;

import java.util.ArrayList;
import java.util.List;

public class PluginInvocationCommand {
	private String procDefId;
	private String procDefKey;
	private Integer procDefVersion;

	private String procInstId;
	private String procInstKey;

	private String nodeId;
	private String nodeName;

	private String executionId;

	private List<String> allowedOptions = new ArrayList<>();

	public String getProcDefId() {
		return procDefId;
	}

	public void setProcDefId(String procDefId) {
		this.procDefId = procDefId;
	}

	public String getProcDefKey() {
		return procDefKey;
	}

	public void setProcDefKey(String procDefKey) {
		this.procDefKey = procDefKey;
	}

	public Integer getProcDefVersion() {
		return procDefVersion;
	}

	public void setProcDefVersion(Integer procDefVersion) {
		this.procDefVersion = procDefVersion;
	}

	public String getProcInstId() {
		return procInstId;
	}

	public void setProcInstId(String procInstId) {
		this.procInstId = procInstId;
	}

	public String getProcInstKey() {
		return procInstKey;
	}

	public void setProcInstKey(String procInstKey) {
		this.procInstKey = procInstKey;
	}

	public String getNodeId() {
		return nodeId;
	}

	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}

	public String getNodeName() {
		return nodeName;
	}

	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}

	public String getExecutionId() {
		return executionId;
	}

	public void setExecutionId(String executionId) {
		this.executionId = executionId;
	}

	public List<String> getAllowedOptions() {
		return allowedOptions;
	}

	public void setAllowedOptions(List<String> allowedOptions) {
		this.allowedOptions = allowedOptions;
	}
	
	public void addAllowedOptions(List<String> allowedOptions) {
		if(allowedOptions == null) {
			return;
		}
		if(this.allowedOptions == null) {
			this.allowedOptions = new ArrayList<>();
		}
		this.allowedOptions.addAll(allowedOptions);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("PluginInvocationCommand [procDefId=");
		builder.append(procDefId);
		builder.append(", procDefKey=");
		builder.append(procDefKey);
		builder.append(", procDefVersion=");
		builder.append(procDefVersion);
		builder.append(", procInstId=");
		builder.append(procInstId);
		builder.append(", procInstKey=");
		builder.append(procInstKey);
		builder.append(", nodeId=");
		builder.append(nodeId);
		builder.append(", nodeName=");
		builder.append(nodeName);
		builder.append(", executionId=");
		builder.append(executionId);
		builder.append(", allowedOptions=");
		builder.append(allowedOptions);
		builder.append("]");
		return builder.toString();
	}

}
