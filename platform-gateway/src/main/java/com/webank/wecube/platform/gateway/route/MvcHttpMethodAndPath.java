package com.webank.wecube.platform.gateway.route;

import org.springframework.http.HttpMethod;

public class MvcHttpMethodAndPath {

	private HttpMethod httpMethod;
	private String path;

	public MvcHttpMethodAndPath(HttpMethod httpMethod, String path) {
		super();
		this.httpMethod = httpMethod;
		this.path = path;
	}

	public HttpMethod getHttpMethod() {
		return httpMethod;
	}

	public void setHttpMethod(HttpMethod httpMethod) {
		this.httpMethod = httpMethod;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((httpMethod == null) ? 0 : httpMethod.hashCode());
		result = prime * result + ((path == null) ? 0 : path.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MvcHttpMethodAndPath other = (MvcHttpMethodAndPath) obj;
		if (httpMethod != other.httpMethod)
			return false;
		if (path == null) {
			if (other.path != null)
				return false;
		} else if (!path.equals(other.path))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "[httpMethod=" + httpMethod + ", path=" + path + "]";
	}
}
