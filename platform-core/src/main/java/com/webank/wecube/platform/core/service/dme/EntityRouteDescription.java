package com.webank.wecube.platform.core.service.dme;

import java.io.Serializable;

public class EntityRouteDescription implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = -2432883105935803360L;
    private String packageName;
    private String entityName;
    private String httpScheme = "http";
    private String httpHost;
    private String httpPort;

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public String getHttpScheme() {
        return httpScheme;
    }

    public void setHttpScheme(String httpScheme) {
        this.httpScheme = httpScheme;
    }

    public String getHttpHost() {
        return httpHost;
    }

    public void setHttpHost(String httpHost) {
        this.httpHost = httpHost;
    }

    public String getHttpPort() {
        return httpPort;
    }

    public void setHttpPort(String httpPort) {
        this.httpPort = httpPort;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[packageName=");
        builder.append(packageName);
        builder.append(", entityName=");
        builder.append(entityName);
        builder.append(", httpScheme=");
        builder.append(httpScheme);
        builder.append(", httpHost=");
        builder.append(httpHost);
        builder.append(", httpPort=");
        builder.append(httpPort);
        builder.append("]");
        return builder.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((entityName == null) ? 0 : entityName.hashCode());
        result = prime * result + ((httpHost == null) ? 0 : httpHost.hashCode());
        result = prime * result + ((httpPort == null) ? 0 : httpPort.hashCode());
        result = prime * result + ((httpScheme == null) ? 0 : httpScheme.hashCode());
        result = prime * result + ((packageName == null) ? 0 : packageName.hashCode());
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
        EntityRouteDescription other = (EntityRouteDescription) obj;
        if (entityName == null) {
            if (other.entityName != null) {
                return false;
            }
        } else if (!entityName.equals(other.entityName)) {
            return false;
        }
        if (httpHost == null) {
            if (other.httpHost != null) {
                return false;
            }
        } else if (!httpHost.equals(other.httpHost)) {
            return false;
        }
        if (httpPort == null) {
            if (other.httpPort != null) {
                return false;
            }
        } else if (!httpPort.equals(other.httpPort)) {
            return false;
        }
        if (httpScheme == null) {
            if (other.httpScheme != null) {
                return false;
            }
        } else if (!httpScheme.equals(other.httpScheme)) {
            return false;
        }
        if (packageName == null) {
            if (other.packageName != null) {
                return false;
            }
        } else if (!packageName.equals(other.packageName)) {
            return false;
        }
        return true;
    }

}
