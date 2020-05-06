package com.webank.wecube.platform.gateway.route;

public class HttpDestination {
	public static final String HTTP_SCHEME = "http";
	public static final String HTTPS_SCHEME = "https";
	public static final int DEFAULT_WEIGHT = 0;

	private String scheme;
	private int port;
	private String host;

	private int weight = DEFAULT_WEIGHT;
	private long createdTime = System.currentTimeMillis();
	private long lastModifiedTime = System.currentTimeMillis();

	private long version;

	private boolean disabled;

	public HttpDestination(String scheme, String host, int port) {
		super();
		this.scheme = scheme;
		this.port = port;
		this.host = host;
		this.weight = DEFAULT_WEIGHT;
	}

	public HttpDestination(String scheme, String host, int port, int weight) {
		super();
		this.scheme = scheme;
		this.port = port;
		this.host = host;
		this.weight = weight;
	}

	HttpDestination disabled(boolean disabledFlag) {
		this.disabled = disabledFlag;
		lastModifiedTime();
		return this;
	}

	HttpDestination version(long version) {
		this.version = version;
		lastModifiedTime();
		return this;
	}

	HttpDestination weight(int weight) {
		this.weight = weight;
		lastModifiedTime();
		return this;
	}

	public String getScheme() {
		return scheme;
	}

	public int getPort() {
		return port;
	}

	public String getHost() {
		return host;
	}

	public long getVersion() {
		return version;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((host == null) ? 0 : host.hashCode());
		result = prime * result + port;
		result = prime * result + ((scheme == null) ? 0 : scheme.hashCode());
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
		HttpDestination other = (HttpDestination) obj;
		if (host == null) {
			if (other.host != null)
				return false;
		} else if (!host.equals(other.host))
			return false;
		if (port != other.port)
			return false;
		if (scheme == null) {
			if (other.scheme != null)
				return false;
		} else if (!scheme.equals(other.scheme))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("HttpDestination [scheme=");
		builder.append(scheme);
		builder.append(", port=");
		builder.append(port);
		builder.append(", host=");
		builder.append(host);
		builder.append("]");
		return builder.toString();
	}

	public int getWeight() {
		return weight;
	}

	public long getCreatedTime() {
		return createdTime;
	}

	public long getLastModifiedTime() {
		return lastModifiedTime;
	}

	private void lastModifiedTime() {
		this.lastModifiedTime = System.currentTimeMillis();
	}

	public boolean isDisabled() {
		return disabled;
	}

	public HttpDestination clone() {
		HttpDestination h = new HttpDestination(this.scheme, this.host, this.port, this.weight);
		return h;
	}

}
