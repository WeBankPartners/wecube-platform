package com.webank.wecube.platform.core.service.plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.dto.QueryRequestDto;
import com.webank.wecube.platform.core.dto.plugin.PluginInstanceDto;
import com.webank.wecube.platform.core.entity.plugin.PluginInstances;
import com.webank.wecube.platform.core.entity.plugin.PluginPackages;
import com.webank.wecube.platform.core.entity.plugin.ResourceServer;
import com.webank.wecube.platform.core.repository.plugin.PluginInstancesMapper;
import com.webank.wecube.platform.core.repository.plugin.PluginPackagesMapper;
import com.webank.wecube.platform.core.repository.plugin.ResourceServerMapper;
import com.webank.wecube.platform.core.service.resource.ResourceItemType;
import com.webank.wecube.platform.core.service.resource.ResourceManagementService;
import com.webank.wecube.platform.core.service.resource.ResourceServerType;
import com.webank.wecube.platform.core.utils.StringUtilsEx;

@Service
public class PluginInstanceMgmtService extends AbstractPluginMgmtService {
    private static final Logger log = LoggerFactory.getLogger(PluginInstanceMgmtService.class);

    private static final int PLUGIN_DEFAULT_START_PORT = 20000;
    private static final int PLUGIN_DEFAULT_END_PORT = 30000;

    @Autowired
    private ResourceManagementService resourceManagementService;

    @Autowired
    private PluginPackagesMapper pluginPackagesMapper;

    @Autowired
    private PluginInstancesMapper pluginInstancesMapper;

    @Autowired
    private ResourceServerMapper resourceServerMapper;

    /**
     * 
     * @param pluginPackageId
     * @return
     */
    public List<PluginInstanceDto> getAvailableInstancesByPackageId(String pluginPackageId) {
        List<PluginInstances> instEntities = pluginInstancesMapper.selectAllByPluginPackageAndStatus(pluginPackageId,
                PluginInstances.CONTAINER_STATUS_RUNNING);

        List<PluginInstanceDto> resultDtos = new ArrayList<>();
        if (instEntities == null || instEntities.isEmpty()) {
            return resultDtos;
        }

        for (PluginInstances instEntity : instEntities) {
            PluginInstanceDto dto = buildPluginInstanceDto(instEntity);
            resultDtos.add(dto);

        }

        return resultDtos;
    }

    /**
     * 
     * @param pluginName
     * @return
     */
    public PluginInstances getRunningPluginInstance(String pluginName) {
        List<PluginInstances> instances = fetchRunningPluginInstances(pluginName);
        if (instances == null || instances.isEmpty()) {
            String errMsg = String.format("No instance for plugin [%s] is available.", pluginName);
            log.info(errMsg);
            throw new WecubeCoreException("3069", errMsg, pluginName);
        }

        if (instances.size() > 0) {
            return instances.get(0);
        }
        return null;
    }

    /**
     * 
     * @param pluginName
     * @return
     */
    public List<PluginInstances> getRunningPluginInstances(String pluginName) {
        List<PluginInstances> instances = fetchRunningPluginInstances(pluginName);
        if (instances == null || instances.isEmpty()) {
            String errMsg = String.format("No instance for plugin [%s] is available.", pluginName);
            log.info(errMsg);
            throw new WecubeCoreException("3069", errMsg, pluginName);
        }

        return instances;
    }

    /**
     * 
     * @return
     */
    public List<String> getAvailableContainerHosts() {
        QueryRequestDto queryRequest = QueryRequestDto.defaultQueryObject("type", ResourceServerType.DOCKER);
        List<String> hostList = new ArrayList<String>();
        resourceManagementService.retrieveServers(queryRequest).getContents().forEach(rs -> {
            hostList.add(rs.getHost());
        });
        return hostList;
    }

    // TODO
    /**
     * 
     * @param hostIp
     * @return
     */
    public Integer getAvailablePortByHostIp(String hostIp) {
        if (!(StringUtilsEx.isValidIp(hostIp))) {
            throw new WecubeCoreException("3066", "Invalid host ip.");
        }

        List<ResourceServer> resourceServerEntities = resourceServerMapper.selectAllByHostAndType(hostIp,
                ResourceServerType.DOCKER.getCode());
        if (resourceServerEntities == null || resourceServerEntities.isEmpty()) {
            throw new WecubeCoreException("3065", String.format("Host IP [%s] is not found", hostIp), hostIp);
        }

        ResourceServer resourceServer = resourceServerEntities.get(0);

        QueryRequestDto queryRequest = QueryRequestDto.defaultQueryObject("type", ResourceItemType.DOCKER_CONTAINER)
                .addEqualsFilter("resourceServerId", resourceServer.getId());

        List<Integer> hasUsedPorts = new ArrayList<>();
        resourceManagementService.retrieveItems(queryRequest).getContents().forEach(rs -> {
            Arrays.asList(rs.getAdditionalPropertiesMap().get("portBindings").split(",")).forEach(port -> {
                String[] portArray = port.split(":");
                hasUsedPorts.add(Integer.valueOf(portArray[0]));
            });
        });
        if (hasUsedPorts.size() == 0) {
            return PLUGIN_DEFAULT_START_PORT;
        }

        for (int i = PLUGIN_DEFAULT_START_PORT; i < PLUGIN_DEFAULT_END_PORT; i++) {
            if (!hasUsedPorts.contains(i)) {
                return i;
            }
        }
        throw new WecubeCoreException("3067", "There is no available ports in specified host");
    }

    private PluginInstanceDto buildPluginInstanceDto(PluginInstances entity) {
        PluginInstanceDto dto = new PluginInstanceDto();
        dto.setId(entity.getId());
        dto.setContainerName(entity.getContainerName());
        dto.setDockerInstanceResourceId(entity.getDockerInstanceResourceId());
        dto.setHost(entity.getHost());
        dto.setInstanceName(entity.getInstanceName());
        dto.setPackageId(entity.getPackageId());
        dto.setPluginMysqlInstanceResourceId(entity.getPluginMysqlInstanceResourceId());
        dto.setPort(entity.getPort());
        dto.setS3bucketResourceId(entity.getS3bucketResourceId());
        dto.setContainerStatus(entity.getContainerStatus());

        return dto;
    }

    private List<PluginInstances> fetchRunningPluginInstances(String pluginName) {
        List<PluginPackages> activePluginPackages = pluginPackagesMapper.selectAllByNameAndStatuses(pluginName,
                PluginPackages.PLUGIN_PACKAGE_ACTIVE_STATUSES);

        if (activePluginPackages == null || activePluginPackages.isEmpty()) {
            String errMsg = String.format("Plugin package [%s] not found.", pluginName);
            throw new WecubeCoreException("3068", errMsg, pluginName);
        }

        List<PluginInstances> runningInstances = new ArrayList<PluginInstances>();
        for (PluginPackages pkg : activePluginPackages) {
            List<PluginInstances> instances = pluginInstancesMapper.selectAllByPluginPackageAndStatus(pkg.getId(),
                    PluginInstances.CONTAINER_STATUS_RUNNING);
            if (instances != null) {
                runningInstances.addAll(instances);
            }

            if (runningInstances.size() > 0) {
                break;
            }
        }

        return runningInstances;
    }

}
