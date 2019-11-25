package com.webank.wecube.platform.core.commons;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "wecube.core")
public class ApplicationProperties {
    private String gatewayUrl;

    @ConfigurationProperties(prefix = "wecube.core.httpclient")
    public class HttpClientProperties {
        private int connectTimeout = 30000;
        private int requestTimeout = 30000;
        private int socketTimeout = 1200000;
        private int maxTotalConnections = 200;
        private int poolSizeOfScheduler = 50;
        private int defaultKeepAliveTimeMillis = 1200000;
        private int closeIdleConnectionWaitTimeSecs = 30;

        public int getConnectTimeout() {
            return connectTimeout;
        }

        public int getRequestTimeout() {
            return requestTimeout;
        }

        public int getSocketTimeout() {
            return socketTimeout;
        }

        public int getMaxTotalConnections() {
            return maxTotalConnections;
        }

        public int getPoolSizeOfScheduler() {
            return poolSizeOfScheduler;
        }

        public int getDefaultKeepAliveTimeMillis() {
            return defaultKeepAliveTimeMillis;
        }

        public int getCloseIdleConnectionWaitTimeSecs() {
            return closeIdleConnectionWaitTimeSecs;
        }

    }

    @ConfigurationProperties(prefix = "wecube.core.plugin")
    public class PluginProperties {
        private String pluginDeployPath = "/opt";
        private String pluginPackageBucketName = "wecube-plugin-package-bucket";
        private String registerFile = "register.xml";
        private String imageFile = "image.tar";
        private String uiFile = "ui.zip";
        private String initDbSql = "init.sql";
        private String upgradeDbSql = "upgrade.sql";
        private String pluginPackageNameOfDeploy;
        private String staticResourceServerIp;
        private String staticResourceServerUser;
        private String staticResourceServerPassword;
        private Integer staticResourceServerPort;
        private String staticResourceServerPath;
        private String baseMountPath;

        public String getPluginDeployPath() {
            return pluginDeployPath;
        }

        public String getPluginPackageBucketName() {
            return pluginPackageBucketName;
        }

        public String getRegisterFile() {
            return registerFile;
        }

        public String getImageFile() {
            return imageFile;
        }

        public String getUiFile() {
            return uiFile;
        }

        public String getInitDbSql() {
            return initDbSql;
        }

        public String getUpgradeDbSql() {
            return upgradeDbSql;
        }

        public String getPluginPackageNameOfDeploy() {
            return pluginPackageNameOfDeploy;
        }

        public String getStaticResourceServerIp() {
            return staticResourceServerIp;
        }

        public String getStaticResourceServerUser() {
            return staticResourceServerUser;
        }

        public String getStaticResourceServerPassword() {
            return staticResourceServerPassword;
        }

        public Integer getStaticResourceServerPort() {
            return staticResourceServerPort;
        }

        public String getStaticResourceServerPath() {
            return staticResourceServerPath;
        }

        public String getBaseMountPath() {
            return baseMountPath;
        }

    }

    @ConfigurationProperties(prefix = "wecube.core.s3")
    public class S3Properties {
        private String endpoint;
        private String accessKey;
        private String secretKey;

        public String getEndpoint() {
            return endpoint;
        }

        public String getAccessKey() {
            return accessKey;
        }

        public String getSecretKey() {
            return secretKey;
        }

    }

    @ConfigurationProperties(prefix = "wecube.core.resource")
    public class ResourceProperties {
        private Integer dockerPullImageTimeout = 300;
        private String passwordEncryptionSeed;

        public Integer getDockerPullImageTimeout() {
            return dockerPullImageTimeout;
        }

        public String getPasswordEncryptionSeed() {
            return passwordEncryptionSeed;
        }

    }

    public String getGatewayUrl() {
        return gatewayUrl;
    }

}
