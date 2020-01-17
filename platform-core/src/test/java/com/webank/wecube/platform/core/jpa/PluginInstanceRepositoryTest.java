package com.webank.wecube.platform.core.jpa;

import com.webank.wecube.platform.core.DatabaseBasedTest;
import com.webank.wecube.platform.core.domain.plugin.PluginInstance;
import com.webank.wecube.platform.core.domain.plugin.PluginPackage;
import com.webank.wecube.platform.core.domain.plugin.PluginPackageMenu;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Timestamp;
import java.util.Optional;

import static com.webank.wecube.platform.core.domain.plugin.PluginPackage.Status.REGISTERED;
import static org.assertj.core.api.Assertions.*;

public class PluginInstanceRepositoryTest extends DatabaseBasedTest {
    @Autowired
    private PluginPackageRepository packageRepository;

    @Autowired
    private PluginInstanceRepository instanceRepository;

    @Autowired
    private PluginPackageMenuRepository menuRepository;

    @Test
    public void givenPluginPackageMenuWhenRelatedPluginInstanceInsertedForPackageThenMenuStatusShouldBeUpdatedToActive() {
        PluginPackage registeredPluginPackage       = new PluginPackage(null, "wecmdb", "v0.2", REGISTERED, new Timestamp(System.currentTimeMillis()), false);
        packageRepository.save(registeredPluginPackage);

        PluginPackageMenu menu = new PluginPackageMenu(registeredPluginPackage, "ADMIN_CMDB_MODEL_MANAGEMENT", "ADMIN", "CMDB Model Management", "/wecmdb/admin/cmdb-model-management");
        PluginPackageMenu savedPluginPackageMenu = menuRepository.save(menu);

        assertThat(savedPluginPackageMenu).isNotNull();
        assertThat(savedPluginPackageMenu.getId()).isNotNull();
        String menuId = savedPluginPackageMenu.getId();
        assertThat(savedPluginPackageMenu.isActive()).isFalse();

        PluginInstance pluginInstance = new PluginInstance(null, registeredPluginPackage, "wecmdb", "wecmdb-v0.2", "127.0.0.1", 20002);
        instanceRepository.save(pluginInstance);

        Optional<PluginPackageMenu> optionalLatestPluginPackageMenu = menuRepository.findById(menuId);
        assertThat(optionalLatestPluginPackageMenu.isPresent()).isTrue();

        assertThat(optionalLatestPluginPackageMenu.get().isActive()).isTrue();
    }

    @Test
    public void givenPluginInstanceRunningAndPluginPackageMenuActiveWhenPluginInstanceDestroyForPackageThenMenuStatusShouldBeUpdatedToInactive() {
        PluginPackage registeredPluginPackage       = new PluginPackage(null, "wecmdb", "v0.2", REGISTERED, new Timestamp(System.currentTimeMillis()), false);
        packageRepository.save(registeredPluginPackage);

        PluginPackageMenu menu = new PluginPackageMenu(registeredPluginPackage, "ADMIN_CMDB_MODEL_MANAGEMENT", "ADMIN", "CMDB Model Management", "/wecmdb/admin/cmdb-model-management");
        PluginPackageMenu packageMenu = menuRepository.save(menu);
        assertThat(packageMenu).isNotNull();
        String menuId = packageMenu.getId();

        PluginInstance pluginInstance = new PluginInstance(null, registeredPluginPackage, "wecmdb", "wecmdb-v0.2", "127.0.0.1", 20002);
        PluginInstance savedPluginInstance = instanceRepository.save(pluginInstance);
        assertThat(savedPluginInstance).isNotNull();
        assertThat(savedPluginInstance.getId()).isNotNull();

        String instanceId = savedPluginInstance.getId();

        Optional<PluginPackageMenu> pluginPackageMenuOptional = menuRepository.findById(menuId);
        assertThat(pluginPackageMenuOptional.isPresent()).isTrue();
        PluginPackageMenu updatedPluginPackageMenu = pluginPackageMenuOptional.get();
        assertThat(updatedPluginPackageMenu).isNotNull();
        assertThat(updatedPluginPackageMenu.getId()).isNotNull();

        assertThat(updatedPluginPackageMenu.isActive()).isTrue();


        instanceRepository.deleteById(instanceId);

        assertThat(instanceRepository.existsById(instanceId)).isFalse();

        Optional<PluginPackageMenu> optionalLatestPluginPackageMenu = menuRepository.findById(menuId);
        assertThat(optionalLatestPluginPackageMenu.isPresent()).isTrue();

        assertThat(optionalLatestPluginPackageMenu.get().isActive()).isFalse();
    }
}