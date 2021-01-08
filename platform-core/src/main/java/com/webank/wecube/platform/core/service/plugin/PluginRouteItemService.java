package com.webank.wecube.platform.core.service.plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import com.webank.wecube.platform.core.dto.plugin.PluginRouteItemDto;
import com.webank.wecube.platform.core.entity.plugin.PluginInstances;
import com.webank.wecube.platform.core.entity.plugin.PluginInstancesInfo;
import com.webank.wecube.platform.core.entity.plugin.PluginPackages;
import com.webank.wecube.platform.core.entity.plugin.SimplePluginConfigInterfaceInfo;
import com.webank.wecube.platform.core.entity.plugin.SimplePluginPackageInfo;
import com.webank.wecube.platform.core.repository.plugin.PluginConfigInterfacesMapper;
import com.webank.wecube.platform.core.repository.plugin.PluginInstancesMapper;
import com.webank.wecube.platform.core.repository.plugin.PluginPackagesMapper;

@Service
public class PluginRouteItemService {
    private static final Logger log = LoggerFactory.getLogger(PluginRouteItemService.class);

    public static final String HTTP_SCHEME = "http";

    @Autowired
    private PluginInstancesMapper pluginInstancesMapper;

    @Autowired
    private PluginPackagesMapper pluginPackagesMapper;

    @Autowired
    private PluginConfigInterfacesMapper pluginConfigInterfacesMapper;

    public List<PluginRouteItemDto> getAllPluginRouteItems() {

        long startTime = System.currentTimeMillis();

        List<PluginRouteItemDto> resultList = new LinkedList<>();

        List<PluginInstancesInfo> pluginInstances = fetchRunningPluginInstanceInfo();

        // packageId -> PluginInstanceEntity
        Map<String, List<PluginInstancesInfo>> pluginInstanceMap = new HashMap<>();
        // 1 assemble default route for each context
        if (pluginInstances != null) {
            pluginInstances.forEach(pi -> {
                PluginRouteItemDto d = new PluginRouteItemDto();
                d.setHost(pi.getHost());
                d.setHttpScheme(HTTP_SCHEME);
                d.setPort(String.valueOf(pi.getPort()));
                d.setContext(pi.getPackageName());

                resultList.add(d);

                String packageId = pi.getPackageId();
                List<PluginInstancesInfo> pluginInstanceList = null;
                if (pluginInstanceMap.containsKey(packageId)) {
                    pluginInstanceList = pluginInstanceMap.get(packageId);
                } else {
                    pluginInstanceList = new ArrayList<>();
                    pluginInstanceMap.put(packageId, pluginInstanceList);
                }
                pluginInstanceList.add(pi);
            });
        }

        List<SimplePluginPackageInfo> pkgs = fetchAllActivePluginPackageEntities();
        Map<String, SimplePluginPackageInfo> latestActivePluginPackageMap = new HashMap<>();
        for (SimplePluginPackageInfo pkg : pkgs) {
            if (!latestActivePluginPackageMap.containsKey(pkg.getName())) {
                latestActivePluginPackageMap.put(pkg.getName(), pkg);
            }
        }

        // 2 assemble routes for each interface
        tryCalculateInterfaceRoutes(resultList, pluginInstanceMap, latestActivePluginPackageMap);

        if (log.isInfoEnabled()) {
            log.info("total {} routes got to push.", resultList.size());
        }

        long endTime = System.currentTimeMillis();

        log.info("total {} seconds elapsed", (endTime - startTime) / 1000);

        return resultList;
    }

    private List<SimplePluginPackageInfo> fetchAllActivePluginPackageEntities() {

        List<SimplePluginPackageInfo> infos = pluginPackagesMapper.selectAllActivePluginPackageEntities();

        if (infos == null) {
            infos = new ArrayList<>();
        }

        return infos;

    }

    private List<PluginInstancesInfo> fetchRunningPluginInstanceInfo() {

        List<PluginInstancesInfo> infos = pluginInstancesMapper.selectAllRunningPluginInstanceInfos();
        if (infos == null) {
            infos = new ArrayList<>();
        }

        return infos;
    }

    private void tryCalculateInterfaceRoutes(List<PluginRouteItemDto> resultList,
            Map<String, List<PluginInstancesInfo>> pluginInstanceMap,
            Map<String, SimplePluginPackageInfo> latestActivePluginPackageMap) {

        List<SimplePluginConfigInterfaceInfo> interfaces = fetchPluginConfigInterfaceInfo();

        if (interfaces == null || interfaces.isEmpty()) {
            return;
        }

        Object mapValue = new Object();
        Map<String, Object> calculatedServiceNames = new HashMap<String, Object>();

        for (SimplePluginConfigInterfaceInfo intf : interfaces) {
            if (intf == null) {
                continue;
            }

            if (StringUtils.isBlank(intf.getServiceName())) {
                continue;
            }

            if (calculatedServiceNames.containsKey(intf.getServiceName())) {
                continue;
            }

            List<PluginRouteItemDto> routeItems = tryCalculatePluginRouteItem(intf, pluginInstanceMap,
                    latestActivePluginPackageMap);
            if (!routeItems.isEmpty()) {
                resultList.addAll(routeItems);
                calculatedServiceNames.put(intf.getServiceName(), mapValue);
            }
        }
    }

    private List<SimplePluginConfigInterfaceInfo> fetchPluginConfigInterfaceInfo() {
        List<SimplePluginConfigInterfaceInfo> infos = pluginConfigInterfacesMapper
                .selectAllPluginConfigInterfaceInfos();
        if (infos == null) {
            infos = new ArrayList<>();
        }

        return infos;

    }

    private List<PluginRouteItemDto> tryCalculatePluginRouteItem(SimplePluginConfigInterfaceInfo intf,
            Map<String, List<PluginInstancesInfo>> packageId2PluginInstances,
            Map<String, SimplePluginPackageInfo> latestActivePluginPackageMap) {
        String packageName = intf.getPackageName();

        List<PluginRouteItemDto> routeItems = new LinkedList<>();
        SimplePluginPackageInfo latestActivePluginPackage = latestActivePluginPackageMap.get(packageName);
        if (latestActivePluginPackage == null) {
            return routeItems;
        }

        List<PluginInstancesInfo> pluginInstances = packageId2PluginInstances.get(latestActivePluginPackage.getId());
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

        for (PluginInstancesInfo pluginInstance : pluginInstances) {

            if (!validateKeyRouteProperties(intf, pluginInstance)) {
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

    private boolean validateKeyRouteProperties(SimplePluginConfigInterfaceInfo intf,
            PluginInstancesInfo pluginInstance) {
        if (StringUtils.isBlank(intf.getPath())) {
            return false;
        }

        if (StringUtils.isBlank(pluginInstance.getHost())) {
            return false;
        }

        if (pluginInstance.getPort() == null) {
            return false;
        }

        return true;
    }

    public List<PluginInstances> getRunningPluginInstances(String pluginName) {
        List<PluginPackages> activePluginPackages = pluginPackagesMapper.selectAllByNameAndStatuses(pluginName,
                PluginPackages.PLUGIN_PACKAGE_ACTIVE_STATUSES);
        if (activePluginPackages == null || activePluginPackages.isEmpty()) {
            log.info("Plugin package [{}] not found.", pluginName);
            return null;
        }
        List<PluginInstances> runningInstances = new ArrayList<PluginInstances>();
        for (PluginPackages pkg : activePluginPackages) {
            List<PluginInstances> instances = pluginInstancesMapper.selectAllByPluginPackageAndStatus(pkg.getId(),
                    PluginInstances.CONTAINER_STATUS_RUNNING);
            if (instances != null && (!instances.isEmpty())) {
                runningInstances.addAll(instances);
            }

            if (runningInstances.size() > 0) {
                break;
            }
        }
        if (runningInstances.isEmpty()) {
            log.info("No instance for plugin [{}] is available.", pluginName);
        }
        return runningInstances;
    }

    public List<PluginRouteItemDto> getPluginRouteItemsByName(String name) {

        List<PluginRouteItemDto> dtos = new ArrayList<>();

        List<PluginInstances> pluginInstances = pluginInstancesMapper
                .selectAllByContainerStatusAndInstanceName(PluginInstances.CONTAINER_STATUS_RUNNING, name);

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
