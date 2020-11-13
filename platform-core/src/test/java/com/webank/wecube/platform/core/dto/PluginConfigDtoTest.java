package com.webank.wecube.platform.core.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.webank.wecube.platform.core.dto.plugin.PluginConfigDto;

public class PluginConfigDtoTest {

    @Test
    public void stringTestCase() {
        PluginConfigDto dto = new PluginConfigDto("id", "packageId", "name", "wecmdb:resource_instance{type eq 'vm'}",
                "status", null);

        assertThat(dto.getTargetPackage()).isEqualTo("wecmdb");
        assertThat(dto.getTargetEntity()).isEqualTo("resource_instance");
        assertThat(dto.getFilterRule()).isEqualTo("{type eq 'vm'}");
        
        PluginConfigDto dto2 = new PluginConfigDto("id", "packageId", "name", "wecmdb:resource_instance",
                "status", null);

        assertThat(dto2.getTargetPackage()).isEqualTo("wecmdb");
        assertThat(dto2.getTargetEntity()).isEqualTo("resource_instance");
        assertThat(dto2.getFilterRule()).isEqualTo("");
        
        PluginConfigDto dto3 = new PluginConfigDto("id", "packageId", "name", "wecmdb",
                "status", null);

        assertThat(dto3.getTargetPackage()).isEqualTo("wecmdb");
        assertThat(dto3.getTargetEntity()).isEqualTo("");
        assertThat(dto3.getFilterRule()).isEqualTo("");
    }
}
