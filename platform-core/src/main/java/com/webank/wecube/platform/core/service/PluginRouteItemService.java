package com.webank.wecube.platform.core.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.webank.wecube.platform.core.domain.plugin.PluginInstance;
import com.webank.wecube.platform.core.dto.PluginRouteItemDto;
import com.webank.wecube.platform.core.jpa.PluginInstanceRepository;

@Service
public class PluginRouteItemService {

    @Autowired
    private PluginInstanceRepository pluginInstanceRepository;

    public List<PluginRouteItemDto> getAllPluginRouteItems() {

        List<PluginRouteItemDto> dtos = new ArrayList<>();

        List<PluginInstance> pluginInstances = pluginInstanceRepository.findAllByContainerStatus(PluginInstance.CONTAINER_STATUS_RUNNING);

        if (pluginInstances != null) {
            pluginInstances.forEach(pi -> {
                PluginRouteItemDto d = new PluginRouteItemDto();
                d.setHost(pi.getHost());
                d.setSchema("http");
                d.setPort(String.valueOf(pi.getPort()));
                d.setName(pi.getInstanceName());

                dtos.add(d);
            });
        }

        return dtos;
    }

    public List<PluginRouteItemDto> getPluginRouteItemsByName(String name) {

        List<PluginRouteItemDto> dtos = new ArrayList<>();

        List<PluginInstance> pluginInstances = pluginInstanceRepository
                .findAllByContainerStatusAndInstanceName(PluginInstance.CONTAINER_STATUS_RUNNING, name);

        if (pluginInstances != null) {
            pluginInstances.forEach(pi -> {
                PluginRouteItemDto d = new PluginRouteItemDto();
                d.setHost(pi.getHost());
                d.setSchema("http");
                d.setPort(String.valueOf(pi.getPort()));
                d.setName(pi.getInstanceName());

                dtos.add(d);
            });
        }

        return dtos;
    }

}
