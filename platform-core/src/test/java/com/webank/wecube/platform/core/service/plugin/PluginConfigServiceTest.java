package com.webank.wecube.platform.core.service.plugin;

import static com.google.common.collect.Sets.newHashSet;
import static com.webank.wecube.platform.core.domain.plugin.PluginConfig.Status.DISABLED;
import static com.webank.wecube.platform.core.domain.plugin.PluginConfig.Status.ENABLED;
import static com.webank.wecube.platform.core.domain.plugin.PluginPackage.Status.DECOMMISSIONED;
import static com.webank.wecube.platform.core.domain.plugin.PluginPackage.Status.REGISTERED;
import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Lists;
import com.webank.wecube.platform.core.DatabaseBasedTest;
import com.webank.wecube.platform.core.domain.plugin.PluginConfig;
import com.webank.wecube.platform.core.domain.plugin.PluginConfigInterface;
import com.webank.wecube.platform.core.domain.plugin.PluginPackage;
import com.webank.wecube.platform.core.dto.plugin.PluginConfigInterfaceDto;
import com.webank.wecube.platform.core.jpa.PluginPackageRepository;

public class PluginConfigServiceTest extends DatabaseBasedTest {
    @Autowired
    private PluginPackageRepository packageRepository;

    @Autowired
    private PluginConfigService configService;

    @Ignore
    @Test
    public void givenMultiplePluginConfigWithDifferentStatusForMultipleVersionPackageWhenQueryAllLatestEnabledPluginConfigInterfaceThenShouldReturnCorrectInterfaces() {
        long now = System.currentTimeMillis();
        PluginPackage pluginPackage1 = new PluginPackage(null, "qcloud", "v1.0", DECOMMISSIONED, new Timestamp(now), false);
        PluginPackage pluginPackage2 = new PluginPackage(null, "qcloud", "v1.1", REGISTERED, new Timestamp(now + 10000), false);
        PluginPackage pluginPackage3 = new PluginPackage(null, "qcloud", "v1.2", REGISTERED, new Timestamp(now + 20000), false);

        PluginConfig pluginConfig10 = new PluginConfig(null, pluginPackage1, "subnet", null, null, DISABLED, null);
        PluginConfig pluginConfig11 = new PluginConfig(null, pluginPackage1, "vm", null, null, DISABLED, null);
        PluginConfig pluginConfig12 = new PluginConfig(null, pluginPackage1, "vpc", null, null, DISABLED, null);
        PluginConfig pluginConfig13 = new PluginConfig(null, pluginPackage1, "redis", null, null, DISABLED, null);
        pluginPackage1.setPluginConfigs(newHashSet(pluginConfig10, pluginConfig11, pluginConfig12, pluginConfig13));

        PluginConfigInterface pluginConfigInterface131 = new PluginConfigInterface(null, pluginConfig13, "create", "qcloud/redis/create", "qcloud/redis/create", "/qcloud/v1/redis/create", "POST", null, null);
        pluginConfig13.setInterfaces(newHashSet(pluginConfigInterface131));

        PluginConfig pluginConfig20 = new PluginConfig(null, pluginPackage2, "subnet", null, null, ENABLED, null);
        PluginConfig pluginConfig21 = new PluginConfig(null, pluginPackage2, "vm", null, null, ENABLED, null);
        pluginPackage2.setPluginConfigs(newHashSet(pluginConfig20, pluginConfig21));

        PluginConfigInterface pluginConfigInterface201 = new PluginConfigInterface(null, pluginConfig20, "create", "qcloud/subnet/create", "qcloud/subnet/create", "/qcloud/v1/subnet/create", "POST", null, null);
        PluginConfigInterface pluginConfigInterface202 = new PluginConfigInterface(null, pluginConfig20, "create-with-routetable", "qcloud/subnet/create-with-routetable", "qcloud/subnet/create-with-routetable", "/qcloud/v1/subnet/create-with-routetable", "POST", null, null);
        PluginConfigInterface pluginConfigInterface203 = new PluginConfigInterface(null, pluginConfig20, "terminate", "qcloud/subnet/terminate", "qcloud/subnet/terminate", "/qcloud/v1/subnet/terminate", "POST", null, null);
        pluginConfig20.setInterfaces(newHashSet(pluginConfigInterface201, pluginConfigInterface202, pluginConfigInterface203));


        PluginConfigInterface pluginConfigInterface211 = new PluginConfigInterface(null, pluginConfig21, "create", "qcloud/vm/create", "qcloud/vm/create", "/qcloud/v1/vm/create", "POST", null, null);
        PluginConfigInterface pluginConfigInterface212 = new PluginConfigInterface(null, pluginConfig21, "restart", "qcloud/vm/restart", "qcloud/vm/restart", "/qcloud/v1/vm/restart", "POST", null, null);
        PluginConfigInterface pluginConfigInterface213 = new PluginConfigInterface(null, pluginConfig21, "terminate", "qcloud/vm/terminate", "qcloud/vm/terminate", "/qcloud/v1/vm/terminate", "POST", null, null);
        pluginConfig21.setInterfaces(newHashSet(pluginConfigInterface211, pluginConfigInterface212, pluginConfigInterface213));

        PluginConfig pluginConfig30 = new PluginConfig(null, pluginPackage3, "subnet", null, null, ENABLED, null);
        PluginConfig pluginConfig31 = new PluginConfig(null, pluginPackage3, "vpc", null, null, ENABLED, null);
        pluginPackage3.setPluginConfigs(newHashSet(pluginConfig30, pluginConfig31));

        PluginConfigInterface pluginConfigInterface301 = new PluginConfigInterface(null, pluginConfig30, "create", "qcloud/subnet/create", "qcloud/subnet/create", "/qcloud/v1/subnet/create", "POST", null, null);
        PluginConfigInterface pluginConfigInterface302 = new PluginConfigInterface(null, pluginConfig30, "create-with-routetable", "qcloud/subnet/create-with-routetable", "qcloud/subnet/create-with-routetable", "/qcloud/v1/subnet/create-with-routetable", "POST", null, null);
        PluginConfigInterface pluginConfigInterface303 = new PluginConfigInterface(null, pluginConfig30, "terminate", "qcloud/subnet/terminate", "qcloud/subnet/terminate", "/qcloud/v1/subnet/terminate", "POST", null, null);
        pluginConfig30.setInterfaces(newHashSet(pluginConfigInterface301, pluginConfigInterface302, pluginConfigInterface303));

        PluginConfigInterface pluginConfigInterface311 = new PluginConfigInterface(null, pluginConfig31, "create", "qcloud/vpc/create", "qcloud/vpc/create", "/qcloud/v1/vpc/create", "POST", null, null);
        PluginConfigInterface pluginConfigInterface312 = new PluginConfigInterface(null, pluginConfig31, "restart", "qcloud/vpc/restart", "qcloud/vpc/restart", "/qcloud/v1/vpc/restart", "POST", null, null);
        PluginConfigInterface pluginConfigInterface313 = new PluginConfigInterface(null, pluginConfig31, "terminate", "qcloud/vpc/terminate", "qcloud/vpc/terminate", "/qcloud/v1/vpc/terminate", "POST", null, null);
        pluginConfig31.setInterfaces(newHashSet(pluginConfigInterface311, pluginConfigInterface312, pluginConfigInterface313));

        Iterable<PluginPackage> pluginPackages = packageRepository.saveAll(newHashSet(pluginPackage1, pluginPackage2, pluginPackage3));


        List<PluginConfigInterfaceDto> pluginConfigInterfaceDtos = configService.queryAllLatestEnabledPluginConfigInterface();
        assertThat(pluginConfigInterfaceDtos).hasSize(9);
        assertThat(pluginConfigInterfaceDtos.get(0).getId()).isEqualTo("qcloud__v1.1__vm__create");
        assertThat(pluginConfigInterfaceDtos.get(1).getId()).isEqualTo("qcloud__v1.1__vm__restart");
        assertThat(pluginConfigInterfaceDtos.get(2).getId()).isEqualTo("qcloud__v1.1__vm__terminate");
        assertThat(pluginConfigInterfaceDtos.get(3).getId()).isEqualTo("qcloud__v1.2__subnet__create");
        assertThat(pluginConfigInterfaceDtos.get(4).getId()).isEqualTo("qcloud__v1.2__subnet__create-with-routetable");
        assertThat(pluginConfigInterfaceDtos.get(5).getId()).isEqualTo("qcloud__v1.2__subnet__terminate");
        assertThat(pluginConfigInterfaceDtos.get(6).getId()).isEqualTo("qcloud__v1.2__vpc__create");
        assertThat(pluginConfigInterfaceDtos.get(7).getId()).isEqualTo("qcloud__v1.2__vpc__restart");
        assertThat(pluginConfigInterfaceDtos.get(8).getId()).isEqualTo("qcloud__v1.2__vpc__terminate");

    }

    @Test
    public void distinctPluginConfigInfDtoShouldSuccess() {
        PluginConfigInterfaceDto dto1 = new PluginConfigInterfaceDto("a", "pluginConfigId", "action", "serviceName1",
                "serviceDisplayName", "path", "httpMethod", null, null);
        PluginConfigInterfaceDto dto2 = new PluginConfigInterfaceDto("b", "pluginConfigId", "action", "serviceName2",
                "serviceDisplayName", "path", "httpMethod", null, null);
        PluginConfigInterfaceDto dto3 = new PluginConfigInterfaceDto("c", "pluginConfigId", "action", "serviceName3",
                "serviceDisplayName", "path", "httpMethod", null, null);
        PluginConfigInterfaceDto dto4 = new PluginConfigInterfaceDto("d", "pluginConfigId", "action", "serviceName1",
                "serviceDisplayName", "path", "httpMethod", null, null);
        PluginConfigInterfaceDto dto5 = new PluginConfigInterfaceDto("e", "pluginConfigId", "action", "serviceName5",
                "serviceDisplayName", "path", "httpMethod", null, null);
        List<PluginConfigInterfaceDto> list = Lists.newArrayList(dto1, dto2, dto3, dto4, dto5);
        List<PluginConfigInterfaceDto> retList = new ArrayList<PluginConfigInterfaceDto>();
        retList = configService.distinctPluginConfigInfDto(list);

        assertThat(retList.size()).isEqualTo(4);
        assertThat(retList).contains(dto1, dto2, dto3, dto5);
        assertThat(retList).doesNotContain(dto4);
    }
}