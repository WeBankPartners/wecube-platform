package com.webank.wecube.platform.core.support.gateway;

public class RouteItem {
	private String context;
	private String httpMethod;
	private String path;
	private String httpScheme;
	private String host;
	private String port;
	
	private String weight;

    public RouteItem() {
    }

    public RouteItem(String context, String httpScheme, String host, String port) {
        this.context = context;
        this.httpScheme = httpScheme;
        this.host = host;
        this.port = port;
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

	public String getWeight() {
		return weight;
	}

	public void setWeight(String weight) {
		this.weight = weight;
	}

	public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }
}
