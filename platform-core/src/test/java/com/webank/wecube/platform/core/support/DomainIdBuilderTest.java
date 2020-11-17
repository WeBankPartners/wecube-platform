package com.webank.wecube.platform.core.support;

import com.webank.wecube.platform.core.domain.MenuItem;
import com.webank.wecube.platform.core.domain.ResourceItem;
import com.webank.wecube.platform.core.domain.ResourceServerDomain;
import com.webank.wecube.platform.core.domain.SystemVariable;
import com.webank.wecube.platform.core.domain.plugin.*;
import com.webank.wecube.platform.core.dto.PluginPackageDataModelDto;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DomainIdBuilderTest {
    private String packageName = "service-management";
    private String packageVersion = "v1.0";
    private PluginPackage pluginPackage = new PluginPackage(packageName, packageVersion);

    @Test
    public void givenPluginPackageWhenBuildIdThenReturnCorrectId() {
        String expectedPackageId = "service-management__v1.0";
        assertThat(DomainIdBuilder.buildDomainId(pluginPackage)).isEqualTo(expectedPackageId);
    }

    @Test
    public void givePluginPackageDependencyWhenBuildIdThenReturnCorrectId() {
        PluginPackageDependency domain = new PluginPackageDependency(null, pluginPackage, "dependencyA", "v2.5");

        String expectedId = "service-management__v1.0__dependencyA__v2.5";
        assertThat(DomainIdBuilder.buildDomainId(domain)).isEqualTo(expectedId);
    }

    @Test
    public void givenPluginPackageMenuWhenBuildIdThenReturnCorrectId() {
        PluginPackageMenu domain = new PluginPackageMenu(pluginPackage, "JOBS_SERVICE_CATALOG_MANAGEMENT", "JOBS", "Servive Catalog Management", "/service-catalog");
        String expectedId = "service-management__v1.0__JOBS__JOBS_SERVICE_CATALOG_MANAGEMENT";
        assertThat(DomainIdBuilder.buildDomainId(domain)).isEqualTo(expectedId);
    }

    @Test
    public void givenDataModelWhenBuildIdThenReturnCorrectId() {
        PluginPackageDataModel domain = new PluginPackageDataModel(null, 1, packageName, false, null, null, PluginPackageDataModelDto.Source.PLUGIN_PACKAGE.name(), System.currentTimeMillis(), null);
        String expectedId = "DataModel__service-management__1";
        assertThat(DomainIdBuilder.buildDomainId(domain)).isEqualTo(expectedId);
    }

    @Test
    public void givenPluginPackageEntityWhenBuildIdThenReturnCorrectId() {
        PluginPackageDataModel dataModel = new PluginPackageDataModel(null, 1, packageName, false, null, null, PluginPackageDataModelDto.Source.PLUGIN_PACKAGE.name(), System.currentTimeMillis(), null);
        PluginPackageEntity domain = new PluginPackageEntity(dataModel, "service_request", "service_request", "Service Request");
        String expectedId = "service-management__1__service_request";

        assertThat(DomainIdBuilder.buildDomainId(domain)).isEqualTo(expectedId);
    }

    @Test
    public void givenPluginPackageAttributeWhenBuildIdThenReturnCorrectId() {
        PluginPackageDataModel dataModel = new PluginPackageDataModel(null, 1, packageName, false, null, null, PluginPackageDataModelDto.Source.PLUGIN_PACKAGE.name(), System.currentTimeMillis(), null);
        PluginPackageEntity entity = new PluginPackageEntity(dataModel, "service_request", "service_request", "Service Request");
        PluginPackageAttribute domain = new PluginPackageAttribute(entity, null, "status", "Status", "str");
        String expectedId = "service-management__1__service_request__status";

        assertThat(DomainIdBuilder.buildDomainId(domain)).isEqualTo(expectedId);
    }

    @Test
    public void givenSystemVariableWhenBuildIdThenReturnCorrectId() {
        SystemVariable systemGlobalVariable = new SystemVariable(null, "MOUNT_PATH", "/opt", null, SystemVariable.SCOPE_GLOBAL, SystemVariable.SOURCE_SYSTEM, null, SystemVariable.ACTIVE);
        String expectedGlobalVariable = "system__global__MOUNT_PATH";
        assertThat(DomainIdBuilder.buildDomainId(systemGlobalVariable)).isEqualTo(expectedGlobalVariable);

        SystemVariable pluginGlobalVariable = new SystemVariable(null, "CMDB_URL", "http://blabla.com/cmdb", null, SystemVariable.SCOPE_GLOBAL, "wecmdb__v1.0", "wecmdb", SystemVariable.ACTIVE);
        String expectedPluginGlobalVariable = "wecmdb__v1.0__global__CMDB_URL";
        assertThat(DomainIdBuilder.buildDomainId(pluginGlobalVariable)).isEqualTo(expectedPluginGlobalVariable);

        SystemVariable pluginPackageVariable = new SystemVariable(null, "accessKey", "blablaAbcXyz", null, "wecmdb", "wecmdb__v1.0", "wecmdb", SystemVariable.ACTIVE);
        String expectedPluginPackageVariable = "wecmdb__v1.0__wecmdb__accessKey";
        assertThat(DomainIdBuilder.buildDomainId(pluginPackageVariable)).isEqualTo(expectedPluginPackageVariable);
    }

    @Test
    public void givenPluginPackageAuthorityWhenBuildIdThenReturnCorrectId() {
        PluginPackageAuthority domain = new PluginPackageAuthority(null, pluginPackage, "admin", "JOBS_SERVICE_CATALOG_MANAGEMENT");
        String expectedId = "service-management__v1.0__admin__JOBS_SERVICE_CATALOG_MANAGEMENT";
        assertThat(DomainIdBuilder.buildDomainId(domain)).isEqualTo(expectedId);
    }

    @Test
    public void givenDockerWhenBuildIdThenReturnCorrectId() {
        PluginPackageRuntimeResourcesDocker domain = new PluginPackageRuntimeResourcesDocker(null, pluginPackage, "service_management", "service_management", "22000:21000", "", "-e DATA_SOURCE_URL={%s} -e DB_USER={%s} -e DB_PWD={%s} -e CORE_ADDR={%s}");
        String expectedId = "Docker__service-management__v1.0__service_management";
        assertThat(DomainIdBuilder.buildDomainId(domain)).isEqualTo(expectedId);
    }

    @Test
    public void givenMySqlWhenBuildIdThenReturnCorrectId() {
        PluginPackageRuntimeResourcesMysql domain = new PluginPackageRuntimeResourcesMysql(null, pluginPackage, "service_management", "init.sql", "update.sql");
        String expectedId = "MySql__service-management__v1.0__service_management";
        assertThat(DomainIdBuilder.buildDomainId(domain)).isEqualTo(expectedId);
    }

    @Test
    public void givenS3WhenBuildIdThenReturnCorrectId() {
        PluginPackageRuntimeResourcesS3 domain = new PluginPackageRuntimeResourcesS3(null, pluginPackage, "service-management");
        String expectedId = "S3__service-management__v1.0__service-management";
        assertThat(DomainIdBuilder.buildDomainId(domain)).isEqualTo(expectedId);
    }

    @Test
    public void givenPluginConfigWhenBuildIdThenReturnCorrectId() {
        PluginConfig domain = new PluginConfig(null, pluginPackage, "task", "service-management", "task", PluginConfig.Status.DISABLED, null);
        String expectedId = "service-management__v1.0__task";
        assertThat(DomainIdBuilder.buildDomainId(domain)).isEqualTo(expectedId);
    }

    @Test
    public void givenPluginConfigInterfaceWhenBuildIdThenReturnCorrectId() {
        PluginConfig pluginConfig = new PluginConfig(null, pluginPackage, "task", "service-management", "task", PluginConfig.Status.DISABLED, null);
        PluginConfigInterface domain = new PluginConfigInterface(null, pluginConfig, "create", null, null, "/service-management/tasks", "POST", null, null);
        String expectedId = "service-management__v1.0__task__create__task";
        assertThat(DomainIdBuilder.buildDomainId(domain)).isEqualTo(expectedId);
    }

    @Test
    public void givenPluginConfigInterfaceParameterWhenBuildIdThenReturnCorrectId() {
        PluginConfig pluginConfig = new PluginConfig(null, pluginPackage, "task", "service-management", "task", PluginConfig.Status.DISABLED, null);
        pluginConfig.initId();
        PluginConfigInterface configInterface = new PluginConfigInterface(null, pluginConfig, "create", null, null, "/service-management/tasks", "POST", null, null);
        configInterface.initId();
        PluginConfigInterfaceParameter inputParameter = new PluginConfigInterfaceParameter(null, configInterface, PluginConfigInterfaceParameter.TYPE_INPUT, "callbackUrl", "string", "system_variable", null, null, "Y");
        String expectedInputId = "service-management__v1.0__task__create__task__INPUT__callbackUrl";
        assertThat(DomainIdBuilder.buildDomainId(inputParameter)).isEqualTo(expectedInputId);

        PluginConfigInterfaceParameter outputParameter = new PluginConfigInterfaceParameter(null, configInterface, PluginConfigInterfaceParameter.TYPE_OUTPUT, "status", "string", null, null, null, null);
        String expectedOutputId = "service-management__v1.0__task__create__task__OUTPUT__status";
        assertThat(DomainIdBuilder.buildDomainId(outputParameter)).isEqualTo(expectedOutputId);
    }

    @Test
    public void givenPluginInstanceWhenBuildIdThenReturnCorrectId() {
        PluginPackage qcloudPackage = new PluginPackage("qcloud", "v1.1");
        PluginInstance domain = new PluginInstance(null, qcloudPackage, "qcloud", "wecube-plugins-qcloud-v1.3", "127.0.0.1", 20000);
        String expectedId = "wecube-plugins-qcloud-v1.3__127.0.0.1__20000";
        assertThat(DomainIdBuilder.buildDomainId(domain)).isEqualTo(expectedId);
    }

    @Test
    public void givenPluginMysqlInstanceWhenBuildIdThenReturnCorrectId() {
        pluginPackage.initId();
        PluginMysqlInstance domain = new PluginMysqlInstance("service_mgmt", "service-mgmt__v1.8.2__service_mgmt__service_mgmt__service_mgmt__mysql_database", "service_mgmt", "woszHUwVEmYQF7qYCpYm5xsUF6hh02IWInyBHxtgn1A=", PluginMysqlInstance.MYSQL_INSTANCE_STATUS_ACTIVE, pluginPackage);
        String expectedId = "service-management__v1.0__service_mgmt__service_mgmt";
        assertThat(DomainIdBuilder.buildDomainId(domain)).isEqualTo(expectedId);
    }

    @Test
    public void givenMenuItemWhenBuildIdThenReturnCorrectId() {
        MenuItem rootMenu = new MenuItem("IMPLEMENTATION_WORKFLOW_EXECUTION", "IMPLEMENTATION", "Workflow Execution");
        String expectedRootMenuId = "IMPLEMENTATION__IMPLEMENTATION_WORKFLOW_EXECUTION";
        assertThat(DomainIdBuilder.buildDomainId(rootMenu)).isEqualTo(expectedRootMenuId);

        MenuItem childMenu = new MenuItem("IMPLEMENTATION", null, "Implementation");
        String expectedChildMenuId = "IMPLEMENTATION";
        assertThat(DomainIdBuilder.buildDomainId(childMenu)).isEqualTo(expectedChildMenuId);
    }

    @Test
    public void givenResourceServerWhenBuildIdThenReturnCorrectId() {
        ResourceServerDomain domain = new ResourceServerDomain(null, "containerHost", "127.0.0.1", "22", "root", "FBzGPMbCod8MXqoghHhOkA==", "docker", 1, "docker", "active", null, "Ben", null, null, null);
        String expectedId = "127.0.0.1__docker__containerHost";
        assertThat(DomainIdBuilder.buildDomainId(domain)).isEqualTo(expectedId);
    }

    @Test
    public void givenResourceItemWhenBuildIdThenReturnCorrectId() {
        ResourceServerDomain resourceServer = new ResourceServerDomain(null, "containerHost", "127.0.0.1", "22", "root", "FBzGPMbCod8MXqoghHhOkA==", "docker", 1, "docker", "active", null, "Ben", null, null, null);
        resourceServer.initId();
        ResourceItem domain = new ResourceItem(null, "service-mgmt-v1.8.4", "docker_container", "{\"volumeBindings\":\"/data/service-mgmt/log:/log\",\"imageName\":\"service-mgmt:v1.8.4\",\"portBindings\":\"20005:21000\",\"envVariables\":\"DB_HOST=127.0.0.1,DB_PORT=3306,DB_SCHEMA=service_mgmt,DB_USER=service_mgmt,DB_PWD=3284e2195ba5f03a,CORE_ADDR=http://111.230.161.237:19090/platform\",\"containerId\":\"4d660434213eb0e6f521416a557f8045e8337088838ecfa5aaeff40e0bf423d1\"}",
                "127.0.0.1__docker__containerHost", resourceServer, 1, "Create docker instance for plugin[service-mgmt]", "active", "Ben", null, null, null);

        String expectedRootMenuId = "127.0.0.1__docker__containerHost__docker_container__service-mgmt-v1.8.4";
        assertThat(DomainIdBuilder.buildDomainId(domain)).isEqualTo(expectedRootMenuId);
    }

}
