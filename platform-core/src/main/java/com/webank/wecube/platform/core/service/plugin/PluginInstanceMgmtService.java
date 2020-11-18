package com.webank.wecube.platform.core.service.plugin;

import static com.google.common.collect.Lists.newArrayList;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.h2.jmx.DatabaseInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Service;

import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.commons.ApplicationProperties.ResourceProperties;
import com.webank.wecube.platform.core.domain.ResourceServerDomain;
import com.webank.wecube.platform.core.domain.plugin.PluginInstance;
import com.webank.wecube.platform.core.domain.plugin.PluginMysqlInstance;
import com.webank.wecube.platform.core.domain.plugin.PluginPackage;
import com.webank.wecube.platform.core.dto.CreateInstanceDto;
import com.webank.wecube.platform.core.dto.ResourceItemDto;
import com.webank.wecube.platform.core.dto.plugin.PluginInstanceDto;
import com.webank.wecube.platform.core.entity.plugin.PluginInstances;
import com.webank.wecube.platform.core.entity.plugin.PluginMysqlInstances;
import com.webank.wecube.platform.core.entity.plugin.PluginPackageRuntimeResourcesDocker;
import com.webank.wecube.platform.core.entity.plugin.PluginPackageRuntimeResourcesMysql;
import com.webank.wecube.platform.core.entity.plugin.PluginPackageRuntimeResourcesS3;
import com.webank.wecube.platform.core.entity.plugin.PluginPackages;
import com.webank.wecube.platform.core.entity.plugin.ResourceItem;
import com.webank.wecube.platform.core.entity.plugin.ResourceServer;
import com.webank.wecube.platform.core.repository.plugin.PluginInstancesMapper;
import com.webank.wecube.platform.core.repository.plugin.PluginMysqlInstancesMapper;
import com.webank.wecube.platform.core.repository.plugin.PluginPackageRuntimeResourcesDockerMapper;
import com.webank.wecube.platform.core.repository.plugin.PluginPackageRuntimeResourcesMysqlMapper;
import com.webank.wecube.platform.core.repository.plugin.PluginPackageRuntimeResourcesS3Mapper;
import com.webank.wecube.platform.core.repository.plugin.PluginPackagesMapper;
import com.webank.wecube.platform.core.repository.plugin.ResourceItemMapper;
import com.webank.wecube.platform.core.repository.plugin.ResourceServerMapper;
import com.webank.wecube.platform.core.service.resource.ResourceItemType;
import com.webank.wecube.platform.core.service.resource.ResourceManagementService;
import com.webank.wecube.platform.core.service.resource.ResourceServerType;
import com.webank.wecube.platform.core.support.gateway.GatewayResponse;
import com.webank.wecube.platform.core.utils.EncryptionUtils;
import com.webank.wecube.platform.core.utils.StringUtilsEx;
import com.webank.wecube.platform.core.utils.SystemUtils;
import com.webank.wecube.platform.workflow.commons.LocalIdGenerator;

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

    @Autowired
    private ResourceItemMapper resourceItemMapper;

    @Autowired
    private PluginPackageRuntimeResourcesDockerMapper pluginPackageRuntimeResourcesDockerMapper;

    @Autowired
    private PluginPackageRuntimeResourcesMysqlMapper pluginPackageRuntimeResourcesMysqlMapper;

    @Autowired
    private PluginPackageRuntimeResourcesS3Mapper pluginPackageRuntimeResourcesS3Mapper;

    @Autowired
    private PluginMysqlInstancesMapper pluginMysqlInstancesMapper;

    private VersionComparator versionComparator = new VersionComparator();
    
    @Autowired
    private ResourceProperties resourceProperties;

    /**
     * 
     * @param pluginPackageId
     * @param hostIp
     * @param port
     */
    public void launchPluginInstance(String pluginPackageId, String hostIpAddr, Integer port) {

        PluginPackages pluginPackageEntity = pluginPackagesMapper.selectByPrimaryKey(pluginPackageId);
        if (pluginPackageEntity == null) {
            String errMsg = String.format("Plugin package does not exist, id =  %s", pluginPackageId);
            throw new WecubeCoreException("3077", errMsg, pluginPackageId);
        }

        doLaunchPluginInstance(pluginPackageEntity, hostIpAddr, port);
    }

    private void doLaunchPluginInstance(PluginPackages pluginPackage, String hostIpAddr, Integer port) {
        validateLauchPluginInstanceParameters(pluginPackage, hostIpAddr, port);

        List<PluginPackageRuntimeResourcesDocker> dockerInfoSet = pluginPackageRuntimeResourcesDockerMapper
                .selectAllByPackage(pluginPackage.getId());
        List<PluginPackageRuntimeResourcesMysql> mysqlInfoSet = pluginPackageRuntimeResourcesMysqlMapper
                .selectAllByPackage(pluginPackage.getId());
        List<PluginPackageRuntimeResourcesS3> s3InfoSet = pluginPackageRuntimeResourcesS3Mapper
                .selectAllByPackage(pluginPackage.getId());

        PluginInstances instanceEntity = new PluginInstances();
        instanceEntity.setId(LocalIdGenerator.generateId());
        instanceEntity.setPackageId(pluginPackage.getId());
        instanceEntity.setPluginPackage(pluginPackage);

        //

        LocalDatabaseInfo dbInfo = handleCreateDatabase(mysqlInfoSet, pluginPackage);
        if (dbInfo != null) {
            instance.setPluginMysqlInstanceResourceId(dbInfo.getResourceItemId());
        }

        String s3BucketResourceId = handleCreateS3Bucket(s3InfoSet, pluginPackage);
        if (s3BucketResourceId != null)
            instance.setS3BucketResourceId(s3BucketResourceId);

        // 3. create docker instance
        if (dockerInfoSet.size() != 1) {
            throw new WecubeCoreException("3078", "Only support plugin running in one container so far");
        }
        PluginPackageRuntimeResourcesDocker dockerInfo = dockerInfoSet.iterator().next();

        String portBindingString = replaceAllocatePort(dockerInfo.getPortBindings(), port);
        String envVariablesString = replaceHostIp(dockerInfo.getEnvVariables(), hostIpAddr);
        String volumeBindingString = replaceBaseMountPath(dockerInfo.getVolumeBindings());

        CreateInstanceDto createContainerParameters = new CreateInstanceDto(dockerInfo.getImageName(),
                dockerInfo.getContainerName(), portBindingString, volumeBindingString);

        envVariablesString = envVariablesString.replace(",", "\\,");
        if (mysqlInfoSet.size() != 0) {

            String password = dbInfo.getPassword();
            if (password.startsWith(ResourceManagementService.PASSWORD_ENCRYPT_AES_PREFIX)) {
                password = password.substring(ResourceManagementService.PASSWORD_ENCRYPT_AES_PREFIX.length());
            }

            password = EncryptionUtils.decryptWithAes(password, resourceProperties.getPasswordEncryptionSeed(),
                    dbInfo.getSchema());

            envVariablesString = envVariablesString.replace("{{DB_HOST}}", dbInfo.getHost())
                    .replace("{{DB_PORT}}", dbInfo.getPort()).replace("{{DB_SCHEMA}}", dbInfo.getSchema())
                    .replace("{{DB_USER}}", dbInfo.getUser())
                    .replace("{{DB_PWD}}", tryEncryptPasswordAsPluginEnv(password));
        }
        logger.info("before replace envVariablesString=" + envVariablesString);
        envVariablesString = replaceJwtSigningKey(envVariablesString);
        envVariablesString = replaceSystemVariablesForEnvVariables(pluginPackage.getName(), envVariablesString);
        logger.info("after replace envVariablesString=" + envVariablesString);

        createContainerParameters.setEnvVariableParameters(envVariablesString.isEmpty() ? "" : envVariablesString);

        try {
            ResourceItemDto dockerResourceDto = createPluginDockerInstance(pluginPackage, hostIpAddr,
                    createContainerParameters);
            instance.setDockerInstanceResourceId(dockerResourceDto.getId());
        } catch (Exception e) {
            logger.error("Creating docker container instance meet error: ", e.getMessage());
            throw new WecubeCoreException("3079", "Creating docker container instance meet error: " + e.getMessage(),
                    e);
        }

        instance.setContainerName(dockerInfo.getContainerName());
        instance.setInstanceName(pluginPackage.getName());
        instance.setHost(hostIpAddr);
        instance.setPort(port);

        // 4. insert to DB
        instance.setContainerStatus(PluginInstance.CONTAINER_STATUS_RUNNING);
        pluginInstanceRepository.save(instance);

        // 6. register route
        GatewayResponse response = registerRoute(pluginPackage.getName(), hostIpAddr, String.valueOf(port));
        if (!response.getStatus().equals(GatewayResponse.getStatusCodeOk())) {
            logger.error("Launch instance has done, but register routing information is failed, please check");
        }
    }

    private LocalDatabaseInfo handleCreateDatabase(List<PluginPackageRuntimeResourcesMysql> mysqlInfoEntities,
            PluginPackages pluginPackage) {
        if (mysqlInfoEntities == null || mysqlInfoEntities.isEmpty()) {
            return null;
        }
        if (mysqlInfoEntities.size() > 1) {
            log.error("Apply [{}] schema is not allow", mysqlInfoEntities.size());
            throw new WecubeCoreException("3073", "Only allow to apply one MYSQL instance so far.");
        }

        List<PluginMysqlInstances> mysqlInstancesEntities = pluginMysqlInstancesMapper.selectAllByPackageNameAndStatus(
                pluginPackage.getName(), PluginMysqlInstances.MYSQL_INSTANCE_STATUS_ACTIVE);

        if (mysqlInstancesEntities == null || mysqlInstancesEntities.isEmpty()) {
            // new mysql instance
            // TODO
            return tryInitMysqlDatabaseSchema(mysqlInfoSet, pluginPackage);
        }

        if (mysqlInstancesEntities.size() > 1) {
            // TODO throw exception
        }

        PluginMysqlInstances mysqlInstancesEntity = mysqlInstancesEntities.get(0);

        LocalDatabaseInfo resultLocalDatabaseInfo = tryHandleExistMysqlInstance(mysqlInstancesEntity, pluginPackage);

        return resultLocalDatabaseInfo;
    }

    private LocalDatabaseInfo tryHandleExistMysqlInstance(PluginMysqlInstances mysqlInstance,
            PluginPackages pluginPackage) {
        // already exists
        ResourceItem resourceItemEntity = resourceItemMapper.selectByPrimaryKey(mysqlInstance.getResourceItemId());
        ResourceServer resourceServerEntity = resourceServerMapper.selectByPrimaryKey(resourceItemEntity.getResourceServerId());
        tryUpgradeMysqlDatabaseData(mysqlInstance, pluginPackage, resourceItemEntity, resourceServerEntity);
        if (StringUtils.isBlank(mysqlInstance.getPreVersion())) {
            mysqlInstance.setPreVersion(pluginPackage.getVersion());
        }
        int versionCompare = versionComparator.compare(pluginPackage.getVersion(),
                mysqlInstance.getPreVersion());
        if (versionCompare >= 0) {
            mysqlInstance.setPreVersion(pluginPackage.getVersion());
        }
        mysqlInstance.setUpdatedTime(new Date());
        
        pluginMysqlInstancesMapper.updateByPrimaryKeySelective(mysqlInstance);
//        pluginMysqlInstanceRepository.save(mysqlInstance);
//        ResourceServerDomain resourceServer = mysqlInstance.getResourceItem().getResourceServer();
        return new LocalDatabaseInfo(resourceServerEntity.getHost(), resourceServerEntity.getPort(), mysqlInstance.getSchemaName(),
                mysqlInstance.getUsername(), mysqlInstance.getPassword(), mysqlInstance.getResourceItemId());
    }
    
    private LocalDatabaseInfo tryInitMysqlDatabaseSchema(List<PluginPackageRuntimeResourcesMysql> mysqlSet,
            PluginPackages pluginPackage) {
        if (mysqlSet.size() != 0) {
            PluginMysqlInstance mysqlInstance = createPluginMysqlDatabase(mysqlSet.iterator().next(),
                    pluginPackage.getVersion());

            ResourceServerDomain dbServer = resourceItemRepository.findById(mysqlInstance.getResourceItemId()).get()
                    .getResourceServer();
            LocalDatabaseInfo dbInfo = new LocalDatabaseInfo(dbServer.getHost(), dbServer.getPort(),
                    mysqlInstance.getSchemaName(), mysqlInstance.getUsername(), mysqlInstance.getPassword(),
                    mysqlInstance.getId());

            // execute init.sql
            initMysqlDatabaseTables(dbServer, mysqlInstance, pluginPackage);
            return dbInfo;
        }
        return null;
    }

    private void tryUpgradeMysqlDatabaseData(PluginMysqlInstances mysqlInstance, PluginPackages pluginPackage, ResourceItem resourceItem, ResourceServer resourceServer) {
        String latestVersion = mysqlInstance.getPreVersion();
        if (!shouldUpgradeMysqlDatabaseData(latestVersion, pluginPackage.getVersion())) {
            log.info("latest version {} and current version {}, no need to upgrade.", latestVersion,
                    pluginPackage.getVersion());
            return;
        }

        if (isStringBlank(latestVersion)) {
            latestVersion = pluginPackage.getVersion();
        }

        try {
            log.info("try to perform database upgrade for {} {}", pluginPackage.getName(),
                    pluginPackage.getVersion());
            performUpgradeMysqlDatabaseData(mysqlInstance, pluginPackage, latestVersion, resourceItem, resourceServer);
        } catch (IOException e) {
            log.error("errors while processing upgrade sql", e);
            throw new WecubeCoreException("3074", "System error to upgrade plugin database.");
        }
    }
    
    private boolean isStringBlank(String s) {
        if (s == null || s.trim().length() < 1) {
            return true;
        }

        return false;
    }
    
    private boolean versionEquals(String version, String baseVersion) {
        VersionComparator vc = new VersionComparator();
        int compare = vc.compare(version, baseVersion);
        if (compare == 0) {
            return true;
        }

        return false;
    }
    
    private void performUpgradeMysqlDatabaseData(PluginMysqlInstances mysqlInstance, PluginPackages pluginPackage,
            String latestVersion, ResourceItem resourceItem, ResourceServer resourceServer) throws IOException {
        String tmpFolderName = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
        String baseTmpDir = SystemUtils.getTempFolderPath() + tmpFolderName + "/";
        String initSqlPath = baseTmpDir + pluginProperties.getInitDbSql();

        String s3KeyName = pluginPackage.getName() + File.separator + pluginPackage.getVersion() + File.separator
                + pluginProperties.getInitDbSql();
        log.info("Download init.sql from S3: {}", s3KeyName);

        s3Client.downFile(pluginProperties.getPluginPackageBucketName(), s3KeyName, initSqlPath);

//        ResourceServerDomain dbServer = resourceItemRepository.findById(mysqlInstance.getResourceItemId()).get()
//                .getResourceServer();
        String password = mysqlInstance.getPassword();
        if (password.startsWith(ResourceManagementService.PASSWORD_ENCRYPT_AES_PREFIX)) {
            password = password.substring(ResourceManagementService.PASSWORD_ENCRYPT_AES_PREFIX.length());
        }
        password = EncryptionUtils.decryptWithAes(password, resourceProperties.getPasswordEncryptionSeed(),
                mysqlInstance.getSchemaName());
        DriverManagerDataSource dataSource = new DriverManagerDataSource(
                "jdbc:mysql://" + resourceServer.getHost() + ":" + resourceServer.getPort() + "/" + mysqlInstance.getSchemaName()
                        + "?characterEncoding=utf8&serverTimezone=UTC",
                mysqlInstance.getUsername(), password);
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");

        File initSqlFile = new File(initSqlPath);

        File upgradeSqlFile = parseUpgradeMysqlDataFile(baseTmpDir, initSqlFile, pluginPackage, latestVersion);
        List<Resource> scripts = newArrayList(new FileSystemResource(upgradeSqlFile));
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.setContinueOnError(false);
        populator.setIgnoreFailedDrops(false);
        populator.setSeparator(";");
        populator.setCommentPrefix("#");
        populator.setSqlScriptEncoding("utf-8");
        for (Resource script : scripts) {
            populator.addScript(script);
        }
        try {
            log.info("start to execute sql script file:{}, host:{},port:{},schema:{}",
                    upgradeSqlFile.getAbsolutePath(), resourceServer.getHost(), resourceServer.getPort(),
                    mysqlInstance.getSchemaName());
            populator.execute(dataSource);
        } catch (Exception e) {
            String errorMessage = String.format("Failed to execute [%s] for schema[%s]", upgradeSqlFile.getName(),
                    mysqlInstance.getSchemaName());
            log.error(errorMessage);
            throw new WecubeCoreException("3075", errorMessage, e);
        }
        log.info(String.format("Upgrade database[%s] finished...", mysqlInstance.getSchemaName()));
    }
    
    private File parseUpgradeMysqlDataFile(String baseTmpDir, File initSqlFile, PluginPackages pluginPackage,
            String latestVersion) throws IOException {
        File upgradeSqlFile = new File(baseTmpDir, String.format("upgrade%s.sql", System.currentTimeMillis()));
        Pattern p = Pattern.compile(VersionTagInfo.VERSION_TAG_PATTERN);
        String foreignCheckOff = "SET FOREIGN_KEY_CHECKS = 0;";
        String foreignCheckOn = "SET FOREIGN_KEY_CHECKS = 1;";

        BufferedReader br = null;
        BufferedWriter bw = null;
        String currentVersion = pluginPackage.getVersion();
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(initSqlFile), Charset.forName("utf-8")));
            bw = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(upgradeSqlFile), Charset.forName("utf-8")));

            bw.write(foreignCheckOff + "\n");
            long lineNum = 0L;
            String sLine = null;
            boolean shouldStart = false;
            boolean shouldStop = false;
            while ((sLine = br.readLine()) != null) {
                lineNum++;
                String trimLine = sLine.trim();
                Matcher m = p.matcher(trimLine);
                if (m.matches()) {
                    VersionTagInfo info = VersionTagInfo.parseVersionTagInfo(m, lineNum);
                    if (!shouldStart) {
                        if (shouldStart(info, latestVersion, currentVersion)) {
                            shouldStart = true;
                        }
                    }

                    if (!shouldStop) {
                        if (shouldStop(info, currentVersion)) {
                            shouldStop = true;
                        }
                    }
                }

                if (shouldStart) {
                    bw.write(sLine + "\n");
                }

                if (shouldStop) {
                    break;
                }
            }

            bw.write(foreignCheckOn + "\n");
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    logger.warn("", e);
                }
            }

            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException e) {
                    logger.warn("", e);
                }
            }
        }
        return upgradeSqlFile;
    }
    
    private boolean shouldStart(VersionTagInfo info, String latestVersion, String currentVersion) {
        if (!info.isBegin()) {
            return false;
        }
        if (versionGreaterThan(info.getVersion(), latestVersion)) {
            return true;
        }

        if (versionEquals(info.getVersion(), currentVersion)) {
            return true;
        }

        return false;
    }
    
    private boolean versionGreaterThan(String version, String baseVersion) {
        VersionComparator vc = new VersionComparator();
        int compare = vc.compare(version, baseVersion);
        if (compare > 0) {
            return true;
        }

        return false;

    }

    private boolean shouldStop(VersionTagInfo info, String currentVersion) {

        if (versionGreaterThan(info.getVersion(), currentVersion)) {
            return true;
        }

        if (versionEquals(info.getVersion(), currentVersion)) {
            if (info.isEnd()) {
                return true;
            }
        }

        return false;
    }
    
    private boolean shouldUpgradeMysqlDatabaseData(String latestVersion, String currentVersion) {
        if (isStringBlank(latestVersion)) {
            return true;
        }

        if (versionEquals(currentVersion, latestVersion)) {
            return false;
        }

        if (versionGreaterThan(currentVersion, latestVersion)) {
            return true;
        }

        return false;
    }

    private void validateLauchPluginInstanceParameters(PluginPackages pluginPackage, String hostIpAddr, Integer port)
            throws Exception {
        if (!isContainerHostValid(hostIpAddr)) {
            throw new WecubeCoreException("3070", "Unavailable container host ip");
        }

        if (!isPortValid(hostIpAddr, port)) {
            String errMsg = String.format(
                    "The port[%s] of host[%s] is already in used, please try to reassignment port", port, hostIpAddr);
            throw new WecubeCoreException("3071", errMsg, port, hostIpAddr);
        }

        if (pluginPackage.getStatus().equals(PluginPackage.Status.DECOMMISSIONED)
                || pluginPackage.getStatus().equals(PluginPackage.Status.UNREGISTERED)) {
            throw new WecubeCoreException("3072",
                    "'DECOMMISSIONED' or 'UNREGISTERED' state can not launch plugin instance ");
        }
    }

    private boolean isPortValid(String hostIp, Integer port) {
        List<PluginInstances> pluginInstances = pluginInstancesMapper.selectAllByHostAndPortAndStatus(hostIp, port,
                PluginInstance.CONTAINER_STATUS_RUNNING);
        if (pluginInstances == null || pluginInstances.isEmpty()) {
            return true;
        }
        return false;
    }

    private boolean isContainerHostValid(String hostIp) {
        if (StringUtilsEx.isValidIp(hostIp) && isHostIpAvailable(hostIp)) {
            return true;
        }
        return false;
    }

    private boolean isHostIpAvailable(String hostIp) {
        List<String> hosts = getAvailableContainerHosts();
        if (hosts == null || hosts.isEmpty()) {
            return false;
        }

        if (hosts.contains(hostIp)) {
            return true;
        }

        return false;
    }

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

        List<ResourceServer> resourceServerEntities = resourceServerMapper
                .selectAllByType(ResourceServerType.DOCKER.getCode());

        List<String> hostList = new ArrayList<String>();

        for (ResourceServer entity : resourceServerEntities) {
            hostList.add(entity.getHost());
        }

        return hostList;
    }

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

        List<ResourceItem> resourceItemEntities = resourceItemMapper
                .selectAllByResourceServerAndType(resourceServer.getId(), ResourceItemType.DOCKER_CONTAINER.getCode());

        List<Integer> hasUsedPorts = new ArrayList<>();

        for (ResourceItem resourceItemEntity : resourceItemEntities) {
            String[] portBindingsParts = resourceItemEntity.getAdditionalPropertiesMap().get("portBindings").split(",");
            for (String portBindingsPart : portBindingsParts) {
                String[] portArray = portBindingsPart.split(":");
                hasUsedPorts.add(Integer.valueOf(portArray[0]));
            }
        }

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

    private static class LocalDatabaseInfo {
        String host;
        String port;
        String schema;
        String user;
        String password;
        String resourceItemId;

        private LocalDatabaseInfo(String host, String port, String schema, String user, String password,
                String resourceItemId) {
            this.host = host;
            this.port = port;
            this.schema = schema;
            this.user = user;
            this.password = password;
            this.resourceItemId = resourceItemId;
        }

        private LocalDatabaseInfo() {
        }

        public String getUser() {
            return user;
        }

        public String getPassword() {
            return password;
        }

        public String getHost() {
            return host;
        }

        public String getPort() {
            return port;
        }

        public String getSchema() {
            return schema;
        }

        public String getResourceItemId() {
            return resourceItemId;
        }
    }

}
