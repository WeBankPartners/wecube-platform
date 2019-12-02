package com.webank.wecube.platform.core.jpa;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.webank.wecube.platform.core.DatabaseBasedTest;
import com.webank.wecube.platform.core.domain.plugin.PluginPackage;
import com.webank.wecube.platform.core.domain.plugin.PluginPackageMenu;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.webank.wecube.platform.core.domain.plugin.PluginPackage.Status.*;
import static com.webank.wecube.platform.core.domain.MenuItem.Source.*;
import static org.assertj.core.api.Assertions.assertThat;

public class PluginPackageMenuRepositoryTest extends DatabaseBasedTest {
    @Autowired
    private PluginPackageRepository packageRepository;
    @Autowired
    private PluginPackageMenuRepository menuRepository;

    @Test
    public void givenMultipleVersionPackagesWithMenuWhenFindAllForAllActivePackagesThenReturnOnlyMenusForActivePackages() {

        long now = System.currentTimeMillis();
        PluginPackage unregisteredPluginPackage     = new PluginPackage(null, "wecmdb", "v0.1", UNREGISTERED, new Timestamp(now), false);
        PluginPackage registeredPluginPackage       = new PluginPackage(null, "wecmdb", "v0.2", REGISTERED, new Timestamp(now + 10000), false);
        PluginPackage runningPluginPackage          = new PluginPackage(null, "wecmdb", "v0.3", RUNNING, new Timestamp(now + 20000), false);
        PluginPackage stoppedPluginPackage          = new PluginPackage(null, "wecmdb", "v0.4", STOPPED, new Timestamp(now + 30000), false);
        PluginPackage decommissionedPluginPackage   = new PluginPackage(null, "wecmdb", "v0.5", DECOMMISSIONED, new Timestamp(now + 40000), false);

        PluginPackageMenu packageMenuForUnregistered    = new PluginPackageMenu(unregisteredPluginPackage, "DESIGNING_CI_DATA_ENQUIRY", "DESIGNING", "CI Data Enquiry", "/wecmdb/designing/ci-data-enquiry");
        PluginPackageMenu packageMenuForRegistered      = new PluginPackageMenu(registeredPluginPackage, "DESIGNING_CI_INTEGRATED_QUERY_EXECUTION", "DESIGNING", "CI Integrated Enquiry", "/wecmdb/designing/ci-integrated-query-execution");
        PluginPackageMenu packageMenuForRunning         = new PluginPackageMenu(runningPluginPackage, "CMDB_DESIGNING_ENUM_ENQUIRY", "DESIGNING", "Enum Enquiry", "/wecmdb/designing/enum-enquiry");
        PluginPackageMenu packageMenuForStopped         = new PluginPackageMenu(stoppedPluginPackage, "DESIGNING_CI_INTEGRATED_QUERY_MANAGEMENT", "DESIGNING", "CI Integrated Enquiry Management", "/wecmdb/designing/ci-integrated-query-management");
        PluginPackageMenu packageMenuForDecommissioned  = new PluginPackageMenu(decommissionedPluginPackage, "DESIGNING_CI_DATA_MANAGEMENT", "DESIGNING", "CI Data Management", "/wecmdb/designing/ci-data-management");

        unregisteredPluginPackage.setPluginPackageMenus(Sets.newHashSet(packageMenuForUnregistered));
        registeredPluginPackage.setPluginPackageMenus(Sets.newHashSet(packageMenuForRegistered));
        runningPluginPackage.setPluginPackageMenus(Sets.newHashSet(packageMenuForRunning));
        stoppedPluginPackage.setPluginPackageMenus(Sets.newHashSet(packageMenuForStopped));
        decommissionedPluginPackage.setPluginPackageMenus(Sets.newHashSet(packageMenuForDecommissioned));

        packageRepository.saveAll(Lists.newArrayList(unregisteredPluginPackage, registeredPluginPackage, runningPluginPackage, stoppedPluginPackage, decommissionedPluginPackage));

        Iterable<PluginPackageMenu> menuIterable = menuRepository.findAll();
        assertThat(menuIterable).hasSize(5);

        Optional<List<PluginPackageMenu>> allMenusForAllActivePackages = menuRepository.findAllForAllActivePackages();
        assertThat(allMenusForAllActivePackages.isPresent()).isTrue();
        List<PluginPackageMenu> pluginPackageMenus = allMenusForAllActivePackages.get();
        assertThat(pluginPackageMenus).hasSize(3);

    }

    @Test
    public void givenMultipleVersionMenuWhenFindAndMergePluginMenusThenReturnMergedMenus() {

        long now = System.currentTimeMillis();
        PluginPackage unregisteredPluginPackage = new PluginPackage(null, "wecmdb", "v0.1", UNREGISTERED, new Timestamp(now), false);
        PluginPackage runningPluginPackage = new PluginPackage(null, "wecmdb", "v0.3", RUNNING, new Timestamp(now + 20000), false);
        PluginPackage stoppedPluginPackage = new PluginPackage(null, "wecmdb", "v0.4", STOPPED, new Timestamp(now + 30000), false);
        PluginPackage decommissionedPluginPackage = new PluginPackage(null, "wecmdb", "v0.5", DECOMMISSIONED, new Timestamp(now + 40000), false);

        PluginPackageMenu packageMenuForUnregistered = new PluginPackageMenu(unregisteredPluginPackage, "DESIGNING_CI_DATA_ENQUIRY", "DESIGNING", "CI Data Enquiry", "/wecmdb/designing/ci-data-enquiry");
        PluginPackageMenu packageMenuForRunning1 = new PluginPackageMenu(runningPluginPackage, "DESIGNING_CI_INTEGRATED_QUERY_EXECUTION", "DESIGNING", "CI Integrated Enquiry", "/wecmdb/designing/ci-integrated-query-execution");
        String cmdb_designing_enum_enquiry = "CMDB_DESIGNING_ENUM_ENQUIRY";
        PluginPackageMenu packageMenuForRunning2 = new PluginPackageMenu(runningPluginPackage, cmdb_designing_enum_enquiry, "DESIGNING", "Enum Enquiry", "/wecmdb/designing/enum-enquiry");
        String newPath = "/wecmdb/designing/enum-enquiry2";
        PluginPackageMenu packageMenuForStopped1 = new PluginPackageMenu(stoppedPluginPackage, cmdb_designing_enum_enquiry, "DESIGNING", "Enum Enquiry", newPath);
        PluginPackageMenu packageMenuForStopped2 = new PluginPackageMenu(stoppedPluginPackage, "DESIGNING_CI_INTEGRATED_QUERY_MANAGEMENT", "DESIGNING", "CI Integrated Enquiry Management", "/wecmdb/designing/ci-integrated-query-management");
        PluginPackageMenu packageMenuForDecommissioned = new PluginPackageMenu(decommissionedPluginPackage, "DESIGNING_CI_DATA_MANAGEMENT", "DESIGNING", "CI Data Management", "/wecmdb/designing/ci-data-management");

        unregisteredPluginPackage.setPluginPackageMenus(Sets.newHashSet(packageMenuForUnregistered));
        runningPluginPackage.setPluginPackageMenus(Sets.newHashSet(packageMenuForRunning1, packageMenuForRunning2));
        stoppedPluginPackage.setPluginPackageMenus(Sets.newHashSet(packageMenuForStopped1, packageMenuForStopped2));
        decommissionedPluginPackage.setPluginPackageMenus(Sets.newHashSet(packageMenuForDecommissioned));

        packageRepository.save(unregisteredPluginPackage);
        packageRepository.save(runningPluginPackage);
        packageRepository.save(stoppedPluginPackage);
        packageRepository.save(decommissionedPluginPackage);

        Iterable<PluginPackageMenu> menuIterable = menuRepository.findAll();
        assertThat(menuIterable).hasSize(6);

        Optional<List<PluginPackageMenu>> allMenusForAllActivePackages = menuRepository.findAllForAllActivePackages();
        assertThat(allMenusForAllActivePackages.isPresent()).isTrue();
        List<PluginPackageMenu> pluginPackageMenus = allMenusForAllActivePackages.get();
        assertThat(pluginPackageMenus).hasSize(4);

        Optional<List<PluginPackageMenu>> mergedPluginMenusOptional = menuRepository.findAndMergePluginMenus();
        assertThat(mergedPluginMenusOptional.isPresent()).isTrue();
        List<PluginPackageMenu> mergedPluginMenus = mergedPluginMenusOptional.get();
        assertThat(mergedPluginMenus).hasSize(3);

        Map<String, PluginPackageMenu> codeOrderMap = mergedPluginMenus.stream().collect(Collectors.toMap(it -> it.getCode(), it -> it));
        assertThat(codeOrderMap).isNotNull();
        PluginPackageMenu cmdbDesigningEnumEnquiry = codeOrderMap.get(cmdb_designing_enum_enquiry);
        assertThat(cmdbDesigningEnumEnquiry).isNotNull();
        assertThat(cmdbDesigningEnumEnquiry.getPath()).isEqualTo(newPath);
        assertThat(cmdbDesigningEnumEnquiry.getPluginPackage()).isNotNull();
        assertThat(cmdbDesigningEnumEnquiry.getPluginPackage().getVersion()).isEqualTo("v0.4");
    }
}