package com.webank.wecube.platform.core.service;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

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
import com.webank.wecube.platform.core.utils.StringUtils;
import com.webank.wecube.platform.core.utils.ZipFileUtils;

import javassist.expr.NewArray;

import com.webank.wecube.platform.core.service.ScpService;
import com.webank.wecube.platform.core.service.CommandService;
import com.webank.wecube.platform.core.commons.ApplicationProperties.ResourceProperties;
import com.webank.wecube.platform.core.commons.ApplicationProperties;
import com.webank.wecube.platform.core.commons.ApplicationProperties.PluginProperties;
import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.domain.ResourceItem;
import com.webank.wecube.platform.core.domain.ResourceServer;
import com.webank.wecube.platform.core.domain.plugin.PluginInstance;
import com.webank.wecube.platform.core.domain.plugin.PluginMysqlInstance;
import com.webank.wecube.platform.core.domain.plugin.PluginPackage;
import com.webank.wecube.platform.core.domain.plugin.PluginPackageRuntimeResourcesMysql;
import com.webank.wecube.platform.core.domain.plugin.PluginPackageRuntimeResourcesS3;
import com.webank.wecube.platform.core.dto.CreateInstanceDto;
import com.webank.wecube.platform.core.dto.QueryRequest;
import com.webank.wecube.platform.core.dto.ResourceItemDto;
import com.webank.wecube.platform.core.dto.ResourceServerDto;
import com.webank.wecube.platform.core.jpa.PluginConfigRepository;
import com.webank.wecube.platform.core.jpa.PluginInstanceRepository;
import com.webank.wecube.platform.core.jpa.PluginMysqlInstanceRepository;
import com.webank.wecube.platform.core.jpa.PluginPackageRepository;
import com.webank.wecube.platform.core.jpa.ResourceItemRepository;
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
    PluginMysqlInstanceRepository pluginMysqlInstanceRepository;

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
    @Autowired
    private ResourceItemRepository resourceItemRepository;

    private static final int PLUGIN_DEFAULT_START_PORT = 20000;
    private static final int PLUGIN_DEFAULT_END_PORT = 30000;
    private static final String SPACE = " ";

    public List<String> getAvailableContainerHosts() {
        QueryRequest queryRequest = QueryRequest.defaultQueryObject("type", ResourceItemType.DOCKER_CONTAINER);
        List<String> hostList = new ArrayList<String>();
        resourceManagementService.retrieveServers(queryRequest).getContents().forEach(rs -> {
            hostList.add(rs.getHost());
        });
        return hostList;
    }

    public Integer getAvailablePortByHostIp(String hostIp) {
        if (!(StringUtils.isValidIp(hostIp))) {
            throw new RuntimeException("Invalid host ip");
        }
        ResourceServer resourceServer = resourceServerRepository.findByHost(hostIp).get(0);
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
        if (StringUtils.isValidIp(hostIp) && isHostIpAvailable(hostIp)) {
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

    private class DatabaseInfo {
        String connectString;
        String user;
        String password;

        public String getConnectString() {
            return connectString;
        }

        public void setConnectString(String connectString) {
            this.connectString = connectString;
        }

        public String getUser() {
            return user;
        }

        public void setUser(String user) {
            this.user = user;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    public void launchPluginInstance(Integer packageId, String hostIp, Integer port,
            CreateInstanceDto createContainerParameters) throws Exception {
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

        PluginInstance instance = new PluginInstance();
        instance.setPluginPackage(pluginPackage);

        DatabaseInfo dbInfo = new DatabaseInfo();
        // 1. create MySql DB
        Set<PluginPackageRuntimeResourcesMysql> mysqlSet = pluginPackage.getPluginPackageRuntimeResourcesMysql();
        if (mysqlSet.size() != 0) {
            PluginMysqlInstance mysqlInstance = createPluginMysqlDatabase(mysqlSet.iterator().next());
            instance.setPluginMysqlInstanceResourceId(mysqlInstance.getId());
            ResourceServer dbServer = resourceItemRepository.findById(mysqlInstance.getResourceItemId()).get()
                    .getResourceServer();
            dbInfo.connectString = String.format("jdbc:mysql://%s:%s/%s?characterEncoding=utf8&serverTimezone=UTC",
                    dbServer.getHost(), dbServer.getPort(), mysqlInstance.getSchemaName());
            dbInfo.user = mysqlInstance.getUsername();
            dbInfo.password = mysqlInstance.getPassword();
        }
        // 2. create S3 bucket
        Set<PluginPackageRuntimeResourcesS3> s3Set = pluginPackage.getPluginPackageRuntimeResourcesS3();
        if (s3Set.size() != 0) {
            instance.setS3BucketResourceId(createPluginS3Bucket(s3Set.iterator().next()));
        }
        // 3. create docker instance
        createContainerParameters.setPortBindingParameters(
                createContainerParameters.getPortBindingParameters().replace("{{host_port}}", String.valueOf(port)));
        createContainerParameters.setEnvVariableParameters(createContainerParameters.getEnvVariableParameters()
                .replace("{{data_source_url}}", dbInfo.getConnectString()).replace("{{db_user}}", dbInfo.getUser())
                .replace("{{db_password}}", dbInfo.getPassword()));
        pluginPackage.getPluginPackageRuntimeResourcesDocker().forEach(dockerInfo -> {
            try {
                String additonalParam = "";

                ResourceItemDto dockerResourceDto = createPluginDockerInstance(pluginPackage, hostIp,
                        createContainerParameters);
                instance.setDockerInstanceResourceId(dockerResourceDto.getId());
                instance.setHost(hostIp);
                instance.setPort(port);
            } catch (Exception e) {
                logger.error("Creating docker container instance meet error");
                e.printStackTrace();
            }
        });

        // 4. deploy UI package

        deployUiPackage(pluginPackage);

        // 5. insert to DB
        instance.setStatus(PluginInstance.STATUS_RUNNING);
        pluginInstanceRepository.save(instance);

        // TODO - 6. notify gateway

    }

    public PluginMysqlInstance createPluginMysqlDatabase(PluginPackageRuntimeResourcesMysql mysqlInfo) {
        // get mysql server
        QueryRequest queryRequest = QueryRequest.defaultQueryObject("type", ResourceServerType.MYSQL);
        ResourceServerDto mysqlServer = resourceManagementService.retrieveServers(queryRequest).getContents().get(0);

        String dbPassword = genRandomPassword();
        ResourceItemDto createMysqlDto = new ResourceItemDto(mysqlInfo.getSchemaName(),
                ResourceItemType.MYSQL_DATABASE.getCode(),
                buildAdditionalPropertiesForMysqlDatabase(mysqlInfo.getPluginPackage().getName(),
                        mysqlInfo.getSchemaName(), dbPassword),
                mysqlServer.getId(), String.format("Build MySQL database for plugin[%s]", mysqlInfo.getSchemaName()));
        createMysqlDto.setResourceServer(mysqlServer);
        logger.info("createMysqlDto = " + createMysqlDto);

        List<ResourceItemDto> result = resourceManagementService.createItems(Lists.newArrayList(createMysqlDto));
        PluginMysqlInstance mysqlInstance = new PluginMysqlInstance(mysqlInfo.getSchemaName(), result.get(0).getId(),
                mysqlInfo.getSchemaName(), dbPassword, "active");
        pluginMysqlInstanceRepository.saveAndFlush(mysqlInstance);

        logger.info("createPluginMysqlDatabase done...");
        return mysqlInstance;
    }

    private Integer createPluginS3Bucket(PluginPackageRuntimeResourcesS3 s3Info) {
        QueryRequest queryRequest = QueryRequest.defaultQueryObject("type", ResourceServerType.S3);
        ResourceServerDto s3Server = resourceManagementService.retrieveServers(queryRequest).getContents().get(0);

        ResourceItemDto createS3BucketDto = new ResourceItemDto(s3Info.getBucketName(),
                ResourceItemType.S3_BUCKET.getCode(), null, s3Server.getId(),
                String.format("Build S3 bucket for plugin[%s]", s3Info.getBucketName()));
        createS3BucketDto.setResourceServer(s3Server);
        logger.info("createS3BucketDto = " + createS3BucketDto);

        List<ResourceItemDto> result = resourceManagementService.createItems(Lists.newArrayList(createS3BucketDto));
        return result.get(0).getId();
    }

    private ResourceItemDto createPluginDockerInstance(PluginPackage pluginPackage, String hostIp,
            CreateInstanceDto createContainerParameters) throws Exception {
        ResourceServer hostInfo = resourceServerRepository.findByHost(hostIp).get(0);

        // download package from MinIO
        String tmpFolderName = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
        String tmpFilePath = getTempFolderPath() + tmpFolderName + "/" + pluginProperties.getImageFile();

        String s3KeyName = pluginPackage.getName() + File.separator + pluginPackage.getVersion() + File.separator
                + pluginProperties.getImageFile();
        logger.info("Download plugin package from S3: {}", s3KeyName);

        s3Client.downFile(pluginProperties.getPluginPackageBucketName(), s3KeyName, tmpFilePath);

        logger.info("scp from local:{} to remote: {}", tmpFilePath, pluginProperties.getPluginDeployPath());
        try {
            logger.info(hostIp + "+" + Integer.valueOf(hostInfo.getPort()) + "+" + hostInfo.getLoginUsername() + "+"
                    + EncryptionUtils.decryptWithAes(hostInfo.getLoginPassword(),
                            resourceProperties.getPasswordEncryptionSeed(), hostInfo.getName())
                    + "+" + tmpFilePath + "+" + pluginProperties.getPluginDeployPath());
            scpService.put(hostIp, Integer.valueOf(hostInfo.getPort()), hostInfo.getLoginUsername(),
                    EncryptionUtils.decryptWithAes(hostInfo.getLoginPassword(),
                            resourceProperties.getPasswordEncryptionSeed(), hostInfo.getName()),
                    tmpFilePath, pluginProperties.getPluginDeployPath());
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

        String imageName = pluginPackage.getName() + ":" + pluginPackage.getVersion();

        ResourceItemDto createDockerInstanceDto = new ResourceItemDto(pluginPackage.getName(),
                ResourceItemType.DOCKER_CONTAINER.getCode(),
                buildAdditionalPropertiesForDocker(imageName, createContainerParameters), hostInfo.getId(), "purpose");
        logger.info("createDockerInstanceDto = " + createDockerInstanceDto.toString());

        List<ResourceItemDto> result = resourceManagementService
                .createItems(Lists.newArrayList(createDockerInstanceDto));

        return result.get(0);
    }

    private void deployUiPackage(PluginPackage pluginPackage) throws Exception {
        // download UI package from MinIO
        String tmpFolderName = getTempFolderPath() + new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
        String downloadUiZipPath = tmpFolderName + File.separator + pluginProperties.getUiFile();

        String s3UiPackagePath = pluginPackage.getName() + File.separator + pluginPackage.getVersion() + File.separator
                + pluginProperties.getUiFile();
        s3Client.downFile(pluginProperties.getPluginPackageBucketName(), s3UiPackagePath, downloadUiZipPath);

        String remotePath = pluginProperties.getStaticResourceServerPath() + pluginPackage.getName() + File.separator
                + pluginPackage.getVersion();

        // mkdir at remote host
        String mkdirCmd = String.format("mkdir %s", remotePath);
        try {
            commandService.runAtRemote(pluginProperties.getStaticResourceServerIp(),
                    pluginProperties.getStaticResourceServerUser(), pluginProperties.getStaticResourceServerPassword(),
                    pluginProperties.getStaticResourceServerPort(), mkdirCmd);
        } catch (Exception e) {
            logger.error("Run command [mkdir] meet error: ", e.getMessage());
            throw new WecubeCoreException(String.format("Run remote command meet error: %s", e.getMessage()));
        }

        // scp UI.zip to Static Resource Server
        try {
            scpService.put(pluginProperties.getStaticResourceServerIp(), pluginProperties.getStaticResourceServerPort(),
                    pluginProperties.getStaticResourceServerUser(), pluginProperties.getStaticResourceServerPassword(),
                    downloadUiZipPath, remotePath);
        } catch (Exception e) {
            throw new WecubeCoreException("Put file to remote host meet error: " + e.getMessage());
        }

        // unzip file
        String unzipCmd = String.format("unzip %s", remotePath + pluginProperties.getUiFile());
        try {
            commandService.runAtRemote(pluginProperties.getStaticResourceServerIp(),
                    pluginProperties.getStaticResourceServerUser(), pluginProperties.getStaticResourceServerPassword(),
                    pluginProperties.getStaticResourceServerPort(), unzipCmd);
        } catch (Exception e) {
            logger.error("Run command [unzip] meet error: ", e.getMessage());
            throw new WecubeCoreException(String.format("Run remote command meet error: %s", e.getMessage()));
        }

    }

    public void removePluginInstanceById(Integer instanceId) throws Exception {

        ResourceItemDto createDockerInstanceDto = new ResourceItemDto();
        logger.info("createDockerInstanceDto = " + createDockerInstanceDto.toString());

        List<ResourceItemDto> result = resourceManagementService
                .createItems(Lists.newArrayList(createDockerInstanceDto));
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
        additionalProperties.put("username", username.substring(0, 16));
        additionalProperties.put("password",
                EncryptionUtils.encryptWithAes(password, resourceProperties.getPasswordEncryptionSeed(), username));
        return JsonUtils.toJsonString(additionalProperties);
    }

    private String buildAdditionalPropertiesForDocker(String imageName, CreateInstanceDto additionalParams) {
        HashMap<String, String> additionalProperties = new HashMap<String, String>();
        additionalProperties.put("imageName", imageName);
        additionalProperties.put("portBindings", additionalParams.getPortBindingParameters());
        additionalProperties.put("volumeBindings", additionalParams.getVolumeBindingParameters());
        additionalProperties.put("envVariables", additionalParams.getEnvVariableParameters());

        return JsonUtils.toJsonString(additionalProperties);
    }

    public String getInstanceAddress(PluginInstance instance) {
        return trim(instance.getHost()) + ":" + trim(instance.getPort().toString());
    }

}
