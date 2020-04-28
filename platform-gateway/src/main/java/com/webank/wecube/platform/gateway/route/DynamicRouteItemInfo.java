package com.webank.wecube.platform.gateway.route;

public class DynamicRouteItemInfo{
	public static final String HTTP_SCHEME = "http";
	public static final String HTTPS_SCHEME = "https";
	private String itemId;
	private String context;
	private String httpMethod;
	private String path;
	private String httpScheme = HTTP_SCHEME;
	private String host;
	private int port;

	private boolean disabled;
	private int weight;

	public String getItemId() {
		return itemId;
	}

	public void setItemId(String itemId) {
		this.itemId = itemId;
	}

	public String getContext() {
		return context;
	}

	public void setContext(String context) {
		this.context = context;
	}

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

	public String getHttpScheme() {
		return httpScheme;
	}

	public void setHttpScheme(String httpScheme) {
		this.httpScheme = httpScheme;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}

	public boolean isDisabled() {
		return disabled;
	}

	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

	public DynamicRouteItemInfo clone() {
		
		DynamicRouteItemInfo d = new DynamicRouteItemInfo();
		d.setContext(this.getContext());
		d.setDisabled(this.disabled);
		d.setHost(this.getHost());
		d.setHttpMethod(this.getHttpMethod());
		d.setHttpScheme(this.getHttpScheme());
		d.setItemId(this.getItemId());
		d.setPath(this.getPath());
		d.setPort(this.getPort());
		d.setWeight(this.getWeight());
		
		return d;
		
	}

	
}
