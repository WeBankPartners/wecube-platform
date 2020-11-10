package com.webank.wecube.platform.core.service.plugin;

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;

import com.google.common.collect.Sets;
import com.webank.wecube.platform.core.commons.ApplicationProperties.PluginProperties;
import com.webank.wecube.platform.core.commons.AuthenticationContextHolder;
import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.dto.plugin.UploadPackageResultDto;
import com.webank.wecube.platform.core.dto.user.RoleDto;
import com.webank.wecube.platform.core.entity.plugin.PluginConfigRoles;
import com.webank.wecube.platform.core.entity.plugin.PluginConfigs;
import com.webank.wecube.platform.core.entity.plugin.PluginPackageAttributes;
import com.webank.wecube.platform.core.entity.plugin.PluginPackageDataModel;
import com.webank.wecube.platform.core.entity.plugin.PluginPackageEntities;
import com.webank.wecube.platform.core.entity.plugin.PluginPackageResourceFiles;
import com.webank.wecube.platform.core.entity.plugin.PluginPackages;
import com.webank.wecube.platform.core.entity.plugin.SystemVariables;
import com.webank.wecube.platform.core.parser.PluginConfigXmlValidator;
import com.webank.wecube.platform.core.parser.PluginPackageDataModelValidator;
import com.webank.wecube.platform.core.parser.PluginPackageValidator;
import com.webank.wecube.platform.core.repository.plugin.PluginConfigRolesMapper;
import com.webank.wecube.platform.core.repository.plugin.PluginConfigsMapper;
import com.webank.wecube.platform.core.repository.plugin.PluginPackageAttributesMapper;
import com.webank.wecube.platform.core.repository.plugin.PluginPackageDataModelMapper;
import com.webank.wecube.platform.core.repository.plugin.PluginPackageEntitiesMapper;
import com.webank.wecube.platform.core.repository.plugin.PluginPackageResourceFilesMapper;
import com.webank.wecube.platform.core.repository.plugin.PluginPackagesMapper;
import com.webank.wecube.platform.core.repository.plugin.SystemVariablesMapper;
import com.webank.wecube.platform.core.service.CommandService;
import com.webank.wecube.platform.core.service.ScpService;
import com.webank.wecube.platform.core.service.plugin.xml.register.AttributeType;
import com.webank.wecube.platform.core.service.plugin.xml.register.DataModelType;
import com.webank.wecube.platform.core.service.plugin.xml.register.EntityType;
import com.webank.wecube.platform.core.service.plugin.xml.register.InterfaceType;
import com.webank.wecube.platform.core.service.plugin.xml.register.PackageType;
import com.webank.wecube.platform.core.service.plugin.xml.register.PluginType;
import com.webank.wecube.platform.core.service.plugin.xml.register.PluginsType;
import com.webank.wecube.platform.core.service.plugin.xml.register.RoleBindType;
import com.webank.wecube.platform.core.service.plugin.xml.register.RoleBindsType;
import com.webank.wecube.platform.core.service.plugin.xml.register.SystemParameterType;
import com.webank.wecube.platform.core.service.plugin.xml.register.SystemParametersType;
import com.webank.wecube.platform.core.service.user.UserManagementService;
import com.webank.wecube.platform.core.support.S3Client;
import com.webank.wecube.platform.core.utils.JaxbUtils;
import com.webank.wecube.platform.core.utils.SystemUtils;
import com.webank.wecube.platform.workflow.commons.LocalIdGenerator;

public class PluginPackageMgmtService {
    private static final Logger log = LoggerFactory.getLogger(PluginPackageMgmtService.class);

    public static final Set<String> ACCEPTED_FILES = Sets.newHashSet("register.xml", "image.tar", "ui.zip", "init.sql",
            "upgrade.sql");

    public static final String SYS_VAR_PUBLIC_PLUGIN_ARTIFACTS_RELEASE_URL = "PLUGIN_ARTIFACTS_RELEASE_URL";

    public static final String PLATFORM_NAME = "platform";

    private static final String DEFAULT_USER = "sys";

    @Autowired
    private PluginProperties pluginProperties;

    @Autowired
    private S3Client s3Client;
    @Autowired
    private ScpService scpService;
    @Autowired
    private CommandService commandService;

    @Autowired
    private PluginPackageValidator pluginPackageValidator;

    @Autowired
    private PluginPackageDataModelValidator dataModelValidator;

    @Autowired
    private PluginPackagesMapper pluginPackagesMapper;

    @Autowired
    private PluginConfigsMapper pluginConfigsMapper;

    @Autowired
    private PluginConfigRolesMapper pluginConfigRolesMapper;
    
    @Autowired
    private SystemVariablesMapper systemVariablesMapper;
    
    @Autowired
    private PluginPackageDataModelMapper pluginPackageDataModelMapper;
    
    @Autowired
    private PluginPackageEntitiesMapper pluginPackageEntitiesMapper;
    
    @Autowired
    private PluginPackageAttributesMapper pluginPackageAttributesMapper;
    
    @Autowired
    private PluginPackageResourceFilesMapper pluginPackageResourceFilesMapper;

    @Autowired
    private UserManagementService userManagementService;

    public UploadPackageResultDto uploadPackage(MultipartFile pluginPackageFile) {
        String pluginPackageFileName = pluginPackageFile.getName();

        // 1. save package file to local
        String tmpFileName = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
        File localFilePath = new File(SystemUtils.getTempFolderPath() + tmpFileName + "/");
        log.info("tmp File Path= {}", localFilePath.getName());
        if (!localFilePath.exists()) {
            if (localFilePath.mkdirs()) {
                log.info("Create directory [{}] successful", localFilePath.getAbsolutePath());
            } else {
                throw new WecubeCoreException("3099",
                        String.format("Create directory [%s] failed.", localFilePath.getAbsolutePath()),
                        localFilePath.getAbsolutePath());
            }
        }

        File dest = new File(localFilePath + "/" + pluginPackageFileName);
        try {
            log.info("new file location: {}, filename: {}, canonicalpath: {}, canonicalfilename: {}",
                    dest.getAbsoluteFile(), dest.getName(), dest.getCanonicalPath(), dest.getCanonicalFile().getName());
            pluginPackageFile.transferTo(dest);
        } catch (IOException e) {
            log.error("errors to transfer uploaded files.", e);
            throw new WecubeCoreException("Failed to upload package,due to " + e.getMessage());
        }

        UploadPackageResultDto result = null;
        try {
            result = parsePackageFile(dest, localFilePath);
            log.info("Package uploaded successfully.");
        } catch (IOException | SAXException e) {
            log.error("Errors to parse uploaded package file.", e);
            throw new WecubeCoreException("Failed to upload package due to "+e.getMessage());
        }
        
        //TODO to remove tmp files

        return result;
    }

    private UploadPackageResultDto parsePackageFile(File dest, File localFilePath) throws IOException, SAXException {
        // 2. unzip local package file
        unzipLocalFile(dest.getCanonicalPath(), localFilePath.getCanonicalPath() + "/");

        // 3. read xml file in plugin package
        File registerXmlFile = new File(localFilePath.getCanonicalPath() + "/" + pluginProperties.getRegisterFile());
        if (!registerXmlFile.exists()) {
            throw new WecubeCoreException("3114", String.format("Plugin package definition file: [%s] does not exist.",
                    pluginProperties.getRegisterFile()), pluginProperties.getRegisterFile());
        }

        FileInputStream registerXmlFileFis = null;
        try {
            registerXmlFileFis = new FileInputStream(registerXmlFile);
            new PluginConfigXmlValidator().validate(registerXmlFileFis);
        } finally {
            closeSilently(registerXmlFileFis);
        }

        String registerXmlDataAsStr = null;
        try {
            registerXmlFileFis = new FileInputStream(registerXmlFile);
            registerXmlDataAsStr = IOUtils.toString(registerXmlFileFis, Charset.forName("utf-8"));
        } finally {
            closeSilently(registerXmlFileFis);
        }

        PackageType xmlPackage = JaxbUtils.convertToObject(registerXmlDataAsStr, PackageType.class);

        pluginPackageValidator.validatePackage(xmlPackage);
        dataModelValidator.validateDataModel(xmlPackage.getDataModel());

        if (isPluginPackageExists(xmlPackage.getName(), xmlPackage.getVersion())) {
            throw new WecubeCoreException("3115", String.format("Plugin package [name=%s, version=%s] exists.",
                    xmlPackage.getName(), xmlPackage.getVersion()), xmlPackage.getName(), xmlPackage.getVersion());
        }

        processPluginDockerImageFile(localFilePath, xmlPackage);

        PluginPackages pluginPackageEntity = new PluginPackages();
        pluginPackageEntity.setId(LocalIdGenerator.generateId());
        pluginPackageEntity.setName(xmlPackage.getName());
        pluginPackageEntity.setVersion(xmlPackage.getVersion());

        List<PluginPackageResourceFiles> pluginPackageResourceFilesEntities = processPluginUiPackageFile(localFilePath,
                xmlPackage, pluginPackageEntity);
        
        trySavePluginPackageResourceFiles(pluginPackageResourceFilesEntities);

        processPluginInitSqlFile(localFilePath, xmlPackage);
        processPluginUpgradeSqlFile(localFilePath, xmlPackage);

        pluginPackageEntity.setStatus(PluginPackages.UNREGISTERED);
        pluginPackageEntity.setUploadTimestamp(new Date());

        pluginPackagesMapper.insert(pluginPackageEntity);

        processPluginConfigs(xmlPackage.getPlugins(), xmlPackage, pluginPackageEntity);

        processSystemVaraibles(xmlPackage.getSystemParameters(), xmlPackage, pluginPackageEntity);

        processDataModels(xmlPackage.getDataModel(),xmlPackage, pluginPackageEntity);
        
        UploadPackageResultDto result = new UploadPackageResultDto();
        result.setId(pluginPackageEntity.getId());
        result.setName(pluginPackageEntity.getName());
        result.setStatus(pluginPackageEntity.getStatus());
        result.setVersion(pluginPackageEntity.getVersion());
        result.setUiPackageIncluded(pluginPackageEntity.getUiPackageIncluded());
        

        return result;
    }
    
    private void trySavePluginPackageResourceFiles(List<PluginPackageResourceFiles> pluginPackageResourceFilesEntities){
        if(pluginPackageResourceFilesEntities == null || pluginPackageResourceFilesEntities.isEmpty()){
            return;
        }
        
        for(PluginPackageResourceFiles fileEntity : pluginPackageResourceFilesEntities){
            pluginPackageResourceFilesMapper.insert(fileEntity);
        }
    }
    
    private void processDataModels(DataModelType xmlDataModel, PackageType xmlPackage,
            PluginPackages pluginPackageEntity){
        if(xmlDataModel == null){
            return;
        }
        
        //TODO
        PluginPackageDataModel dataModelEntity = new PluginPackageDataModel();
        dataModelEntity.setId(LocalIdGenerator.generateId());
        dataModelEntity.setIsDynamic(false);
        dataModelEntity.setPackageName(xmlPackage.getName());
        dataModelEntity.setUpdateMethod(xmlDataModel.getMethod());
        dataModelEntity.setUpdatePath(xmlDataModel.getPath());
        dataModelEntity.setUpdateSource(PluginPackageDataModel.PLUGIN_PACKAGE);
        dataModelEntity.setUpdateTime(System.currentTimeMillis());
        //TODO
        dataModelEntity.setVersion(0);
        pluginPackageDataModelMapper.insert(dataModelEntity);
        
        List<EntityType> xmlEntityList = xmlDataModel.getEntity();
        if(xmlEntityList == null || xmlEntityList.isEmpty()){
            return;
        }
        
        for(EntityType xmlEntity : xmlEntityList){
            PluginPackageEntities entity = new PluginPackageEntities();
            entity.setDataModelId(dataModelEntity.getId());
            //TODO
            entity.setDataModelVersion(0);
            entity.setDescription(xmlEntity.getDescription());
            entity.setDisplayName(xmlEntity.getDisplayName());
            entity.setId(LocalIdGenerator.generateId());
            entity.setName(xmlEntity.getName());
            entity.setPackageName(xmlPackage.getName());
            
            pluginPackageEntitiesMapper.insert(entity);
            
            
            List<AttributeType> xmlAttributeList =  xmlEntity.getAttribute();
            if(xmlAttributeList == null || xmlAttributeList.isEmpty()){
                continue;
            }
            
            for(AttributeType xmlAttribute : xmlAttributeList){
                //TODO
                PluginPackageAttributes attributeEntity = new PluginPackageAttributes();
                attributeEntity.setId(LocalIdGenerator.generateId());
                attributeEntity.setDataType(xmlAttribute.getDatatype());
                attributeEntity.setDescription(xmlAttribute.getDescription());
                attributeEntity.setEntityId(entity.getId());
                attributeEntity.setName(xmlAttribute.getName());
                
                //TODO
                String referenceId = null;
                attributeEntity.setReferenceId(referenceId);
                
                pluginPackageAttributesMapper.insert(attributeEntity);
            }
        }
        
    }
    
    private void processSystemVaraibles(SystemParametersType xmlSystemParameters, PackageType xmlPackage,
            PluginPackages pluginPackageEntity){
        if(xmlSystemParameters == null ){
            return;
        }
        
        List<SystemParameterType> xmlSystemParameterList = xmlSystemParameters.getSystemParameter();
        if(xmlSystemParameterList == null || xmlSystemParameterList.isEmpty()){
            return;
        }
        
        for(SystemParameterType xmlSystemParameter : xmlSystemParameterList){
            SystemVariables systemVariableEntity = new SystemVariables();
            systemVariableEntity.setId(LocalIdGenerator.generateId());
            systemVariableEntity.setName(xmlSystemParameter.getName());
            systemVariableEntity.setPackageName(xmlPackage.getName());
            //TODO
            systemVariableEntity.setScope(xmlSystemParameter.getScopeType());
            systemVariableEntity.setDefaultValue(xmlSystemParameter.getDefaultValue());
            systemVariableEntity.setValue(xmlSystemParameter.getValue());
            systemVariableEntity.setStatus(SystemVariables.INACTIVE);
            systemVariableEntity.setSource(pluginPackageEntity.getId());
            systemVariableEntity.setPackageName(xmlPackage.getName());
            
            systemVariablesMapper.insert(systemVariableEntity);
        }
    }

    private void processPluginConfigs(PluginsType xmlPlugins, PackageType xmlPackage,
            PluginPackages pluginPackageEntity) {
        if (xmlPlugins == null) {
            return;
        }

        List<PluginType> xmlPluginList = xmlPlugins.getPlugin();
        if (xmlPluginList == null || xmlPluginList.isEmpty()) {
            return;
        }

        for (PluginType xmlPlugin : xmlPluginList) {
            processSinglePlugin(xmlPlugin, pluginPackageEntity);
        }
    }

    private void processSinglePlugin(PluginType xmlPlugin, PluginPackages pluginPackageEntity) {
        PluginConfigs pluginConfigEntity = new PluginConfigs();
        pluginConfigEntity.setId(LocalIdGenerator.generateId());
        pluginConfigEntity.setName(xmlPlugin.getName());
        pluginConfigEntity.setPluginPackageId(pluginPackageEntity.getId());
        pluginConfigEntity.setRegisterName(xmlPlugin.getRegisterName());
        pluginConfigEntity.setStatus(PluginConfigs.DISABLED);
        pluginConfigEntity.setTargetEntity(xmlPlugin.getTargetEntity());
        // TODO
        pluginConfigEntity.setTargetEntityFilterRule(xmlPlugin.getTargetEntityFilterRule());
        pluginConfigEntity.setTargetPackage(xmlPlugin.getTargetPackage());

        pluginConfigsMapper.insert(pluginConfigEntity);

        List<InterfaceType> xmlInterfaces = xmlPlugin.getInterface();
        // TODO

        RoleBindsType xmlRoleBinds = xmlPlugin.getRoleBinds();
        // TODO
        processRoleBinds(xmlRoleBinds, pluginConfigEntity);

    }

    private void processRoleBinds(RoleBindsType xmlRoleBinds, PluginConfigs pluginConfigEntity) {
        if (xmlRoleBinds == null) {
            return;
        }

        List<RoleBindType> xmlRoleBindList = xmlRoleBinds.getRoleBind();

        if (xmlRoleBindList == null || xmlRoleBindList.isEmpty()) {
            return;
        }

        for (RoleBindType xmlRoleBind : xmlRoleBindList) {
            PluginConfigRoles pluginConfigRoleEntity = new PluginConfigRoles();
            pluginConfigRoleEntity.setId(LocalIdGenerator.generateId());
            pluginConfigRoleEntity.setIsActive(true);

            pluginConfigRoleEntity.setCreatedBy(AuthenticationContextHolder.getCurrentUsername());
            pluginConfigRoleEntity.setCreatedTime(new Date());
            pluginConfigRoleEntity.setPermType(xmlRoleBind.getPermission());
            pluginConfigRoleEntity.setPluginCfgId(pluginConfigEntity.getId());
            RoleDto roleDto = fetchRoleWithRoleName(xmlRoleBind.getRoleName());
            if (roleDto != null) {
                pluginConfigRoleEntity.setRoleId(roleDto.getId());
            }
            pluginConfigRoleEntity.setRoleName(xmlRoleBind.getRoleName());

            pluginConfigRolesMapper.insert(pluginConfigRoleEntity);
        }

    }

    private RoleDto fetchRoleWithRoleName(String roleName) {
        RoleDto role = null;
        try {
            role = userManagementService.retrieveRoleByRoleName(roleName);
        } catch (Exception e) {
            log.warn("errors while fetch role with role name:{}", roleName);
            role = null;
        }

        return role;
    }

    private void processPluginUpgradeSqlFile(File localFilePath, PackageType xmlPackage) {
        File pluginUpgradeSqlFile = new File(localFilePath + File.separator + pluginProperties.getUpgradeDbSql());
        if (pluginUpgradeSqlFile.exists()) {
            String keyName = xmlPackage.getName() + "/" + xmlPackage.getVersion() + "/"
                    + pluginProperties.getUpgradeDbSql();
            log.info("Uploading upgrade sql {} to MinIO {}", pluginUpgradeSqlFile.getAbsolutePath(), keyName);
            String upgradeSqlUrl = s3Client.uploadFile(pluginProperties.getPluginPackageBucketName(), keyName,
                    pluginUpgradeSqlFile);
            log.info("Upgrade sql {} has been uploaded to MinIO {}", pluginProperties.getUpgradeDbSql(), upgradeSqlUrl);
        } else {
            log.info("Upgrade sql {} is not included in package.", pluginProperties.getUpgradeDbSql());
        }
    }

    private void processPluginInitSqlFile(File localFilePath, PackageType xmlPackage) {
        File pluginInitSqlFile = new File(localFilePath + File.separator + pluginProperties.getInitDbSql());
        if (pluginInitSqlFile.exists()) {
            String keyName = xmlPackage.getName() + "/" + xmlPackage.getVersion() + "/"
                    + pluginProperties.getInitDbSql();
            log.info("Uploading init sql {} to MinIO {}", pluginInitSqlFile.getAbsolutePath(), keyName);
            String initSqlUrl = s3Client.uploadFile(pluginProperties.getPluginPackageBucketName(), keyName,
                    pluginInitSqlFile);
            log.info("Init sql {} has been uploaded to MinIO {}", pluginProperties.getInitDbSql(), initSqlUrl);
        } else {
            log.info("Init sql {} is not included in package.", pluginProperties.getInitDbSql());
        }
    }

    private List<PluginPackageResourceFiles> processPluginUiPackageFile(File localFilePath, PackageType xmlPackage,
            PluginPackages pluginPackageEntity) throws IOException  {
        File pluginUiPackageFile = new File(localFilePath + "/" + pluginProperties.getUiFile());
        log.info("pluginUiPackageFile: {}", pluginUiPackageFile.getAbsolutePath());
        String uiPackageUrl = "";
        List<PluginPackageResourceFiles> pluginPackageResourceFilesEntities = new ArrayList<>();
        if (pluginUiPackageFile.exists()) {

            String keyName = xmlPackage.getName() + "/" + xmlPackage.getVersion() + "/" + pluginUiPackageFile.getName();
            log.info("keyName : {}", keyName);

            pluginPackageResourceFilesEntities = parseAllPluginPackageResourceFile(pluginPackageEntity,
                    pluginUiPackageFile.getAbsolutePath(), pluginUiPackageFile.getName());
            uiPackageUrl = s3Client.uploadFile(pluginProperties.getPluginPackageBucketName(), keyName,
                    pluginUiPackageFile);
            pluginPackageEntity.setUiPackageIncluded(true);
            log.info("UI static package file has uploaded to MinIO {}", uiPackageUrl.split("\\?")[0]);
        }

        return pluginPackageResourceFilesEntities;
    }

    private List<PluginPackageResourceFiles> parseAllPluginPackageResourceFile(PluginPackages pluginPackageEntity,
            String sourceZipFile, String sourceZipFileName) throws IOException {
        List<PluginPackageResourceFiles> pluginPackageResourceFilesEntities = new ArrayList<>();
        try (ZipFile zipFile = new ZipFile(sourceZipFile)) {
            Enumeration<?> entries = zipFile.entries();
            while(entries.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) entries.nextElement();
                if (!entry.isDirectory()) {
                    String zipEntryName = entry.getName();
                    PluginPackageResourceFiles pluginPackageResourceFilesEntity = new PluginPackageResourceFiles();
                    pluginPackageResourceFilesEntity.setId(LocalIdGenerator.generateId());
                    pluginPackageResourceFilesEntity.setPackageName(pluginPackageEntity.getName());
                    pluginPackageResourceFilesEntity.setPackageVersion(pluginPackageEntity.getVersion());
                    pluginPackageResourceFilesEntity.setPluginPackageId(pluginPackageEntity.getId());
                    String relatedPath = "/ui-resources/" + pluginPackageEntity.getName() + File.separator
                            + pluginPackageEntity.getVersion() + File.separator + zipEntryName;
                    pluginPackageResourceFilesEntity.setRelatedPath(relatedPath);
                    pluginPackageResourceFilesEntity.setSource(sourceZipFileName);

                    log.info("File in ui package [{}] : {}", sourceZipFileName, zipEntryName);

                    pluginPackageResourceFilesEntities.add(pluginPackageResourceFilesEntity);
                }
            }
        }

        return pluginPackageResourceFilesEntities;
    }

    private void processPluginDockerImageFile(File localFilePath, PackageType xmlPackage) {
        File pluginDockerImageFile = new File(localFilePath + "/" + pluginProperties.getImageFile());
        log.info("pluginDockerImageFile: {}", pluginDockerImageFile.getAbsolutePath());

        if (pluginDockerImageFile.exists()) {
            String keyName = xmlPackage.getName() + "/" + xmlPackage.getVersion() + "/"
                    + pluginDockerImageFile.getName();
            log.info("keyName : {}", keyName);

            String dockerImageUrl = s3Client.uploadFile(pluginProperties.getPluginPackageBucketName(), keyName,
                    pluginDockerImageFile);
            log.info("Plugin Package has uploaded to MinIO {}", dockerImageUrl.split("\\?")[0]);
        }
    }

    @SuppressWarnings("rawtypes")
    private void unzipLocalFile(String sourceZipFile, String destFilePath) throws IOException {
        try (ZipFile zipFile = new ZipFile(sourceZipFile)) {
            Enumeration entries = zipFile.entries();

            while (entries.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) entries.nextElement();
                String zipEntryName = entry.getName();
                if (entry.isDirectory() || !ACCEPTED_FILES.contains(zipEntryName)) {
                    continue;
                }

                if (new File(destFilePath + zipEntryName).createNewFile()) {
                    log.info("Create new temporary file: {}", destFilePath + zipEntryName);
                }

                try (BufferedInputStream inputStream = new BufferedInputStream(zipFile.getInputStream(entry));
                        OutputStream outputStream = new FileOutputStream(destFilePath + zipEntryName, true)) {
                    byte[] buf = new byte[2048];
                    int len;
                    while ((len = inputStream.read(buf)) > 0) {
                        outputStream.write(buf, 0, len);
                    }
                } catch (Exception e) {
                    log.error("Read input stream meet error: ", e);
                }
            }
        }

        log.info("Zip file has uploaded !");
    }

    private boolean isPluginPackageExists(String name, String version) {
        return (pluginPackagesMapper.countByNameAndVersion(name, version) > 0);
    }

    protected void closeSilently(Closeable c) {
        if (c == null) {
            return;
        }

        try {
            c.close();
        } catch (Exception e) {
        }
    }
}
