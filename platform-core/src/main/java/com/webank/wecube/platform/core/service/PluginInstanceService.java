package com.webank.wecube.platform.core.service;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;
import com.webank.wecube.platform.core.support.S3Client;
import com.webank.wecube.platform.core.utils.EncryptionUtils;
import com.webank.wecube.platform.core.utils.JsonUtils;

import javassist.expr.NewArray;

import com.webank.wecube.platform.core.service.ScpService;
import com.webank.wecube.platform.core.service.CommandService;
import com.webank.wecube.platform.core.commons.ApplicationProperties.ResourceProperties;
import com.webank.wecube.platform.core.commons.ApplicationProperties;
import com.webank.wecube.platform.core.commons.ApplicationProperties.PluginProperties;
import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.domain.ResourceServer;
import com.webank.wecube.platform.core.domain.plugin.PluginInstance;
import com.webank.wecube.platform.core.domain.plugin.PluginPackage;
import com.webank.wecube.platform.core.domain.plugin.PluginPackageRuntimeResourcesMysql;
import com.webank.wecube.platform.core.domain.plugin.PluginPackageRuntimeResourcesS3;
import com.webank.wecube.platform.core.dto.QueryRequest;
import com.webank.wecube.platform.core.dto.ResourceItemDto;
import com.webank.wecube.platform.core.jpa.PluginConfigRepository;
import com.webank.wecube.platform.core.jpa.PluginInstanceRepository;
import com.webank.wecube.platform.core.jpa.PluginPackageRepository;
import com.webank.wecube.platform.core.jpa.ResourceServerRepository;
import com.webank.wecube.platform.core.service.resource.ResourceItemType;
import com.webank.wecube.platform.core.service.resource.ResourceManagementService;
import com.webank.wecube.platform.core.service.resource.ResourceServerType;

import static org.apache.commons.lang3.StringUtils.trim;

@Service
@Transactional
public class PluginInstanceService {
    private static final Logger logger = LoggerFactory.getLogger(PluginPackageDataModelServiceImpl.class);

    @Autowired
    private PluginProperties pluginProperties;

    @Autowired
    PluginInstanceRepository pluginInstanceRepository;
    @Autowired
    PluginPackageRepository pluginPackageRepository;
    @Autowired
    PluginConfigRepository pluginConfigRepository;
    @Autowired
    ResourceServerRepository resourceServerRepository;

    @Autowired
    private ApplicationProperties.S3Properties s3Properties;
    @Autowired
    private S3Client s3Client;
    @Autowired
    private ScpService scpService;
    @Autowired
    private CommandService commandService;
    @Autowired
    private ResourceProperties resourceProperties;

    @Autowired
    private ResourceManagementService resourceManagementService;

    private static final int PLUGIN_DEFAULT_START_PORT = 20000;
    private static final int PLUGIN_DEFAULT_END_PORT = 30000;

    public List<String> getAvailableContainerHosts() {
        QueryRequest queryRequest = QueryRequest.defaultQueryObject("type", ResourceItemType.DOCKER_CONTAINER);
        List<String> hostList = new ArrayList<String>();
        resourceManagementService.retrieveServers(queryRequest).getContents().forEach(rs -> {
            hostList.add(rs.getHost());
        });
        return hostList;
    }

    public Integer getAvailablePortByHostIp(String hostIp) {
        if (!(isIpValid(hostIp))) {
            throw new RuntimeException("Invalid host ip");
        }
        ResourceServer resourceServer = resourceServerRepository.findOneByHost(hostIp);
        if (null == resourceServer)
            throw new WecubeCoreException(String.format("Host IP [%s] is not found", hostIp));
        QueryRequest queryRequest = QueryRequest.defaultQueryObject("type", ResourceServerType.DOCKER)
                .addEqualsFilter("resourceServerId", resourceServer.getId());

        List<Integer> hasUsedPorts = Lists.newArrayList();
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
        throw new WecubeCoreException("There is no available ports in specified host");
    }

    public boolean isIpValid(String ip) {
        if (ip != null && !ip.isEmpty()) {
            String ipValidityRegularExpression = "^(([1-9])|([1-9][0-9])|(1[0-9][0-9])|(2[0-4][0-9])|(25[0-5]))((\\.([0-9]|([1-9][0-9])|(1[0-9][0-9])|(2[0-4][0-9])|(25[0-5]))){3})$";
            return ip.matches(ipValidityRegularExpression);
        }
        return false;
    }

    public List<PluginInstance> getAllInstances() {
        return Lists.newArrayList(pluginInstanceRepository.findAll());
    }

    public List<PluginInstance> getAvailableInstancesByPackageId(int packageId) {
        return pluginInstanceRepository.findByStatusAndPackageId(PluginInstance.STATUS_RUNNING, packageId);
    }

    public List<PluginInstance> getRunningPluginInstances(String pluginName) {
        Optional<PluginPackage> pkg = pluginPackageRepository.findLatestVersionByName(pluginName);
        if (!pkg.isPresent()) {
            throw new WecubeCoreException(String.format("Plugin pacakge [%s] not found.", pluginName));
        }

        List<PluginInstance> instances = pluginInstanceRepository
                .findByStatusAndPackageId(PluginInstance.STATUS_RUNNING, pkg.get().getId());
        if (instances == null || instances.size() == 0) {
            throw new WecubeCoreException(String.format("No instance for plugin [%s] is available.", pluginName));
        }
        return instances;
    }

    private boolean isContainerHostValid(String hostIp) {
        if (isIpValid(hostIp) && isHostIpAvailable(hostIp)) {
            return true;
        }
        return false;
    }

    private boolean isPortValid(String hostIp, Integer port) {
        List<PluginInstance> pluginInstances = pluginInstanceRepository.findByHostAndPort(hostIp, port);
        if (pluginInstances.size() == 0 || null == pluginInstances) {
            return true;
        }
        return false;
    }

    private String genRandomPassword() {
        return DigestUtils.md5Hex(String.valueOf(System.currentTimeMillis())).substring(0, 16);
    }

    public void launchPluginInstance(Integer packageId, String hostIp, Integer port) throws Exception {
        // 0. checking
        if (!isContainerHostValid(hostIp))
            throw new WecubeCoreException("Unavailable container host ip");

        if (!isPortValid(hostIp, port))
            throw new IllegalArgumentException(String.format(
                    "The port[%d] of host[%s] is already in used, please try to reassignment port", port, hostIp));

        Optional<PluginPackage> pluginPackageResult = pluginPackageRepository.findById(packageId);
        if (!pluginPackageResult.isPresent())
            throw new WecubeCoreException("Plugin package id does not exist, id = " + packageId);
        PluginPackage pluginPackage = pluginPackageResult.get();

        // 1. create MySql DB

        pluginPackage.getPluginPackageRuntimeResourcesMysql().forEach(mysqlInfo -> {
            createPluginMysqlDatabase(mysqlInfo);
        });

        // 2. create S3 bucket
        pluginPackage.getPluginPackageRuntimeResourcesS3().forEach(s3Info -> {
            createPluginS3Bucket(s3Info);
        });

        // 3. create docker instance
        pluginPackage.getPluginPackageRuntimeResourcesDocker().forEach(dockerInfo -> {
            try {
                createPluginDockerInstance(pluginPackage, hostIp, port);
            } catch (Exception e) {
                logger.error("Creating docker container instance meet error");
                e.printStackTrace();
            }
        });

        // 4. deploy UI package

        // 5. insert to DB

        // TODO - 6. notify gateway

    }

    private void createPluginMysqlDatabase(PluginPackageRuntimeResourcesMysql mysqlInfo) {
        String dbPassword = genRandomPassword();

        ResourceItemDto createResourceItem = new ResourceItemDto(mysqlInfo.getSchemaName(),
                ResourceItemType.MYSQL_DATABASE.getCode(),
                buildAdditionalPropertiesForMysqlDatabase(mysqlInfo.getPluginPackage().getName(),
                        mysqlInfo.getSchemaName(), dbPassword),
                null, "purpose");
        List<ResourceItemDto> result = resourceManagementService.createItems(Lists.newArrayList(createResourceItem));

    }

    private void createPluginS3Bucket(PluginPackageRuntimeResourcesS3 s3Info) {

    }

    private void createPluginDockerInstance(PluginPackage pluginPackage, String hostIp, Integer port) throws Exception {

        List<PluginInstance> pluginInstances = pluginInstanceRepository.findByHostAndPort(hostIp, port);
        if (pluginInstances.size() != 0) {
            throw new IllegalArgumentException(String.format(
                    "The port[%d] of host[%s] is already in use by container[%s], please try to reassignment port",
                    port, hostIp, pluginInstances.get(0).getInstanceContainerId()));
        }

        ResourceServer hostInfo = resourceServerRepository.findOneByHost(hostIp);

        // download package from MinIO
        String tmpFolderName = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
        String tmpFilePath = getTempFolderPath() + tmpFolderName + "/" + pluginProperties.getImageFile();

        logger.info("bucketName={}, tmpFilePath= {}", pluginProperties.getPluginPackageBucketName(), tmpFilePath);

        s3Client.downFile(pluginProperties.getPluginPackageBucketName(), pluginPackage.getName() + File.separator
                + pluginPackage.getVersion() + File.separator + pluginProperties.getImageFile(), tmpFilePath);

        logger.info("scp from local:{} to remote{}", tmpFilePath, pluginProperties.getPluginDeployPath());
        try {
            scpService.put(hostIp, Integer.valueOf(hostInfo.getPort()), hostInfo.getLoginUsername(),
                    hostInfo.getLoginPassword(), tmpFilePath, pluginProperties.getPluginDeployPath());
        } catch (Exception e) {
            throw new WecubeCoreException("Put file to remote host meet error: " + e.getMessage());
        }

        // load image at remote host
        String loadCmd = "docker load -i " + pluginProperties.getPluginDeployPath().trim() + File.separator
                + pluginProperties.getImageFile();
        logger.info("Run docker load command: " + loadCmd);
        try {
            commandService.runAtRemote(hostIp, hostInfo.getLoginUsername(), hostInfo.getLoginPassword(),
                    Integer.valueOf(hostInfo.getPort()), loadCmd);
        } catch (Exception e) {
            logger.error("Run command [{}] meet error: {}", loadCmd, e.getMessage());
            throw new WecubeCoreException(String.format("Run remote command meet error: %s", e.getMessage()));
        }

        ResourceItemDto createResourceItem = new ResourceItemDto(pluginPackage.getName(),
                ResourceItemType.DOCKER_CONTAINER.getCode(), buildAdditionalProperties(), hostInfo.getId(), "purpose");
        List<ResourceItemDto> result = resourceManagementService.createItems(Lists.newArrayList(createResourceItem));

        PluginInstance newPluginInstance = new PluginInstance(null, pluginPackage,
                result.get(0).getAdditionalPropertiesMap().get("containerId"), hostIp, port,
                PluginInstance.STATUS_RUNNING);
        pluginInstanceRepository.save(newPluginInstance);
    }

    public void removePluginInstanceById(Integer instanceId) throws Exception {
    }

    private boolean isHostIpAvailable(String hostIp) {
        List<String> hostIps = getAvailableContainerHosts();
        for (String ip : hostIps) {
            if (ip.equals(hostIp)) {
                return true;
            }
        }
        return false;
    }

    public static String getTempFolderPath() {
        String tmpFolder = "";
        tmpFolder = System.getProperty("java.io.tmpdir");
        logger.info("tmpFolder is {}", tmpFolder);
        if (!tmpFolder.endsWith("/") && !tmpFolder.endsWith("\\")) {
            tmpFolder = tmpFolder + "/";
        }

        return tmpFolder;
    }

    private String buildAdditionalPropertiesForMysqlDatabase(String name, String username, String password) {
        HashMap<String, String> additionalProperties = new HashMap<String, String>();
        additionalProperties.put("username", username);
        additionalProperties.put("password",
                EncryptionUtils.encryptWithAes(password, resourceProperties.getPasswordEncryptionSeed(), name));
        return JsonUtils.toJsonString(additionalProperties);
    }

    private String buildAdditionalProperties() {
        // TODO
        // -p 21000:21000 -e
        // DATA_SOURCE_URL='jdbc:mysql://129.204.99.160:3306/service_management?characterEncoding=utf8&serverTimezone=UTC'
        // -e DB_USER='xxxx' -e DB_PWD='xxxxx' -e CORE_ADDR='localhost' -v
        // /root/haixinhuang/service-management/wecube-plugins-service-management/log:/log:rw
        return "";
    }

    public String getInstanceAddress(PluginInstance instance) {
        return trim(instance.getHost()) + ":" + trim(instance.getPort().toString());
    }

}
