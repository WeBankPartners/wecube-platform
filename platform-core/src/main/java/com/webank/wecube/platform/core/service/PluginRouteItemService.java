package com.webank.wecube.platform.core.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import com.webank.wecube.platform.core.domain.plugin.PluginConfig;
import com.webank.wecube.platform.core.domain.plugin.PluginConfigInterface;
import com.webank.wecube.platform.core.domain.plugin.PluginInstance;
import com.webank.wecube.platform.core.domain.plugin.PluginPackage;
import com.webank.wecube.platform.core.dto.PluginRouteItemDto;
import com.webank.wecube.platform.core.jpa.PluginConfigInterfaceRepository;
import com.webank.wecube.platform.core.jpa.PluginInstanceRepository;
import com.webank.wecube.platform.core.jpa.PluginPackageRepository;

@Service
public class PluginRouteItemService {
    private static final Logger log = LoggerFactory.getLogger(PluginRouteItemService.class);

    public static final String HTTP_SCHEME = "http";

    @Autowired
    private PluginInstanceRepository pluginInstanceRepository;

    @Autowired
    private PluginConfigInterfaceRepository pluginConfigInterfaceRepository;

    @Autowired
    private PluginPackageRepository pluginPackageRepository;

    public List<PluginRouteItemDto> getAllPluginRouteItems() {

        List<PluginRouteItemDto> resultList = new LinkedList<>();

        List<PluginInstance> pluginInstances = pluginInstanceRepository
                .findAllByContainerStatus(PluginInstance.CONTAINER_STATUS_RUNNING);

        // 1 assemble default route for each context
        if (pluginInstances != null) {
            pluginInstances.forEach(pi -> {
                PluginRouteItemDto d = new PluginRouteItemDto();
                d.setHost(pi.getHost());
                d.setHttpScheme(HTTP_SCHEME);
                d.setPort(String.valueOf(pi.getPort()));
                d.setContext(pi.getPluginPackage().getName());

                resultList.add(d);
            });
        }

        // 2 assemble routes for each interface
        tryCalculateInterfaceRoutes(resultList);

        if (log.isInfoEnabled()) {
            log.info("total {} routes got to push.", resultList.size());
        }

        return resultList;
    }

    private void tryCalculateInterfaceRoutes(List<PluginRouteItemDto> resultList) {

        List<PluginConfigInterface> interfaces = pluginConfigInterfaceRepository.findAllEnabledInterfaces();
        if (interfaces == null || interfaces.isEmpty()) {
            return;
        }

        Object mapValue = new Object();
        Map<String, Object> calculatedServiceNames = new HashMap<String, Object>();

        for (PluginConfigInterface intf : interfaces) {
            if (intf == null) {
                continue;
            }

            if (StringUtils.isBlank(intf.getServiceName())) {
                continue;
            }

            if (calculatedServiceNames.containsKey(intf.getServiceName())) {
                continue;
            }

            List<PluginRouteItemDto> routeItems = tryCalculatePluginRouteItem(intf);
            if(!routeItems.isEmpty()){
                resultList.addAll(routeItems);
                calculatedServiceNames.put(intf.getServiceName(), mapValue);
            }
        }

        return;
    }

    private List<PluginRouteItemDto> tryCalculatePluginRouteItem(PluginConfigInterface intf) {
        PluginConfig pluginConfig = intf.getPluginConfig();
        PluginPackage pluginPackage = pluginConfig.getPluginPackage();
        String packageName = pluginPackage.getName();

        List<PluginRouteItemDto> routeItems = new LinkedList<>();
        List<PluginInstance> pluginInstances = getRunningPluginInstances(packageName);
        if (pluginInstances == null) {
            return routeItems;
        }

        if (StringUtils.isBlank(intf.getPath())) {
            return routeItems;
        }

        String httpMethod = intf.getHttpMethod();
        if (StringUtils.isBlank(httpMethod)) {
            httpMethod = HttpMethod.POST.name();
        }

        String path = intf.getPath();
        if (!path.startsWith("/")) {
            path = "/" + path;
        }

        for (PluginInstance pluginInstance : pluginInstances) {
            
            if(!validateKeyRouteProperties(intf, pluginInstance)){
                log.info("such route is invalid,service name={}", intf.getServiceName());
            }
            
            PluginRouteItemDto routeItem = new PluginRouteItemDto();
            routeItem.setContext(packageName);
            routeItem.setHttpMethod(httpMethod);
            routeItem.setPath(path);
            routeItem.setHost(pluginInstance.getHost());
            routeItem.setHttpScheme(HTTP_SCHEME);
            routeItem.setPort(String.valueOf(pluginInstance.getPort()));

            routeItem.setWeight("0");
            
            routeItems.add(routeItem);
        }

        return routeItems;
    }

    private boolean validateKeyRouteProperties(PluginConfigInterface intf,PluginInstance pluginInstance){
        if(StringUtils.isBlank(intf.getPath())){
            return false;
        }
        
        if(StringUtils.isBlank(pluginInstance.getHost())){
            return false;
        }
        
        if(pluginInstance.getPort() == null){
            return false;
        }
        
        return true;
    }

    public List<PluginInstance> getRunningPluginInstances(String pluginName) {
        Optional<PluginPackage> pkg = pluginPackageRepository.findLatestActiveVersionByName(pluginName);
        if (!pkg.isPresent()) {
            log.info("Plugin package [{}] not found.", pluginName);
            return null;
        }

        List<PluginInstance> instances = pluginInstanceRepository
                .findByContainerStatusAndPluginPackage_Id(PluginInstance.CONTAINER_STATUS_RUNNING, pkg.get().getId());
        if (instances == null || instances.size() == 0) {
            log.info("No instance for plugin [{}] is available.", pluginName);
        }
        return instances;
    }

    public List<PluginRouteItemDto> getPluginRouteItemsByName(String name) {

        List<PluginRouteItemDto> dtos = new ArrayList<>();

        List<PluginInstance> pluginInstances = pluginInstanceRepository
                .findAllByContainerStatusAndInstanceName(PluginInstance.CONTAINER_STATUS_RUNNING, name);

        if (pluginInstances != null) {
            pluginInstances.forEach(pi -> {
                PluginRouteItemDto d = new PluginRouteItemDto();
                d.setHost(pi.getHost());
                d.setHttpScheme(HTTP_SCHEME);
                d.setPort(String.valueOf(pi.getPort()));
                d.setContext(pi.getInstanceName());

                dtos.add(d);
            });
        }

        return dtos;
    }

}
