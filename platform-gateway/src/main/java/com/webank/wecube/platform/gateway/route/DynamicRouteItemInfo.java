package com.webank.wecube.platform.gateway.route;

/**
 * 
 * @author gavin
 *
 */
public class DynamicRouteItemInfo implements Cloneable {
	public static final String HTTP_SCHEMA = "http";
	public static final String HTTPS_SCHEMA = "https";
	private String itemId;
	private String name;
	private String httpSchema = HTTP_SCHEMA;
	private String host;
	private String port;

	private Long createTime;
	private Long lastModifiedTime;

	private boolean available;
	private int orderNo;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getHttpSchema() {
		return httpSchema;
	}

	public void setHttpSchema(String httpSchema) {
		this.httpSchema = httpSchema;
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

	public Long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Long createTime) {
		this.createTime = createTime;
	}

	public Long getLastModifiedTime() {
		return lastModifiedTime;
	}

	public void setLastModifiedTime(Long lastModifiedTime) {
		this.lastModifiedTime = lastModifiedTime;
	}

	public boolean isAvailable() {
		return available;
	}

	public void setAvailable(boolean available) {
		this.available = available;
	}

	public int getOrderNo() {
		return orderNo;
	}

	public void setOrderNo(int orderNo) {
		this.orderNo = orderNo;
	}

	public String getItemId() {
		return itemId;
	}

	public void setItemId(String itemId) {
		this.itemId = itemId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((host == null) ? 0 : host.hashCode());
		result = prime * result + ((httpSchema == null) ? 0 : httpSchema.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((port == null) ? 0 : port.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		DynamicRouteItemInfo other = (DynamicRouteItemInfo) obj;

		return checkProperties(other);
	}

	private boolean checkProperties(DynamicRouteItemInfo other) {
		if (host == null) {
			if (other.host != null) {
				return false;
			}
		} else if (!host.equals(other.host)) {
			return false;
		}
		if (httpSchema == null) {
			if (other.httpSchema != null) {
				return false;
			}
		} else if (!httpSchema.equals(other.httpSchema)) {
			return false;
		}
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		if (port == null) {
			if (other.port != null) {
				return false;
			}
		} else if (!port.equals(other.port)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("[itemId=");
		builder.append(itemId);
		builder.append(", name=");
		builder.append(name);
		builder.append(", httpSchema=");
		builder.append(httpSchema);
		builder.append(", host=");
		builder.append(host);
		builder.append(", port=");
		builder.append(port);
		builder.append("]");
		return builder.toString();
	}

	public DynamicRouteItemInfo clone() {
		DynamicRouteItemInfo retItem = new DynamicRouteItemInfo();
		retItem.setAvailable(this.isAvailable());
		retItem.setCreateTime(this.getCreateTime());
		retItem.setHost(this.getHost());
		retItem.setHttpSchema(this.getHttpSchema());
		retItem.setItemId(this.getItemId());
		retItem.setLastModifiedTime(this.getLastModifiedTime());
		retItem.setName(this.getName());
		retItem.setOrderNo(this.getOrderNo());
		retItem.setPort(this.getPort());

		return retItem;
	}

}
