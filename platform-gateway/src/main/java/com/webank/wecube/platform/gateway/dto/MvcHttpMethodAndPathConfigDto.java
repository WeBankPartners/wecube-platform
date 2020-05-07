package com.webank.wecube.platform.gateway.dto;

import java.util.ArrayList;
import java.util.List;

public class MvcHttpMethodAndPathConfigDto {


	private String httpMethod;
	private String path;
	private long createdTime;
	private long lastModifiedTime;
	private boolean disabled;

	private long version;

	private List<HttpDestinationDto> httpDestinations = new ArrayList<>();

	public String getHttpMethod() {
		return httpMethod;
	}

	public void setHttpMethod(String httpMethod) {
		this.httpMethod = httpMethod;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public long getCreatedTime() {
		return createdTime;
	}

	public void setCreatedTime(long createdTime) {
		this.createdTime = createdTime;
	}

	public long getLastModifiedTime() {
		return lastModifiedTime;
	}

	public void setLastModifiedTime(long lastModifiedTime) {
		this.lastModifiedTime = lastModifiedTime;
	}

	public boolean isDisabled() {
		return disabled;
	}

	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

	public long getVersion() {
		return version;
	}

	public void setVersion(long version) {
		this.version = version;
	}

	public List<HttpDestinationDto> getHttpDestinations() {
		return httpDestinations;
	}

	public void setHttpDestinations(List<HttpDestinationDto> httpDestinations) {
		this.httpDestinations = httpDestinations;
	}

	public void addHttpDestinations(HttpDestinationDto httpDestination) {
		this.httpDestinations.add(httpDestination);
	}
    

}
