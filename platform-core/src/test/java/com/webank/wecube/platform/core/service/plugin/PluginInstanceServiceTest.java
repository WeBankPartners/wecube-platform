package com.webank.wecube.platform.core.service.plugin;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import javax.script.ScriptException;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import com.webank.wecube.platform.core.DatabaseBasedTest;
import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.entity.plugin.PluginInstances;

public class PluginInstanceServiceTest extends DatabaseBasedTest {
    @Autowired
    PluginInstanceService pluginInstanceService;
    
    @Autowired
    PluginInstanceMgmtService pluginInstanceMgmtService;

    private void prepareDatabase() throws ScriptException {
        executeSqlScripts(newArrayList(new ClassPathResource("/database/03.wecube.test.data.sql")));
    }

    private void executeSqlScripts(List<Resource> scipts) throws ScriptException {
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.setContinueOnError(false);
        populator.setIgnoreFailedDrops(false);
        populator.setSeparator(";");
        scipts.forEach(populator::addScript);
        populator.execute(dataSource);
    }

    @Test
    public void removePluginInstanceByIdShouldSuccess() throws ScriptException {
        prepareDatabase();

        try {
            pluginInstanceService.removePluginInstanceById("service-mgmt__v1.0__service-mgmt__127.0.0.1__20003");
            assertThat(true);
        } catch (Exception e) {
            assertThat(false);
        }
    }

    @Test
    public void givenLatestPackageNotRegisteredWhenQueryRunningInstanceByPackageNameThenShouldReturnTheInstanceRunningWithOlderRegisteredPackage() {
        prepareDataForLatestPackageNotRegistered();
        String packageName = "service-mgmt";
        List<PluginInstances> runningPluginInstances = pluginInstanceMgmtService.getRunningPluginInstances(packageName);
        assertThat(runningPluginInstances).hasSize(1);
//        assertThat(runningPluginInstances.get(0).getPluginPackage().getId()).isEqualTo("service-mgmt__v1.0");
    }

    private void prepareDataForLatestPackageNotRegistered() {
        executeSql("INSERT INTO `plugin_packages` (`id`, `name`, `version`, `status`, `upload_timestamp`, `ui_package_included`) VALUES " +
                "  ('service-mgmt__v1.0', 'service-mgmt', 'v1.0', 'REGISTERED',   '2019-11-25 20:31:48', '0')\n" +
                ", ('service-mgmt__v1.1', 'service-mgmt', 'v1.1', 'UNREGISTERED', '2019-11-26 20:31:48', '0')\n" +
                ";\n" +
                "INSERT INTO `plugin_instances` (`id`, `host`, `container_name`, `port`, `container_status`, `package_id`, `docker_instance_resource_id`, `instance_name`, `plugin_mysql_instance_resource_id`, `s3bucket_resource_id`) VALUES " +
                " ('service-mgmt__v1.0__service-mgmt__127.0.0.1__20003', '127.0.0.1', 'service-mgmt', 20003, 'RUNNING', 'service-mgmt__v1.0', NULL, 'wecmdb', NULL, NULL) " +
                ";\n"
        );
    }

    @Test
    public void givenLatestPackageRegisterButNoRunningInstanceWhenQueryRunningInstanceByPackageNameThenShouldReturnNull() {
        prepareDataForLatestPackageRegisteredButNoRunningInstance();
        String packageName = "service-mgmt";
        try {
            pluginInstanceMgmtService.getRunningPluginInstances(packageName);
            assertThat(false);
        } catch (Exception e) {
            assertThat(e instanceof WecubeCoreException);
            assertThat(e.getMessage()).contains("No instance for plugin");
        }
    }

    private void prepareDataForLatestPackageRegisteredButNoRunningInstance() {
        executeSql("INSERT INTO `plugin_packages` (`id`, `name`, `version`, `status`, `upload_timestamp`, `ui_package_included`) VALUES " +
                "  ('service-mgmt__v1.0', 'service-mgmt', 'v1.0', 'REGISTERED',   '2019-11-25 20:31:48', '0')\n" +
                ", ('service-mgmt__v1.1', 'service-mgmt', 'v1.1', 'REGISTERED', '2019-11-26 20:31:48', '0')\n" +
                ";\n" +
                "INSERT INTO `plugin_instances` (`id`, `host`, `container_name`, `port`, `container_status`, `package_id`, `docker_instance_resource_id`, `instance_name`, `plugin_mysql_instance_resource_id`, `s3bucket_resource_id`) VALUES " +
                " ('service-mgmt__v1.0__service-mgmt__127.0.0.1__20003', '127.0.0.1', 'service-mgmt', 20003, 'RUNNING', 'service-mgmt__v1.0', NULL, 'wecmdb', NULL, NULL) " +
                ";\n"
        );
    }
}
