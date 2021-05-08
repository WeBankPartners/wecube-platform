package com.webank.wecube.platform.core.support.plugin.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PluginRequest<DATATYPE> {
	private String requestId;
	private String dueDate;
	private List<String> allowedOptions;
	private List<DATATYPE> inputs;

	public PluginRequest<DATATYPE> withInputs(List<DATATYPE> inputs) {
		this.inputs = inputs;
		return this;
	}

	public PluginRequest<DATATYPE> withRequestId(String requestId) {
		this.requestId = requestId;
		return this;
	}

	public PluginRequest<DATATYPE> withAllowedOptions(List<String> allowedOptions) {
		if (this.allowedOptions == null) {
			this.allowedOptions = new ArrayList<>();
		}
		this.allowedOptions.addAll(allowedOptions);
		return this;
	}

	public PluginRequest<DATATYPE> withDueDate(String dueDate){
		this.dueDate = dueDate;
		return this;
	}


	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	public List<DATATYPE> getInputs() {
		return inputs;
	}

	public void setInputs(List<DATATYPE> inputs) {
		this.inputs = inputs;
	}

	public List<String> getAllowedOptions() {
		return allowedOptions;
	}

	public void setAllowedOptions(List<String> allowedOptions) {
		this.allowedOptions = allowedOptions;
	}

	public void setDueDate(String dueDate) { this.dueDate = dueDate; }

	public String getDueDate() {
		return dueDate;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("PluginRequest [requestId=");
		builder.append(requestId);
		builder.append(", dueDate=");
		builder.append(dueDate);
		builder.append(", allowedOptions=");
		builder.append(allowedOptions);
		builder.append(", inputs=");
		builder.append(inputs);
		builder.append("]");
		return builder.toString();
	}

	public static class DefaultPluginRequest extends PluginRequest<Map<String, Object>> {
	}

}
