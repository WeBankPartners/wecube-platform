package com.webank.wecube.platform.core.commons;

import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.unit.DataSize;

@Data
@ConfigurationProperties(prefix = "wecube.core")
public class ApplicationProperties {
    private static final String AUTH_PROVIDER_LOCAL = "local";
    private static final String AUTH_PROVIDER_CAS = "CAS";

    private String authenticationProvider;
    private String casServerUrl = "";
    private String cmdbServerUrl = "";
    private String casRedirectAppAddr = "";
    private DataSize maxFileSize = DataSize.ofKilobytes(64);
    private boolean securityEnabled = true;

    public boolean isAuthenticationProviderLocal() {
        return AUTH_PROVIDER_LOCAL.equalsIgnoreCase(authenticationProvider);
    }

    public boolean isAuthenticationProviderCAS() {
        return AUTH_PROVIDER_CAS.equalsIgnoreCase(authenticationProvider);
    }

    @Data
    @ConfigurationProperties(prefix = "wecube.core.httpclient")
    public class HttpClientProperties {
        private int connectTimeout = 30000;
        private int requestTimeout = 30000;
        private int socketTimeout = 1200000;
        private int maxTotalConnections = 50;
        private int poolSizeOfScheduler = 50;
        private int defaultKeepAliveTimeMillis = 1200000;
        private int closeIdleConnectionWaitTimeSecs = 30;
    }

    @Data
    @ConfigurationProperties(prefix = "wecube.core.cmdb-data")
    public class CmdbDataProperties {
        private Integer enumCategoryTypeSystem = 1;
        private Integer enumCategoryTypeCommon = 2;
        private Integer ciTypeIdOfIdcDesign = 22;
        private Integer ciTypeIdOfIdc = 16;
        private Integer ciTypeIdOfSystemDesign = 1;
        private Integer ciTypeIdOfSubsys = 7;
        private Integer ciTypeIdOfUnitDesign = 3;
        private Integer ciTypeIdOfPackage = 11;
        private String referenceNameOfRelate = "关联";
        private String referenceNameOfBelong = "属于";
        private String referenceNameOfRealize = "实现";
        private Integer ciTypeIdOfZoneLinkDesign = 24;
        private Integer ciTypeIdOfZone = 17;
        private Integer ciTypeIdOfZoneLink = 18;
        private Integer ciTypeIdOfZoneDesign = 23;

        // for getApplicationDeploymentDesignDataTreeBySystemDesignGuidAndEnvCode()
        private Integer ciTypeIdOfHost = 12;
        private Integer ciTypeIdOfInstance = 15;
        private Integer ciTypeIdOfUnit = 8;
        private Integer ciTypeIdOfSubsystemDesign = 2;
        private String referenceNameOfRunning = "运行在";

        private String enumCategoryCiTypeLayer = "ci_layer";
        private String enumCategoryCiTypeCatalog = "ci_catalog";
        private String enumCategoryCiTypeZoomLevels = "ci_zoom_level";
        private String enumCategoryNameOfEnv = "env";
        private String enumCategoryNameOfDiffConf = "diff_conf";
        private String enumCodeOfStateDelete = "delete";
        private String propertyNameOfFixedDate = "fixed_date";
        private String enumCategoryCiStateOfCreate = "ci_state_create";
        private String enumCodeChangeOfCiStateOfCreate = "update";
        private String enumCodeDestroyedOfCiStateOfCreate = "delete";
        private String enumCategorySecurity = "security";
        private String enumCodeOfSeed = "seed";

        private String statusAttributeName = "status";
        private String businessKeyAttributeName = "bizKey";
        private String catNameOfDeployDesign = "tab_of_deploy_design";
        private String catNameOfArchitectureDesign = "tab_of_architecture_design";
        private String catNameOfPlanningDesign = "tab_of_planning_design";
        private String catNameOfResourcePlanning = "tab_of_resource_planning";
        private String catNameOfQueryDeployDesign = "tab_query_of_deploy_design";
        private String codeOfDeployDetail = "guid_of_deploy_detail";
        private String propertyNameOfState = "state";
    }

    @Data
    @ConfigurationProperties(prefix = "wecube.core.api-proxy")
    public class ApiProxyProperties {
        private Map<String, String> customHeaders = new LinkedHashMap<>();
        private Set<String> sensitiveHeaders = null;
    }

    @Data
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
    }

    @Data
    @ConfigurationProperties(prefix = "wecube.core.s3")
    public class S3Properties {
        private String endpoint;
        private String accessKey;
        private String secretKey;
    }

    @Data
    @ConfigurationProperties(prefix = "wecube.core.resource")
    public class ResourceProperties {
        private Integer dockerPullImageTimeout = 300;
        private String passwordEncryptionSeed;
    }
}
