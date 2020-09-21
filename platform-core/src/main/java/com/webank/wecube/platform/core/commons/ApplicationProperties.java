package com.webank.wecube.platform.core.commons;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "wecube.core")
public class ApplicationProperties {
    private String gatewayUrl = "127.0.0.1:19110";
    private String dbInitStrategy = "update";
    private String jwtSigningKey = "Platform+Auth+Server+Secret";
    
    @ConfigurationProperties(prefix = "wecube.core.config")
    public class AppConfigProperties {
        private String propertyRsaKey = null;
        private String propertyRsaPubKey = null;

        public String getPropertyRsaKey() {
            return propertyRsaKey;
        }

        public void setPropertyRsaKey(String propertyRsaKey) {
            this.propertyRsaKey = propertyRsaKey;
        }

        public String getPropertyRsaPubKey() {
            return propertyRsaPubKey;
        }

        public void setPropertyRsaPubKey(String propertyRsaPubKey) {
            this.propertyRsaPubKey = propertyRsaPubKey;
        }
    }

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

        public void setConnectTimeout(int connectTimeout) {
            this.connectTimeout = connectTimeout;
        }

        public void setRequestTimeout(int requestTimeout) {
            this.requestTimeout = requestTimeout;
        }

        public void setSocketTimeout(int socketTimeout) {
            this.socketTimeout = socketTimeout;
        }

        public void setMaxTotalConnections(int maxTotalConnections) {
            this.maxTotalConnections = maxTotalConnections;
        }

        public void setPoolSizeOfScheduler(int poolSizeOfScheduler) {
            this.poolSizeOfScheduler = poolSizeOfScheduler;
        }

        public void setDefaultKeepAliveTimeMillis(int defaultKeepAliveTimeMillis) {
            this.defaultKeepAliveTimeMillis = defaultKeepAliveTimeMillis;
        }

        public void setCloseIdleConnectionWaitTimeSecs(int closeIdleConnectionWaitTimeSecs) {
            this.closeIdleConnectionWaitTimeSecs = closeIdleConnectionWaitTimeSecs;
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

        public void setPluginDeployPath(String pluginDeployPath) {
            this.pluginDeployPath = pluginDeployPath;
        }

        public void setPluginPackageBucketName(String pluginPackageBucketName) {
            this.pluginPackageBucketName = pluginPackageBucketName;
        }

        public void setRegisterFile(String registerFile) {
            this.registerFile = registerFile;
        }

        public void setImageFile(String imageFile) {
            this.imageFile = imageFile;
        }

        public void setUiFile(String uiFile) {
            this.uiFile = uiFile;
        }

        public void setInitDbSql(String initDbSql) {
            this.initDbSql = initDbSql;
        }

        public void setUpgradeDbSql(String upgradeDbSql) {
            this.upgradeDbSql = upgradeDbSql;
        }

        public void setPluginPackageNameOfDeploy(String pluginPackageNameOfDeploy) {
            this.pluginPackageNameOfDeploy = pluginPackageNameOfDeploy;
        }

        public void setStaticResourceServerIp(String staticResourceServerIp) {
            this.staticResourceServerIp = staticResourceServerIp;
        }

        public void setStaticResourceServerUser(String staticResourceServerUser) {
            this.staticResourceServerUser = staticResourceServerUser;
        }

        public void setStaticResourceServerPassword(String staticResourceServerPassword) {
            this.staticResourceServerPassword = staticResourceServerPassword;
        }

        public void setStaticResourceServerPort(Integer staticResourceServerPort) {
            this.staticResourceServerPort = staticResourceServerPort;
        }

        public void setStaticResourceServerPath(String staticResourceServerPath) {
            this.staticResourceServerPath = staticResourceServerPath;
        }

        public void setBaseMountPath(String baseMountPath) {
            this.baseMountPath = baseMountPath;
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

        public void setEndpoint(String endpoint) {
            this.endpoint = endpoint;
        }

        public void setAccessKey(String accessKey) {
            this.accessKey = accessKey;
        }

        public void setSecretKey(String secretKey) {
            this.secretKey = secretKey;
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

        public void setDockerPullImageTimeout(Integer dockerPullImageTimeout) {
            this.dockerPullImageTimeout = dockerPullImageTimeout;
        }

        public void setPasswordEncryptionSeed(String passwordEncryptionSeed) {
            this.passwordEncryptionSeed = passwordEncryptionSeed;
        }

    }

    @ConfigurationProperties(prefix = "wecube.core.docker-remote")
    public class DockerRemoteProperties {
        private Integer port = 2375;
        private Boolean enableTls = false;
        private String certPath;

        public Boolean getEnableTls() {
            return enableTls;
        }

        public void setEnableTls(Boolean enableTls) {
            this.enableTls = enableTls;
        }

        public String getCertPath() {
            return certPath;
        }

        public void setCertPath(String certPath) {
            this.certPath = certPath;
        }

        public Integer getPort() {
            return port;
        }

        public void setPort(Integer port) {
            this.port = port;
        }
    }

    public String getDbInitStrategy() {
        return dbInitStrategy;
    }

    public void setDbInitStrategy(String dbInitStrategy) {
        this.dbInitStrategy = dbInitStrategy;
    }

    public String getGatewayUrl() {
        return gatewayUrl;
    }

    public void setGatewayUrl(String gatewayUrl) {
        this.gatewayUrl = gatewayUrl;
    }

    public String getJwtSigningKey() {
		return jwtSigningKey;
	}

    public void setJwtSigningKey(String jwtSigningKey) {
		this.jwtSigningKey = jwtSigningKey;
	}

    
}
