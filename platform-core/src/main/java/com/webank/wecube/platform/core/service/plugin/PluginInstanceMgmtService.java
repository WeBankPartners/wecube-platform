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
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.webank.wecube.platform.core.commons.ApplicationProperties.ResourceProperties;
import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.dto.plugin.CreateInstanceDto;
import com.webank.wecube.platform.core.dto.plugin.PluginInstanceDto;
import com.webank.wecube.platform.core.dto.plugin.QueryRequestDto;
import com.webank.wecube.platform.core.dto.plugin.ResourceItemDto;
import com.webank.wecube.platform.core.dto.plugin.ResourceServerDto;
import com.webank.wecube.platform.core.entity.plugin.PluginInstances;
import com.webank.wecube.platform.core.entity.plugin.PluginMysqlInstances;
import com.webank.wecube.platform.core.entity.plugin.PluginPackageRuntimeResourcesDocker;
import com.webank.wecube.platform.core.entity.plugin.PluginPackageRuntimeResourcesMysql;
import com.webank.wecube.platform.core.entity.plugin.PluginPackageRuntimeResourcesS3;
import com.webank.wecube.platform.core.entity.plugin.PluginPackages;
import com.webank.wecube.platform.core.entity.plugin.ResourceItem;
import com.webank.wecube.platform.core.entity.plugin.ResourceServer;
import com.webank.wecube.platform.core.entity.plugin.SystemVariables;
import com.webank.wecube.platform.core.propenc.RsaEncryptor;
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
import com.webank.wecube.platform.core.support.authserver.AuthServerRestClient;
import com.webank.wecube.platform.core.support.authserver.SimpleSubSystemDto;
import com.webank.wecube.platform.core.support.gateway.GatewayResponse;
import com.webank.wecube.platform.core.support.gateway.GatewayServiceStub;
import com.webank.wecube.platform.core.support.gateway.RegisterRouteItemsDto;
import com.webank.wecube.platform.core.support.gateway.RouteItem;
import com.webank.wecube.platform.core.utils.EncryptionUtils;
import com.webank.wecube.platform.core.utils.JsonUtils;
import com.webank.wecube.platform.core.utils.StringUtilsEx;
import com.webank.wecube.platform.core.utils.SystemUtils;
import com.webank.wecube.platform.workflow.commons.LocalIdGenerator;

@Service
public class PluginInstanceMgmtService extends AbstractPluginMgmtService {
    private static final Logger log = LoggerFactory.getLogger(PluginInstanceMgmtService.class);

    private static final String PLUGIN_PROP_ENC_KEY_FILE_PATH = "/certs/plugin_rsa_key.pub";
    private static final String SYS_VAR_PLUGIN_PROP_ENC_KEY_SWITCH = "PLUGIN_PROP_ENC_KEY_SWITCH";

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

    @Autowired
    private SystemVariableService systemVariableService;

    @Autowired
    private GatewayServiceStub gatewayServiceStub;

    @Autowired
    private PluginPackageMenuStatusListener pluginPackageMenuStatusListener;
    
    @Autowired
    private AuthServerRestClient authServerRestClient;

    public void removePluginInstanceById(String instanceId) throws Exception {
        log.info("Removing plugin instance,instanceId: {}", instanceId);
        PluginInstances pluginInstanceEntity = pluginInstancesMapper.selectByPrimaryKey(instanceId);
        if (pluginInstanceEntity == null) {
            log.info("The plugin instance {} does not exist.", instanceId);
            throw new WecubeCoreException("3272","Remove plugin package instance failed.");
        }
        ResourceItemDto removeDockerInstanceDto = new ResourceItemDto();
        removeDockerInstanceDto.setName(pluginInstanceEntity.getContainerName());
        removeDockerInstanceDto.setId(pluginInstanceEntity.getDockerInstanceResourceId());

        try {
            resourceManagementService.deleteItems(Lists.newArrayList(removeDockerInstanceDto));
        } catch (Exception e) {
            log.error("Failed to remove docker resource items.", e);
            throw new WecubeCoreException("3321", "Failed to remove docker resource items.",e.getMessage());
        }

        pluginPackageMenuStatusListener.preRemove(pluginInstanceEntity);
        pluginInstancesMapper.deleteByPrimaryKey(instanceId);
        
        log.info("Plugin instance {} was removed.", instanceId);
    }

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

        try {
            doLaunchPluginInstance(pluginPackageEntity, hostIpAddr, port);
        } catch (Exception e) {
            log.error("errors while launch plugin instance.", e);
            throw new WecubeCoreException("Failed to launch plugin instance:" + e.getMessage());
        }
    }

    private void doLaunchPluginInstance(PluginPackages pluginPackage, String hostIpAddr, Integer port) {
        validateLauchPluginInstanceParameters(pluginPackage, hostIpAddr, port);

        List<PluginPackageRuntimeResourcesDocker> dockerInfoSet = pluginPackageRuntimeResourcesDockerMapper
                .selectAllByPackage(pluginPackage.getId());
        List<PluginPackageRuntimeResourcesMysql> mysqlInfoSet = pluginPackageRuntimeResourcesMysqlMapper
                .selectAllByPackage(pluginPackage.getId());
        List<PluginPackageRuntimeResourcesS3> s3InfoSet = pluginPackageRuntimeResourcesS3Mapper
                .selectAllByPackage(pluginPackage.getId());

        PluginInstances pluginInstanceEntity = new PluginInstances();
        pluginInstanceEntity.setId(LocalIdGenerator.generateId());
        pluginInstanceEntity.setPackageId(pluginPackage.getId());
        pluginInstanceEntity.setPluginPackage(pluginPackage);

        //1. try to create mysql schema

        LocalDatabaseInfo dbInfo = handleCreateDatabase(mysqlInfoSet, pluginPackage);
        if (dbInfo != null) {
            pluginInstanceEntity.setPluginMysqlInstanceResourceId(dbInfo.getResourceItemId());
        }

        //2. try to create s3 bucket
        String s3BucketResourceId = handleCreateS3Bucket(s3InfoSet, pluginPackage);
        if (s3BucketResourceId != null){
            pluginInstanceEntity.setS3bucketResourceId(s3BucketResourceId);
        }

        // 3. create docker instance
        if (dockerInfoSet.size() != 1) {
            throw new WecubeCoreException("3078", "Only support plugin running in one container so far");
        }
        PluginPackageRuntimeResourcesDocker dockerInfo = dockerInfoSet.get(0);

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

            envVariablesString = envVariablesString.replace("{{DB_HOST}}", dbInfo.getHost()) //
                    .replace("{{DB_PORT}}", dbInfo.getPort()) //
                    .replace("{{DB_SCHEMA}}", dbInfo.getSchema()) //
                    .replace("{{DB_USER}}", dbInfo.getUser()) //
                    .replace("{{DB_PWD}}", tryEncryptPasswordAsPluginEnv(password));
        }
        
        SimpleSubSystemDto subSystemAs = tryRegisterSubSystem(pluginPackage);
        if(subSystemAs != null){
            envVariablesString = envVariablesString.replace("{{SUB_SYSTEM_CODE}}", subSystemAs.getSystemCode()) //
                    .replace("{{SUB_SYSTEM_KEY}}", subSystemAs.getApikey());
        }
        
        log.info("before replace envVariablesString=" + envVariablesString);
        envVariablesString = replaceJwtSigningKey(envVariablesString);
        envVariablesString = replaceSystemVariablesForEnvVariables(pluginPackage.getName(), envVariablesString);
        envVariablesString = appendTimeZoneToPluginEnv(envVariablesString);
        log.info("after replace envVariablesString=" + envVariablesString);
        

        createContainerParameters.setEnvVariableParameters(envVariablesString.isEmpty() ? "" : envVariablesString);

        try {
            ResourceItemDto dockerResourceDto = createPluginDockerInstance(pluginPackage, hostIpAddr,
                    createContainerParameters);
            pluginInstanceEntity.setDockerInstanceResourceId(dockerResourceDto.getId());
        } catch (Exception e) {
            log.error("Creating docker container instance meet error: ", e);
            throw new WecubeCoreException("3079", "Creating docker container instance meet error: " + e.getMessage(),
                    e);
        }

        pluginInstanceEntity.setContainerName(dockerInfo.getContainerName());
        pluginInstanceEntity.setInstanceName(pluginPackage.getName());
        pluginInstanceEntity.setHost(hostIpAddr);
        pluginInstanceEntity.setPort(port);

        // 4. insert to DB
        pluginInstanceEntity.setContainerStatus(PluginInstances.CONTAINER_STATUS_RUNNING);

        pluginPackageMenuStatusListener.prePersist(pluginInstanceEntity);
        pluginInstancesMapper.insert(pluginInstanceEntity);

        // pluginInstanceRepository.save(instance);

        // 6. register route
        GatewayResponse response = registerRoute(pluginPackage.getName(), hostIpAddr, String.valueOf(port));
        if (!response.getStatus().equals(GatewayResponse.getStatusCodeOk())) {
            log.error("Launch instance has done, but register routing information is failed, please check");
        }
    }
    
    private String appendTimeZoneToPluginEnv(String envVariablesString){
        String tz = System.getenv("TZ");
        if(StringUtils.isBlank(tz)){
            tz = TimeZone.getDefault().getID();
        }
        
        if(StringUtils.isBlank(envVariablesString)){
            return String.format("TZ=%s", tz);
        }else{
            return envVariablesString+String.format("\\,TZ=%s", tz);
        }
    }
    
    private SimpleSubSystemDto tryRegisterSubSystem(PluginPackages pluginPackage){
        log.info("About to register sub system infomation for {}", pluginPackage.getName());
        SimpleSubSystemDto subSystemReq = new SimpleSubSystemDto();
        String subSystemCode = String.format("SYS_%s", pluginPackage.getName().toUpperCase());
        subSystemReq.setName(pluginPackage.getName());
        subSystemReq.setSystemCode(subSystemCode);
        subSystemReq.setActive(true);
        subSystemReq.setBlocked(false);
        subSystemReq.setDescription(String.format("Plugin %s registered from platform.", pluginPackage.getName()));
        
        
        SimpleSubSystemDto subSystemAs = authServerRestClient.registerSimpleSubSystem(subSystemReq);
        log.info("Finished to register sub system infomation for {}:{}", pluginPackage.getName(), subSystemAs);
        return subSystemAs;
        
    }

    private GatewayResponse registerRoute(String name, String host, String port) {
        List<RouteItem> routeItems = new ArrayList<>();
        RouteItem currRouteItem = new RouteItem(name, "http", host, port);
        routeItems.add(currRouteItem);
        return gatewayServiceStub.registerRoute(new RegisterRouteItemsDto(name, routeItems));
    }

    private ResourceItemDto createPluginDockerInstance(PluginPackages pluginPackage, String hostIp,
            CreateInstanceDto createContainerParameters) throws Exception {
        ResourceServer hostInfo = null;
        List<ResourceServer> hostInfos = resourceServerMapper.selectAllByHostAndType(hostIp,
                ResourceServerType.DOCKER.getCode());
        if (hostInfos.size() == 0) {
            log.info("Can not found docker resource server by IP[{}]", hostIp);

            String errMsg = String.format("Can not found docker resource server by IP[%s]", hostIp);
            throw new WecubeCoreException("3084", errMsg, hostIp);
        }
        hostInfo = hostInfos.get(0);

        // download package from MinIO
        String tmpFolderName = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
        String tmpFilePath = SystemUtils.getTempFolderPath() + tmpFolderName + "/" + pluginProperties.getImageFile();

        String s3KeyName = pluginPackage.getName() + File.separator + pluginPackage.getVersion() + File.separator
                + pluginProperties.getImageFile();
        log.info("Download plugin package from S3: {}", s3KeyName);

        s3Client.downFile(pluginProperties.getPluginPackageBucketName(), s3KeyName, tmpFilePath);

        log.info("scp from local:{} to remote: {}", tmpFilePath, pluginProperties.getPluginDeployPath());
        try {
            String dbPassword = hostInfo.getLoginPassword();
            if (dbPassword.startsWith(ResourceManagementService.PASSWORD_ENCRYPT_AES_PREFIX)) {
                dbPassword = dbPassword.substring(ResourceManagementService.PASSWORD_ENCRYPT_AES_PREFIX.length());
            }

            String password = EncryptionUtils.decryptWithAes(dbPassword, resourceProperties.getPasswordEncryptionSeed(),
                    hostInfo.getName());
            scpService.put(hostIp, Integer.valueOf(hostInfo.getPort()), hostInfo.getLoginUsername(), password,
                    tmpFilePath, pluginProperties.getPluginDeployPath());
        } catch (Exception e) {
            log.error("Put file to remote host meet error", e);
            throw new WecubeCoreException("3085",
                    String.format("Put file to remote host meet error:%s ", e.getMessage()), e);
        }

        // load image at remote host
        String loadCmd = "docker load -i " + pluginProperties.getPluginDeployPath().trim() + File.separator
                + pluginProperties.getImageFile();
        log.info("Run docker load command: " + loadCmd);
        try {
            String loginPassword = hostInfo.getLoginPassword();
            if (loginPassword.startsWith(ResourceManagementService.PASSWORD_ENCRYPT_AES_PREFIX)) {
                loginPassword = EncryptionUtils.decryptWithAes(
                        loginPassword.substring(ResourceManagementService.PASSWORD_ENCRYPT_AES_PREFIX.length()),
                        resourceProperties.getPasswordEncryptionSeed(), hostInfo.getName());
            }
            commandService.runAtRemote(hostIp, hostInfo.getLoginUsername(), loginPassword,
                    Integer.valueOf(hostInfo.getPort()), loadCmd);
        } catch (Exception e) {
            log.error("Run command [{}] meet error: {}", loadCmd, e.getMessage());
            throw new WecubeCoreException("3086", String.format("Run remote command meet error: %s", e.getMessage()),
                    e);
        }

        ResourceItemDto createDockerInstanceDto = new ResourceItemDto(createContainerParameters.getContainerName(),
                ResourceItemType.DOCKER_CONTAINER.getCode(),
                buildAdditionalPropertiesForDocker(createContainerParameters), hostInfo.getId(),
                String.format("Create docker instance for plugin[%s]", pluginPackage.getName()));
        createDockerInstanceDto.setIsAllocated(true);
        log.info("Container creating...");
        log.info("Request parameters= " + createDockerInstanceDto.toString());

        List<ResourceItemDto> result = resourceManagementService
                .createItems(Lists.newArrayList(createDockerInstanceDto));

        log.info("Container creation has done...");
        return result.get(0);
    }

    private String buildAdditionalPropertiesForDocker(CreateInstanceDto createContainerParameters) {
        HashMap<String, String> additionalProperties = new HashMap<String, String>();
        additionalProperties.put("imageName", createContainerParameters.getImageName());
        additionalProperties.put("portBindings", createContainerParameters.getPortBindingParameters());
        additionalProperties.put("volumeBindings", createContainerParameters.getVolumeBindingParameters());
        additionalProperties.put("envVariables", createContainerParameters.getEnvVariableParameters());

        return JsonUtils.toJsonString(additionalProperties);
    }

    private String tryEncryptPasswordAsPluginEnv(String rawPassword) {
        if (StringUtils.isBlank(rawPassword)) {
            return rawPassword;
        }

        List<SystemVariables> pluginPropEncKeyFileSysVars = systemVariableService
                .getGlobalSystemVariableByName(SYS_VAR_PLUGIN_PROP_ENC_KEY_SWITCH);
        String propEncSwitchOn = "on";
        String propEncSwitchOnConfig = null;
        if (pluginPropEncKeyFileSysVars != null && !pluginPropEncKeyFileSysVars.isEmpty()) {
            SystemVariables pluginPropEncKeyFileSysVar = pluginPropEncKeyFileSysVars.get(0);
            propEncSwitchOnConfig = pluginPropEncKeyFileSysVar.getValue();
            if (StringUtils.isBlank(propEncSwitchOnConfig)) {
                propEncSwitchOnConfig = pluginPropEncKeyFileSysVar.getDefaultValue();
            }
        }

        if (!StringUtils.isBlank(propEncSwitchOnConfig)) {
            propEncSwitchOn = propEncSwitchOnConfig;
        }

        if ("off".equalsIgnoreCase(propEncSwitchOn)) {
            log.info("property encryption was switched off by system variable:{}", SYS_VAR_PLUGIN_PROP_ENC_KEY_SWITCH);
            return rawPassword;
        }

        File rsaPubKeyFile = new File(PLUGIN_PROP_ENC_KEY_FILE_PATH);
        if (!rsaPubKeyFile.exists()) {
            log.info("plugin property encryption not applied as file not exist.Filepath={}",
                    PLUGIN_PROP_ENC_KEY_FILE_PATH);
            return rawPassword;
        }

        String rsaPubKeyAsString = null;
        try (FileInputStream input = new FileInputStream(rsaPubKeyFile)) {
            rsaPubKeyAsString = readInputStream(input);
        } catch (IOException e) {
            log.info("errors while reading public key", e);
        }

        if (StringUtils.isBlank(rsaPubKeyAsString)) {
            log.info("plugin property encryption not applied as key not available.Filepath={}",
                    PLUGIN_PROP_ENC_KEY_FILE_PATH);
            return rawPassword;
        }

        byte[] cipheredPasswordData = RsaEncryptor.encryptByPublicKey(
                rawPassword.getBytes(Charset.forName(RsaEncryptor.DEF_ENCODING)), rsaPubKeyAsString);
        String cipheredPassword = RsaEncryptor.encodeBase64String(cipheredPasswordData);
        return "RSA@" + cipheredPassword;
    }

    private String replaceJwtSigningKey(String envVariablesString) {
        if (StringUtils.isBlank(envVariablesString)) {
            return envVariablesString;
        }

        String jwtSigningKey = applicationProperties.getJwtSigningKey();
        if (StringUtils.isBlank(jwtSigningKey)) {
            jwtSigningKey = "";
        }

        return envVariablesString.replace("{{JWT_SIGNING_KEY}}", jwtSigningKey);
    }

    private String replaceAllocatePort(String str, Integer allocatePort) {
        String result = str.replace("{{ALLOCATE_PORT}}", String.valueOf(allocatePort));
        result = result.replace("{{MONITOR_PORT}}", String.valueOf(allocatePort + 10000));
        return result;
    }

    private String replaceHostIp(String str, String ip) {
        return str.replace("{{ALLOCATE_HOST}}", ip);
    }

    private String replaceBaseMountPath(String baseMountPathString) {
        return systemVariableService.variableReplacement(null, baseMountPathString);
    }

    private String replaceSystemVariablesForEnvVariables(String packageName, String str) {
        return systemVariableService.variableReplacement(packageName, str);
    }

    private String handleCreateS3Bucket(List<PluginPackageRuntimeResourcesS3> s3ResourceInfoList,
            PluginPackages pluginPackage) {
        if (s3ResourceInfoList == null || s3ResourceInfoList.isEmpty()) {
            return null;
        }
        if (s3ResourceInfoList.size() > 1) {
            log.error("Apply {} s3 buckets is not allow", s3ResourceInfoList.size());
            String errMsg = String.format("Apply [%d] s3 buckets is not allow", s3ResourceInfoList.size());
            throw new WecubeCoreException("3076", errMsg, s3ResourceInfoList.size());
        }

        PluginPackageRuntimeResourcesS3 s3ResourceInfo = s3ResourceInfoList.get(0);

        List<ResourceItem> s3BucketsItemEntities = resourceItemMapper
                .selectAllByNameAndType(s3ResourceInfo.getBucketName(), ResourceItemType.S3_BUCKET.getCode());
        if (s3BucketsItemEntities.size() > 0) {
            return s3BucketsItemEntities.get(0).getId();
        } else {
            return initS3BucketResource(s3ResourceInfo);
        }
    }

    private String initS3BucketResource(PluginPackageRuntimeResourcesS3 s3ResourceInfo) {
        return createPluginS3Bucket(s3ResourceInfo);
    }

    private String createPluginS3Bucket(PluginPackageRuntimeResourcesS3 s3ResourceInfo) {
        QueryRequestDto queryRequest = QueryRequestDto.defaultQueryObject("type", ResourceServerType.S3.getCode());
        List<ResourceServerDto> s3Servers = resourceManagementService.retrieveServers(queryRequest).getContents();
        if (s3Servers.size() == 0) {
            throw new WecubeCoreException("3083", "Can not found available resource server for creating s3 bucket");
        }
        ResourceServerDto s3Server = s3Servers.get(0);
        ResourceItemDto createS3BucketDto = new ResourceItemDto(s3ResourceInfo.getBucketName(),
                ResourceItemType.S3_BUCKET.getCode(), null, s3Server.getId(),
                String.format("Create S3 bucket for plugin[%s]", s3ResourceInfo.getBucketName()));
        createS3BucketDto.setResourceServer(s3Server);
        createS3BucketDto.setIsAllocated(true);
        log.info("S3 bucket creating...");
        if (log.isInfoEnabled()) {
            log.info("Request parameters= " + createS3BucketDto);
        }

        List<ResourceItemDto> resultResourceItemDtos = resourceManagementService
                .createItems(Lists.newArrayList(createS3BucketDto));

        if (resultResourceItemDtos == null || resultResourceItemDtos.isEmpty()) {
            log.error("failed to create S3 resource item.");
            return null;
        }
        log.info("S3 bucket creation has done...");
        return resultResourceItemDtos.get(0).getId();
    }

    private LocalDatabaseInfo handleCreateDatabase(List<PluginPackageRuntimeResourcesMysql> mysqlInfoResourceEntities,
            PluginPackages pluginPackage) {
        if (mysqlInfoResourceEntities == null || mysqlInfoResourceEntities.isEmpty()) {
            return null;
        }
        if (mysqlInfoResourceEntities.size() > 1) {
            log.error("Apply [{}] schema is not allow", mysqlInfoResourceEntities.size());
            throw new WecubeCoreException("3073", "Only allow to apply one MYSQL instance so far.");
        }

        List<PluginMysqlInstances> mysqlInstancesEntities = pluginMysqlInstancesMapper.selectAllByPackageNameAndStatus(
                pluginPackage.getName(), PluginMysqlInstances.MYSQL_INSTANCE_STATUS_ACTIVE);

        if (mysqlInstancesEntities == null || mysqlInstancesEntities.isEmpty()) {
            // new mysql instance
            // TODDO
            LocalDatabaseInfo newLocalDatabaseInfo = tryInitMysqlDatabaseSchema(mysqlInfoResourceEntities,
                    pluginPackage);
            return newLocalDatabaseInfo;
        }

        if (mysqlInstancesEntities.size() > 1) {
            // fixme: throw exception ?
        }

        PluginMysqlInstances mysqlInstancesEntity = mysqlInstancesEntities.get(0);

        LocalDatabaseInfo updatedLocalDatabaseInfo = tryHandleExistMysqlInstance(mysqlInstancesEntity, pluginPackage);

        return updatedLocalDatabaseInfo;
    }

    private LocalDatabaseInfo tryHandleExistMysqlInstance(PluginMysqlInstances mysqlInstance,
            PluginPackages pluginPackage) {
        // already exists
        //
        log.info("Mysql instance already existed for {} and try to process upgrading.", pluginPackage.getName());
        ResourceItem resourceItemEntity = resourceItemMapper.selectByPrimaryKey(mysqlInstance.getResourceItemId());
        ResourceServer resourceServerEntity = resourceServerMapper
                .selectByPrimaryKey(resourceItemEntity.getResourceServerId());
        tryUpgradeMysqlDatabaseData(mysqlInstance, pluginPackage, resourceItemEntity, resourceServerEntity);
        if (StringUtils.isBlank(mysqlInstance.getPreVersion())) {
            mysqlInstance.setPreVersion(pluginPackage.getVersion());
        }
        int versionCompare = versionComparator.compare(pluginPackage.getVersion(), mysqlInstance.getPreVersion());
        if (versionCompare >= 0) {
            mysqlInstance.setPreVersion(pluginPackage.getVersion());
        }
        mysqlInstance.setUpdatedTime(new Date());

        pluginMysqlInstancesMapper.updateByPrimaryKeySelective(mysqlInstance);
        // pluginMysqlInstanceRepository.save(mysqlInstance);
        // ResourceServerDomain resourceServer =
        // mysqlInstance.getResourceItem().getResourceServer();
        return new LocalDatabaseInfo(resourceServerEntity.getHost(), resourceServerEntity.getPort(),
                mysqlInstance.getSchemaName(), mysqlInstance.getUsername(), mysqlInstance.getPassword(),
                mysqlInstance.getResourceItemId());
    }

    private LocalDatabaseInfo tryInitMysqlDatabaseSchema(List<PluginPackageRuntimeResourcesMysql> mysqlResources,
            PluginPackages pluginPackage) {
        log.info("Mysql instance does not exist for {} and try to process initializing.", pluginPackage.getName());

        if (mysqlResources.size() > 0) {
            PluginMysqlInstances mysqlInstance = tryCreatePluginMysqlDatabase(mysqlResources.get(0),
                    pluginPackage.getVersion(), pluginPackage);

            ResourceItem resourceItemEntity = resourceItemMapper.selectByPrimaryKey(mysqlInstance.getResourceItemId());
            ResourceServer resourceServerEntity = resourceServerMapper
                    .selectByPrimaryKey(resourceItemEntity.getResourceServerId());
            // ResourceServerDomain dbServer =
            // resourceItemRepository.findById(mysqlInstance.getResourceItemId()).get()
            // .getResourceServer();
            LocalDatabaseInfo dbInfo = new LocalDatabaseInfo(resourceServerEntity.getHost(),
                    resourceServerEntity.getPort(), mysqlInstance.getSchemaName(), mysqlInstance.getUsername(),
                    mysqlInstance.getPassword(), mysqlInstance.getId());

            // execute init.sql
            tryInitMysqlDatabaseTables(resourceServerEntity, mysqlInstance, pluginPackage);
            return dbInfo;
        } else {
            log.warn("mysql resources is empty for {}", pluginPackage.getName());
            return null;
        }
    }

    // execute init.sql
    private void tryInitMysqlDatabaseTables(ResourceServer dbServer, PluginMysqlInstances mysqlInstance,
            PluginPackages pluginPackage) {

        String tmpFolderName = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
        String initSqlPath = SystemUtils.getTempFolderPath() + tmpFolderName + "/" + pluginProperties.getInitDbSql();

        String s3KeyName = pluginPackage.getName() + File.separator + pluginPackage.getVersion() + File.separator
                + pluginProperties.getInitDbSql();
        log.info("Download init.sql from S3: {} {}", s3KeyName, initSqlPath);

        s3Client.downFile(pluginProperties.getPluginPackageBucketName(), s3KeyName, initSqlPath);

        String password = mysqlInstance.getPassword();
        if (password.startsWith(ResourceManagementService.PASSWORD_ENCRYPT_AES_PREFIX)) {
            password = password.substring(ResourceManagementService.PASSWORD_ENCRYPT_AES_PREFIX.length());
        }

        password = EncryptionUtils.decryptWithAes(password, resourceProperties.getPasswordEncryptionSeed(),
                mysqlInstance.getSchemaName());

        DriverManagerDataSource dataSource = new DriverManagerDataSource(
                "jdbc:mysql://" + dbServer.getHost() + ":" + dbServer.getPort() + "/" + mysqlInstance.getSchemaName()
                        + "?characterEncoding=utf8&serverTimezone=UTC",
                mysqlInstance.getUsername(), password);
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        File initSqlFile = new File(initSqlPath);
        List<Resource> scipts = newArrayList(new FileSystemResource(initSqlFile));
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.setContinueOnError(false);
        populator.setIgnoreFailedDrops(false);
        populator.setSeparator(";");
        scipts.forEach(populator::addScript);
        try {
            populator.execute(dataSource);
        } catch (Exception e) {
            String errorMessage = String.format("Failed to execute init.sql for schema[%s]",
                    mysqlInstance.getSchemaName());
            log.error(errorMessage, e);
            throw new WecubeCoreException("3080", errorMessage, e);
        }
        log.info(String.format("Init database[%s] tables has done..", mysqlInstance.getSchemaName()));
    }

    private PluginMysqlInstances tryCreatePluginMysqlDatabase(PluginPackageRuntimeResourcesMysql mysqlResourceInfo,
            String currentPluginVersion, PluginPackages pluginPackage) {

        List<ResourceServer> mysqlResourceServerEntities = resourceServerMapper
                .selectAllByType(ResourceServerType.MYSQL.getCode());
        if (mysqlResourceServerEntities == null || mysqlResourceServerEntities.isEmpty()) {
            log.error("Cannot find any mysql resource server currently.");
            throw new WecubeCoreException("3082",
                    "Can not found available resource server for creating mysql database");
        }
        ResourceServer mysqlResourceServerEntity = mysqlResourceServerEntities.get(0);

        String dbPassword = genRandomPassword();
        String dbUser = mysqlResourceInfo.getSchemaName();

        String resItemName = mysqlResourceInfo.getSchemaName();
        String resItemType = ResourceItemType.MYSQL_DATABASE.getCode();
        String resItemAdditionalProperties = buildAdditionalPropertiesForMysqlDatabase(
                dbUser.length() > 16 ? dbUser.substring(0, 16) : dbUser, dbPassword);
        String resItemResourceServerId = mysqlResourceServerEntity.getId();
        String resItemPurpose = String.format("Create MySQL database for plugin[%s]",
                mysqlResourceInfo.getSchemaName());
        ResourceItemDto createMysqlDto = new ResourceItemDto(resItemName, resItemType, resItemAdditionalProperties,
                resItemResourceServerId, resItemPurpose);
        // mysqlServer.setResourceItemDtos(null);
        // createMysqlDto.setResourceServer(mysqlServer);
        createMysqlDto.setIsAllocated(true);
        log.info("Mysql Database schema creating...");
        if (log.isInfoEnabled()) {
            log.info("Request parameters= " + createMysqlDto);
        }

        dbPassword = ResourceManagementService.PASSWORD_ENCRYPT_AES_PREFIX + EncryptionUtils.encryptWithAes(dbPassword,
                resourceProperties.getPasswordEncryptionSeed(), mysqlResourceInfo.getSchemaName());

        List<ResourceItemDto> result = resourceManagementService.createItems(Lists.newArrayList(createMysqlDto));

        PluginMysqlInstances newMysqlInstanceEntity = new PluginMysqlInstances();
        newMysqlInstanceEntity.setId(LocalIdGenerator.generateId());
        newMysqlInstanceEntity.setCreatedTime(new Date());
        newMysqlInstanceEntity.setPassword(dbPassword);
        newMysqlInstanceEntity.setPlugunPackageId(pluginPackage.getId());
        newMysqlInstanceEntity.setPreVersion(currentPluginVersion);
        newMysqlInstanceEntity.setResourceItemId(result.get(0).getId());
        newMysqlInstanceEntity.setSchemaName(mysqlResourceInfo.getSchemaName());
        newMysqlInstanceEntity.setStatus(PluginMysqlInstances.MYSQL_INSTANCE_STATUS_ACTIVE);
        newMysqlInstanceEntity.setUsername(dbUser);

        pluginMysqlInstancesMapper.insert(newMysqlInstanceEntity);

        log.info("Mysql Database schema creation has done...");
        return newMysqlInstanceEntity;
    }

    private String buildAdditionalPropertiesForMysqlDatabase(String username, String password) {
        HashMap<String, String> additionalProperties = new HashMap<String, String>();
        additionalProperties.put("username", username);
        additionalProperties.put("password", password);
        return JsonUtils.toJsonString(additionalProperties);
    }

    private void tryUpgradeMysqlDatabaseData(PluginMysqlInstances mysqlInstance, PluginPackages pluginPackage,
            ResourceItem resourceItem, ResourceServer resourceServer) {
        String latestVersion = mysqlInstance.getPreVersion();
        if (!shouldUpgradeMysqlDatabaseData(latestVersion, pluginPackage.getVersion())) {
            log.info("latest version {} and current version {}, no need to upgrade.", latestVersion,
                    pluginPackage.getVersion());
            return;
        }

        if (isStringBlank(latestVersion)) {
            latestVersion = pluginPackage.getVersion();
        }

        log.info("try to upgrade databaase, latest version {} and current version {}, need to upgrade.",
                latestVersion, pluginPackage.getVersion());

        try {
            log.info("try to perform database upgrade for {} {}", pluginPackage.getName(), pluginPackage.getVersion());
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

        // ResourceServerDomain dbServer =
        // resourceItemRepository.findById(mysqlInstance.getResourceItemId()).get()
        // .getResourceServer();
        String password = mysqlInstance.getPassword();
        if (password.startsWith(ResourceManagementService.PASSWORD_ENCRYPT_AES_PREFIX)) {
            password = password.substring(ResourceManagementService.PASSWORD_ENCRYPT_AES_PREFIX.length());
        }
        password = EncryptionUtils.decryptWithAes(password, resourceProperties.getPasswordEncryptionSeed(),
                mysqlInstance.getSchemaName());
        DriverManagerDataSource dataSource = new DriverManagerDataSource(
                "jdbc:mysql://" + resourceServer.getHost() + ":" + resourceServer.getPort() + "/"
                        + mysqlInstance.getSchemaName() + "?characterEncoding=utf8&serverTimezone=UTC",
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
            log.info("start to execute sql script file:{}, host:{},port:{},schema:{}", upgradeSqlFile.getAbsolutePath(),
                    resourceServer.getHost(), resourceServer.getPort(), mysqlInstance.getSchemaName());
            populator.execute(dataSource);
        } catch (Exception e) {
            String errorMessage = String.format("Failed to execute [%s] for schema[%s]", upgradeSqlFile.getName(),
                    mysqlInstance.getSchemaName());
            log.error(errorMessage, e);
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
                    log.warn("", e);
                }
            }

            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException e) {
                    log.warn("", e);
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

    private void validateLauchPluginInstanceParameters(PluginPackages pluginPackage, String hostIpAddr, Integer port) {
        if (!isContainerHostValid(hostIpAddr)) {
            throw new WecubeCoreException("3070", "Unavailable container host ip");
        }

        if (!isPortValid(hostIpAddr, port)) {
            String errMsg = String.format(
                    "The port[%s] of host[%s] is already in used, please try to reassignment port", port, hostIpAddr);
            throw new WecubeCoreException("3071", errMsg, port, hostIpAddr);
        }

        if (PluginPackages.DECOMMISSIONED.equals(pluginPackage.getStatus())
                || PluginPackages.UNREGISTERED.equals(pluginPackage.getStatus())) {
            throw new WecubeCoreException("3072",
                    "'DECOMMISSIONED' or 'UNREGISTERED' state can not launch plugin instance ");
        }
    }

    private boolean isPortValid(String hostIp, Integer port) {
        List<PluginInstances> pluginInstances = pluginInstancesMapper.selectAllByHostAndPortAndStatus(hostIp, port,
                PluginInstances.CONTAINER_STATUS_RUNNING);
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
