package com.webank.wecube.platform.core.service.plugin;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
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

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;

import com.google.common.collect.Sets;
import com.webank.wecube.platform.core.commons.AuthenticationContextHolder;
import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.dto.plugin.S3PluginActifactDto;
import com.webank.wecube.platform.core.dto.plugin.S3PluginActifactPullRequestDto;
import com.webank.wecube.platform.core.dto.plugin.UploadPackageResultDto;
import com.webank.wecube.platform.core.dto.user.RoleDto;
import com.webank.wecube.platform.core.entity.plugin.PluginArtifactPullReq;
import com.webank.wecube.platform.core.entity.plugin.PluginConfigInterfaceParameters;
import com.webank.wecube.platform.core.entity.plugin.PluginConfigInterfaces;
import com.webank.wecube.platform.core.entity.plugin.PluginConfigRoles;
import com.webank.wecube.platform.core.entity.plugin.PluginConfigs;
import com.webank.wecube.platform.core.entity.plugin.PluginPackageAttributes;
import com.webank.wecube.platform.core.entity.plugin.PluginPackageAuthorities;
import com.webank.wecube.platform.core.entity.plugin.PluginPackageDataModel;
import com.webank.wecube.platform.core.entity.plugin.PluginPackageDependencies;
import com.webank.wecube.platform.core.entity.plugin.PluginPackageEntities;
import com.webank.wecube.platform.core.entity.plugin.PluginPackageMenus;
import com.webank.wecube.platform.core.entity.plugin.PluginPackageResourceFiles;
import com.webank.wecube.platform.core.entity.plugin.PluginPackageRuntimeResourcesDocker;
import com.webank.wecube.platform.core.entity.plugin.PluginPackageRuntimeResourcesMysql;
import com.webank.wecube.platform.core.entity.plugin.PluginPackageRuntimeResourcesS3;
import com.webank.wecube.platform.core.entity.plugin.PluginPackages;
import com.webank.wecube.platform.core.entity.plugin.SystemVariables;
import com.webank.wecube.platform.core.parser.PluginConfigXmlValidator;
import com.webank.wecube.platform.core.parser.PluginPackageDataModelValidator;
import com.webank.wecube.platform.core.parser.PluginPackageValidator;
import com.webank.wecube.platform.core.repository.plugin.PluginArtifactPullReqMapper;
import com.webank.wecube.platform.core.repository.plugin.PluginConfigInterfaceParametersMapper;
import com.webank.wecube.platform.core.repository.plugin.PluginConfigInterfacesMapper;
import com.webank.wecube.platform.core.repository.plugin.PluginConfigRolesMapper;
import com.webank.wecube.platform.core.repository.plugin.PluginConfigsMapper;
import com.webank.wecube.platform.core.repository.plugin.PluginPackageAttributesMapper;
import com.webank.wecube.platform.core.repository.plugin.PluginPackageAuthoritiesMapper;
import com.webank.wecube.platform.core.repository.plugin.PluginPackageDataModelMapper;
import com.webank.wecube.platform.core.repository.plugin.PluginPackageDependenciesMapper;
import com.webank.wecube.platform.core.repository.plugin.PluginPackageEntitiesMapper;
import com.webank.wecube.platform.core.repository.plugin.PluginPackageMenusMapper;
import com.webank.wecube.platform.core.repository.plugin.PluginPackageResourceFilesMapper;
import com.webank.wecube.platform.core.repository.plugin.PluginPackageRuntimeResourcesDockerMapper;
import com.webank.wecube.platform.core.repository.plugin.PluginPackageRuntimeResourcesMysqlMapper;
import com.webank.wecube.platform.core.repository.plugin.PluginPackageRuntimeResourcesS3Mapper;
import com.webank.wecube.platform.core.repository.plugin.PluginPackagesMapper;
import com.webank.wecube.platform.core.repository.plugin.SystemVariablesMapper;
import com.webank.wecube.platform.core.service.plugin.PluginArtifactOperationExecutor.PluginArtifactPullContext;
import com.webank.wecube.platform.core.service.plugin.xml.register.AttributeType;
import com.webank.wecube.platform.core.service.plugin.xml.register.AuthoritiesType;
import com.webank.wecube.platform.core.service.plugin.xml.register.AuthorityType;
import com.webank.wecube.platform.core.service.plugin.xml.register.DataModelType;
import com.webank.wecube.platform.core.service.plugin.xml.register.DockerType;
import com.webank.wecube.platform.core.service.plugin.xml.register.EntityType;
import com.webank.wecube.platform.core.service.plugin.xml.register.InputParameterType;
import com.webank.wecube.platform.core.service.plugin.xml.register.InputParametersType;
import com.webank.wecube.platform.core.service.plugin.xml.register.InterfaceType;
import com.webank.wecube.platform.core.service.plugin.xml.register.MenuType;
import com.webank.wecube.platform.core.service.plugin.xml.register.MenusType;
import com.webank.wecube.platform.core.service.plugin.xml.register.MysqlType;
import com.webank.wecube.platform.core.service.plugin.xml.register.OutputParameterType;
import com.webank.wecube.platform.core.service.plugin.xml.register.OutputParametersType;
import com.webank.wecube.platform.core.service.plugin.xml.register.PackageDependenciesType;
import com.webank.wecube.platform.core.service.plugin.xml.register.PackageDependencyType;
import com.webank.wecube.platform.core.service.plugin.xml.register.PackageType;
import com.webank.wecube.platform.core.service.plugin.xml.register.ParamObjectsType;
import com.webank.wecube.platform.core.service.plugin.xml.register.PluginType;
import com.webank.wecube.platform.core.service.plugin.xml.register.PluginsType;
import com.webank.wecube.platform.core.service.plugin.xml.register.ResourceDependenciesType;
import com.webank.wecube.platform.core.service.plugin.xml.register.RoleBindType;
import com.webank.wecube.platform.core.service.plugin.xml.register.RoleBindsType;
import com.webank.wecube.platform.core.service.plugin.xml.register.S3Type;
import com.webank.wecube.platform.core.service.plugin.xml.register.SystemParameterType;
import com.webank.wecube.platform.core.service.plugin.xml.register.SystemParametersType;
import com.webank.wecube.platform.core.service.user.UserManagementService;
import com.webank.wecube.platform.core.utils.JaxbUtils;
import com.webank.wecube.platform.core.utils.SystemUtils;
import com.webank.wecube.platform.workflow.commons.LocalIdGenerator;

@Service
public class PluginArtifactsMgmtService extends AbstractPluginMgmtService {
    private static final Logger log = LoggerFactory.getLogger(PluginArtifactsMgmtService.class);

    public static final Set<String> ACCEPTED_FILES = Sets.newHashSet("register.xml", "image.tar", "ui.zip", "init.sql",
            "upgrade.sql");

    public static final String DEFAULT_DATA_MODEL_UPDATE_PATH = "/data-model";
    public static final String DEFAULT_DATA_MODEL_UPDATE_METHOD = "GET";
    public static final String SYS_VAR_PUBLIC_PLUGIN_ARTIFACTS_RELEASE_URL = "PLUGIN_ARTIFACTS_RELEASE_URL";

    public static final String DEFAULT_REQUIRED = "N";
    public static final String DEFAULT_SENSITIVE_DATA = "N";
    public static final String DEFAULT_TARGET_ENTITY_FILTER_RULE = "";
    public static final String DEFAULT_FILTER_RULE_FOR_INTERFACE = "";

    private static final String DEFAULT_USER = "sys";

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private PluginArtifactOperationExecutor pluginArtifactOperationExecutor;

    @Autowired
    private PluginArtifactPullReqMapper pluginArtifactPullReqMapper;

    @Autowired
    private PluginPackageValidator pluginPackageValidator;

    @Autowired
    private PluginPackageDataModelValidator dataModelValidator;

    @Autowired
    private PluginPackagesMapper pluginPackagesMapper;

    @Autowired
    private PluginConfigsMapper pluginConfigsMapper;

    @Autowired
    private PluginConfigInterfacesMapper pluginConfigInterfacesMapper;

    @Autowired
    private PluginConfigInterfaceParametersMapper pluginConfigInterfaceParameters;

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
    private PluginPackageDependenciesMapper pluginPackageDependenciesMapper;

    @Autowired
    private PluginPackageAuthoritiesMapper pluginPackageAuthoritiesMapper;
    @Autowired
    private PluginPackageMenusMapper pluginPackageMenusMapper;
    @Autowired
    private PluginPackageRuntimeResourcesDockerMapper pluginPackageRuntimeResourcesDockerMapper;
    @Autowired
    private PluginPackageRuntimeResourcesMysqlMapper pluginPackageRuntimeResourcesMysqlMapper;
    @Autowired
    private PluginPackageRuntimeResourcesS3Mapper pluginPackageRuntimeResourcesS3Mapper;

    @Autowired
    private UserManagementService userManagementService;

    @Autowired
    private PluginParamObjectMetaRegister pluginParamObjectSupportService;

    /**
     * 
     * @param ctx
     * @throws Exception
     */
    public void pullPluginArtifact(PluginArtifactPullContext ctx) throws Exception {

        PluginArtifactPullReq reqEntity = getPluginArtifactPullRequestEntity(ctx);

        if (PluginArtifactPullReq.STATE_COMPLETED.equals(reqEntity.getState())) {
            return;
        }

        String pluginPackageFileName = calculatePluginPackageFileName(ctx);

        // 1. save package file to local
        String tmpFileName = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
        File localFilePath = new File(SystemUtils.getTempFolderPath() + tmpFileName + "/");
        log.info("tmpFilePath= {}", localFilePath.getName());

        checkLocalFilePath(localFilePath);

        File dest = new File(localFilePath + "/" + pluginPackageFileName);
        log.info("new file location: {}, filename: {}, canonicalpath: {}, canonicalfilename: {}",
                dest.getAbsoluteFile(), dest.getName(), dest.getCanonicalPath(), dest.getCanonicalFile().getName());

        String releaseFileUrl = getGlobalSystemVariableByName(SYS_VAR_PUBLIC_PLUGIN_ARTIFACTS_RELEASE_URL);
        String artifactFileUrl = buildArtifactUrl(releaseFileUrl, ctx.getKeyName());

        log.info("start to download {}", artifactFileUrl);
        File downloadedFile = restTemplate.execute(artifactFileUrl, HttpMethod.GET, null, clientHttpResponse -> {
            log.info("");
            File ret = dest;
            StreamUtils.copy(clientHttpResponse.getBody(), new FileOutputStream(ret));
            return ret;
        });
        log.info("downloaded file:{}, size:{}", downloadedFile.getAbsoluteFile(), downloadedFile.length());

        UploadPackageResultDto uploadResult = parsePackageFile(dest, localFilePath);

        reqEntity.setUpdatedBy(DEFAULT_USER);
        reqEntity.setUpdatedTime(new Date());
        reqEntity.setTotalSize(downloadedFile.length());
        reqEntity.setPkgId(uploadResult.getId());
        reqEntity.setState(PluginArtifactPullReq.STATE_COMPLETED);

        pluginArtifactPullReqMapper.updateByPrimaryKeySelective(reqEntity);
    }

    /**
     * 
     * @return
     */
    public List<S3PluginActifactDto> listS3PluginActifacts() {
        String releaseFileUrl = getGlobalSystemVariableByName(SYS_VAR_PUBLIC_PLUGIN_ARTIFACTS_RELEASE_URL);

        if (StringUtils.isBlank(releaseFileUrl)) {
            throw new WecubeCoreException("3093", "The remote plugin artifacts release file is not properly provided.");
        }

        try {
            List<S3PluginActifactDto> results = parseReleaseFile(releaseFileUrl);
            return results;
        } catch (Exception e) {
            log.error("Failed to parse release file.", e);
            throw new WecubeCoreException("3094",
                    String.format("Cannot parse release file properly.Caused by " + e.getMessage()));
        }
    }

    /**
     * 
     * @param pullRequestDto
     * @return
     */
    public S3PluginActifactPullRequestDto createS3PluginActifactPullRequest(S3PluginActifactDto pullRequestDto) {
        if (pullRequestDto == null) {
            throw new WecubeCoreException("3095", "Illegal argument.");
        }

        if (StringUtils.isBlank(pullRequestDto.getKeyName())) {
            throw new WecubeCoreException("3096", "Key name cannot be blank.");
        }

        // get system variables
        String releaseFileUrl = getGlobalSystemVariableByName(SYS_VAR_PUBLIC_PLUGIN_ARTIFACTS_RELEASE_URL);

        if (org.apache.commons.lang3.StringUtils.isBlank(releaseFileUrl)) {
            throw new WecubeCoreException("3097", "The remote plugin artifacts release file is not properly provided.");
        }

        PluginArtifactPullReq entity = new PluginArtifactPullReq();
        entity.setId(LocalIdGenerator.generateId());
        entity.setBucketName(null);
        entity.setKeyName(pullRequestDto.getKeyName());
        entity.setRev(0);
        entity.setState(PluginArtifactPullReq.STATE_IN_PROGRESS);
        entity.setCreatedTime(new Date());
        entity.setCreatedBy(AuthenticationContextHolder.getCurrentUsername());

        pluginArtifactPullReqMapper.insert(entity);

        PluginArtifactPullContext ctx = new PluginArtifactPullContext();
        ctx.setAccessKey(null);
        ctx.setBucketName(null);
        ctx.setKeyName(pullRequestDto.getKeyName());
        ctx.setRemoteEndpoint(releaseFileUrl);
        ctx.setSecretKey(null);
        ctx.setRequestId(entity.getId());
        ctx.setEntity(entity);

        pluginArtifactOperationExecutor.pullPluginArtifact(ctx);

        return buildS3PluginActifactPullRequestDto(entity);
    }

    /**
     * 
     * @param requestId
     * @return
     */
    public S3PluginActifactPullRequestDto queryS3PluginActifactPullRequest(String requestId) {
        if (StringUtils.isBlank(requestId)) {
            throw new WecubeCoreException("3295", "Request ID cannot be null.");
        }

        PluginArtifactPullReq reqEntity = pluginArtifactPullReqMapper.selectByPrimaryKey(requestId);
        if (reqEntity == null) {
            throw new WecubeCoreException("3098", String.format("Such request with %s does not exist.", requestId),
                    requestId);
        }

        return buildS3PluginActifactPullRequestDto(reqEntity);
    }

    /**
     * 
     * @param ctx
     * @param e
     */
    public void handlePullPluginArtifactFailure(PluginArtifactPullContext ctx, Exception e) {
        PluginArtifactPullReq reqEntity = pluginArtifactPullReqMapper.selectByPrimaryKey(ctx.getRequestId());
        if (reqEntity == null) {
            log.warn("request entity {} does not exist", ctx.getRequestId());
            return;
        }

        if (PluginArtifactPullReq.STATE_COMPLETED.equals(reqEntity.getState())) {
            return;
        }

        reqEntity.setErrMsg(stripString(e.getMessage()));
        reqEntity.setUpdatedBy(DEFAULT_USER);
        reqEntity.setUpdatedTime(new Date());
        reqEntity.setState(PluginArtifactPullReq.STATE_FAULTED);

        pluginArtifactPullReqMapper.updateByPrimaryKeySelective(reqEntity);
    }

    /**
     * 
     * @param pluginPackageFile
     * @return
     */
    @Transactional
    public UploadPackageResultDto uploadPackage(MultipartFile pluginPackageFile) {

        // 1. save package file to local
        String tmpFileName = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
        File localFilePath = new File(SystemUtils.getTempFolderPath() + tmpFileName + "/");
        log.info("tmp File Path= {}", localFilePath.getName());

        try {
            UploadPackageResultDto result = performUploadPackage(pluginPackageFile, localFilePath);

            return result;
        } finally {
            if (localFilePath != null && localFilePath.exists()) {
                log.info("try to clean up temporary files:{}", localFilePath.getAbsolutePath());
                FileUtils.deleteQuietly(localFilePath);
            }

        }
    }

    /**
     * 
     * @param dest
     * @param localFilePath
     * @return
     * @throws IOException
     * @throws SAXException
     */
    @Transactional
    public UploadPackageResultDto parsePackageFile(File dest, File localFilePath) throws IOException, SAXException {
        // 2. unzip local package file
        unzipLocalFile(dest.getCanonicalPath(), localFilePath.getCanonicalPath() + "/");

        // 3. read xml file in plugin package
        File registerXmlFile = new File(localFilePath.getCanonicalPath() + "/" + pluginProperties.getRegisterFile());
        if (!registerXmlFile.exists()) {
            String errMsg = String.format("Plugin package definition file: [%s] does not exist.",
                    pluginProperties.getRegisterFile());
            throw new WecubeCoreException("3114", errMsg, pluginProperties.getRegisterFile());
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

        if (log.isInfoEnabled()) {
            log.info("parsing register xml:{}", registerXmlDataAsStr);
        }

        PackageType xmlPackage = JaxbUtils.convertToObject(registerXmlDataAsStr, PackageType.class);

        pluginPackageValidator.validatePackage(xmlPackage);
        dataModelValidator.validateDataModel(xmlPackage.getDataModel());

        if (isPluginPackageExists(xmlPackage.getName(), xmlPackage.getVersion())) {
            String errMsg = String.format("Plugin package [name=%s, version=%s] exists.", xmlPackage.getName(),
                    xmlPackage.getVersion());
            throw new WecubeCoreException("3115", errMsg, xmlPackage.getName(), xmlPackage.getVersion());
        }

        processPluginDockerImageFile(localFilePath, xmlPackage);

        PluginPackages pluginPackageEntity = new PluginPackages();
        pluginPackageEntity.setId(LocalIdGenerator.generateId());
        pluginPackageEntity.setName(xmlPackage.getName());
        pluginPackageEntity.setVersion(xmlPackage.getVersion());
        pluginPackageEntity.setStatus(PluginPackages.UNREGISTERED);
        pluginPackageEntity.setUploadTimestamp(new Date());
        pluginPackagesMapper.insert(pluginPackageEntity);

        processPluginUiPackageFile(localFilePath, xmlPackage, pluginPackageEntity);

        // trySavePluginPackageResourceFiles(pluginPackageResourceFilesEntities);

        processPluginInitSqlFile(localFilePath, xmlPackage);
        processPluginUpgradeSqlFile(localFilePath, xmlPackage);

        pluginPackagesMapper.updateByPrimaryKeySelective(pluginPackageEntity);

        processPluginConfigs(xmlPackage.getPlugins(), xmlPackage, pluginPackageEntity);

        processSystemVaraibles(xmlPackage.getSystemParameters(), xmlPackage, pluginPackageEntity);
        processPackageDependencies(xmlPackage.getPackageDependencies(), xmlPackage, pluginPackageEntity);

        processMenus(xmlPackage.getMenus(), xmlPackage, pluginPackageEntity);
        processAuthorities(xmlPackage.getAuthorities(), xmlPackage, pluginPackageEntity);
        processResourceDependencies(xmlPackage.getResourceDependencies(), xmlPackage, pluginPackageEntity);

        processDataModels(xmlPackage.getDataModel(), xmlPackage, pluginPackageEntity);

//        processParamObjects(xmlPackage.getParamObjects(), xmlPackage.getName(), xmlPackage.getVersion());

        UploadPackageResultDto result = new UploadPackageResultDto();
        result.setId(pluginPackageEntity.getId());
        result.setName(pluginPackageEntity.getName());
        result.setStatus(pluginPackageEntity.getStatus());
        result.setVersion(pluginPackageEntity.getVersion());
        result.setUiPackageIncluded(pluginPackageEntity.getUiPackageIncluded());

        return result;
    }

    protected UploadPackageResultDto performUploadPackage(MultipartFile pluginPackageFile, File localFilePath) {
        String pluginPackageFileName = pluginPackageFile.getName();
        if (!localFilePath.exists()) {
            if (localFilePath.mkdirs()) {
                log.info("Create directory [{}] successful", localFilePath.getAbsolutePath());
            } else {
                String errMsg = String.format("Create directory [%s] failed.", localFilePath.getAbsolutePath());
                throw new WecubeCoreException("3099", errMsg, localFilePath.getAbsolutePath());
            }
        }

        File dest = new File(localFilePath, "/" + pluginPackageFileName);
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
        } catch (Exception e) {
            log.error("Errors to parse uploaded package file.", e);
            throw new WecubeCoreException("Failed to upload package due to " + e.getMessage());
        }

        return result;
    }

    private void processParamObjects(ParamObjectsType xmlParamObjects, String packageName, String packageVersion, String configId) {
        pluginParamObjectSupportService.registerParamObjects(xmlParamObjects, packageName, packageVersion, configId);
    }

    private void processPackageDependencies(PackageDependenciesType xmlPackageDependenciesType, PackageType xmlPackage,
            PluginPackages pluginPackageEntity) {
        if (xmlPackageDependenciesType == null) {
            log.info("There is not package dependency defined.");
            return;
        }

        List<PackageDependencyType> xmlPackageDependencyList = xmlPackageDependenciesType.getPackageDependency();

        if (xmlPackageDependencyList == null || xmlPackageDependencyList.isEmpty()) {
            return;
        }

        for (PackageDependencyType xmlDependency : xmlPackageDependencyList) {
            PluginPackageDependencies dependencyEntity = new PluginPackageDependencies();
            dependencyEntity.setId(LocalIdGenerator.generateId());
            dependencyEntity.setPluginPackageId(pluginPackageEntity.getId());
            dependencyEntity.setDependencyPackageName(xmlDependency.getName());
            dependencyEntity.setDependencyPackageVersion(xmlDependency.getVersion());

            dependencyEntity.setPluginPackage(pluginPackageEntity);
            pluginPackageDependenciesMapper.insert(dependencyEntity);

            pluginPackageEntity.getPluginPackageDependencies().add(dependencyEntity);
        }
    }

    private void processMenus(MenusType xmlMenusType, PackageType xmlPackage, PluginPackages pluginPackageEntity) {
        if (xmlMenusType == null) {
            return;
        }

        List<MenuType> xmlMenuList = xmlMenusType.getMenu();
        if (xmlMenuList == null || xmlMenuList.isEmpty()) {
            return;
        }

        for (MenuType xmlMenu : xmlMenuList) {
            PluginPackageMenus packageMenuEntity = new PluginPackageMenus();
            packageMenuEntity.setId(LocalIdGenerator.generateId());
            packageMenuEntity.setActive(false);
            packageMenuEntity.setCategory(xmlMenu.getCat());
            packageMenuEntity.setCode(xmlMenu.getCode());
            packageMenuEntity.setDisplayName(xmlMenu.getDisplayName());
            packageMenuEntity
                    .setLocalDisplayName(StringUtils.isBlank(xmlMenu.getLocalDisplayName()) ? xmlMenu.getDisplayName()
                            : xmlMenu.getLocalDisplayName());
            // packageMenuEntity.setMenuOrder(menuOrder);

            packageMenuEntity.setPath(xmlMenu.getValue());
            packageMenuEntity.setPluginPackageId(pluginPackageEntity.getId());
            packageMenuEntity.setSource(pluginPackageEntity.getId());

            pluginPackageMenusMapper.insert(packageMenuEntity);
            pluginPackageEntity.getPluginPackageMenus().add(packageMenuEntity);
        }

    }

    private void processAuthorities(AuthoritiesType xmlAuthoritiesType, PackageType xmlPackage,
            PluginPackages pluginPackageEntity) {
        if (xmlAuthoritiesType == null) {
            return;
        }

        List<AuthorityType> xmlAuthorityList = xmlAuthoritiesType.getAuthority();
        if (xmlAuthorityList == null || xmlAuthorityList.isEmpty()) {
            return;
        }

        for (AuthorityType xmlAuthortity : xmlAuthorityList) {
            String xmlSystemRoleName = xmlAuthortity.getSystemRoleName();
            List<MenuType> xmlMenuList = xmlAuthortity.getMenu();
            if (xmlMenuList == null || xmlMenuList.isEmpty()) {
                continue;
            }

            for (MenuType xmlMenu : xmlMenuList) {
                PluginPackageAuthorities authEntity = new PluginPackageAuthorities();
                authEntity.setId(LocalIdGenerator.generateId());
                authEntity.setMenuCode(xmlMenu.getCode());
                authEntity.setPluginPackageId(pluginPackageEntity.getId());
                authEntity.setPluginPackge(pluginPackageEntity);
                authEntity.setRoleName(xmlSystemRoleName);

                pluginPackageAuthoritiesMapper.insert(authEntity);
            }

        }
    }

    private void processResourceDependencies(ResourceDependenciesType xmlResourceDependenciesType,
            PackageType xmlPackage, PluginPackages pluginPackageEntity) {
        if (xmlResourceDependenciesType == null) {
            return;
        }

        List<DockerType> xmlDockerList = xmlResourceDependenciesType.getDocker();
        if (xmlDockerList != null) {
            for (DockerType xmlDocker : xmlDockerList) {
                PluginPackageRuntimeResourcesDocker dockerEntity = new PluginPackageRuntimeResourcesDocker();
                dockerEntity.setId(LocalIdGenerator.generateId());
                dockerEntity.setContainerName(xmlDocker.getContainerName());
                dockerEntity.setEnvVariables(xmlDocker.getEnvVariables());
                dockerEntity.setImageName(xmlDocker.getImageName());
                dockerEntity.setPluginPackageId(pluginPackageEntity.getId());
                dockerEntity.setPortBindings(xmlDocker.getPortBindings());
                dockerEntity.setVolumeBindings(xmlDocker.getVolumeBindings());

                pluginPackageRuntimeResourcesDockerMapper.insert(dockerEntity);

                pluginPackageEntity.getDockers().add(dockerEntity);
            }
        }

        List<MysqlType> xmlMysqlList = xmlResourceDependenciesType.getMysql();
        if (xmlMysqlList != null) {
            for (MysqlType xmlMysql : xmlMysqlList) {
                PluginPackageRuntimeResourcesMysql mysqlEntity = new PluginPackageRuntimeResourcesMysql();
                mysqlEntity.setId(LocalIdGenerator.generateId());
                mysqlEntity.setInitFileName(xmlMysql.getInitFileName());
                mysqlEntity.setPluginPackageId(pluginPackageEntity.getId());
                mysqlEntity.setSchemaName(xmlMysql.getSchema());
                mysqlEntity.setUpgradeFileName(xmlMysql.getUpgradeFileName());

                pluginPackageRuntimeResourcesMysqlMapper.insert(mysqlEntity);

                pluginPackageEntity.getMysqls().add(mysqlEntity);
            }
        }

        List<S3Type> xmlS3List = xmlResourceDependenciesType.getS3();
        if (xmlS3List != null) {
            for (S3Type xmlS3 : xmlS3List) {
                PluginPackageRuntimeResourcesS3 s3Entity = new PluginPackageRuntimeResourcesS3();
                s3Entity.setId(LocalIdGenerator.generateId());
                s3Entity.setPluginPackageId(pluginPackageEntity.getId());
                s3Entity.setBucketName(xmlS3.getBucketName());

                pluginPackageRuntimeResourcesS3Mapper.insert(s3Entity);

                pluginPackageEntity.getS3s().add(s3Entity);
            }
        }
    }

    private void processDataModels(DataModelType xmlDataModel, PackageType xmlPackage,
            PluginPackages pluginPackageEntity) {
        log.info("start to process data model...");

        if (xmlDataModel == null) {
            log.info("data model is null...");
            return;
        }

        int lastDataModelVersion = -1;
        PluginPackageDataModel existModelEntity = pluginPackageDataModelMapper
                .selectLatestDataModelByPackageName(pluginPackageEntity.getName());

        if (existModelEntity != null) {
            lastDataModelVersion = existModelEntity.getVersion();
        }

        PluginPackageDataModel dataModelEntity = new PluginPackageDataModel();
        dataModelEntity.setId(LocalIdGenerator.generateId());
        boolean isDynamic = "true".equalsIgnoreCase(xmlDataModel.getIsDynamic());
        dataModelEntity.setIsDynamic(isDynamic);
        dataModelEntity.setPackageName(xmlPackage.getName());
        String updatePath = xmlDataModel.getPath();
        if (StringUtils.isEmpty(updatePath) && isDynamic) {
            updatePath = DEFAULT_DATA_MODEL_UPDATE_PATH;
        }

        dataModelEntity.setUpdatePath(updatePath);
        String updateMethod = xmlDataModel.getMethod();
        if (StringUtils.isEmpty(updateMethod) && isDynamic) {
            updateMethod = DEFAULT_DATA_MODEL_UPDATE_METHOD;
        }
        dataModelEntity.setUpdateMethod(updateMethod);
        dataModelEntity.setUpdateSource(PluginPackageDataModel.PLUGIN_PACKAGE);
        dataModelEntity.setUpdateTime(System.currentTimeMillis());
        dataModelEntity.setVersion(lastDataModelVersion + 1);
        pluginPackageDataModelMapper.insert(dataModelEntity);

        List<EntityType> xmlEntityList = xmlDataModel.getEntity();
        if (xmlEntityList == null || xmlEntityList.isEmpty()) {
            return;
        }

        List<PluginPackageAttributes> savedAttributes = new ArrayList<>();

        for (EntityType xmlEntity : xmlEntityList) {
            PluginPackageEntities entity = new PluginPackageEntities();
            entity.setDataModelId(dataModelEntity.getId());
            entity.setDataModelVersion(dataModelEntity.getVersion());
            entity.setDescription(xmlEntity.getDescription());
            entity.setDisplayName(xmlEntity.getDisplayName());
            entity.setId(LocalIdGenerator.generateId());
            entity.setName(xmlEntity.getName());
            entity.setPackageName(xmlPackage.getName());

            pluginPackageEntitiesMapper.insert(entity);

            List<AttributeType> xmlAttributeList = xmlEntity.getAttribute();
            if (xmlAttributeList == null || xmlAttributeList.isEmpty()) {
                continue;
            }

            for (AttributeType xmlAttribute : xmlAttributeList) {
                PluginPackageAttributes attributeEntity = new PluginPackageAttributes();
                attributeEntity.setId(LocalIdGenerator.generateId());
                attributeEntity.setDataType(xmlAttribute.getDatatype());
                attributeEntity.setDescription(xmlAttribute.getDescription());
                attributeEntity.setEntityId(entity.getId());
                attributeEntity.setName(xmlAttribute.getName());
                String refPackage = xmlAttribute.getRefPackage();
                if (StringUtils.isBlank(refPackage)) {
                    refPackage = xmlPackage.getName();
                }
                attributeEntity.setRefAttr(xmlAttribute.getRef());
                attributeEntity.setRefPackage(refPackage);
                attributeEntity.setRefEntity(xmlAttribute.getRefEntity());

                // String referenceId =
                // calAttributeReference(pluginPackageEntity, dataModelEntity,
                // entity, xmlAttribute);
                // attributeEntity.setReferenceId(referenceId);

                pluginPackageAttributesMapper.insert(attributeEntity);

                savedAttributes.add(attributeEntity);
            }
        }

        for (PluginPackageAttributes attribute : savedAttributes) {
            String referenceId = calAttributeReference(pluginPackageEntity, dataModelEntity, attribute);
            if (StringUtils.isNoneBlank(referenceId)) {
                attribute.setReferenceId(referenceId);
                pluginPackageAttributesMapper.updateByPrimaryKey(attribute);
            }
        }

    }

    private String calAttributeReference(PluginPackages pluginPackageEntity, PluginPackageDataModel dataModelEntity,
            PluginPackageAttributes attribute) {
        String refAttr = attribute.getRefAttr();
        if (StringUtils.isBlank(refAttr)) {
            return null;
        }

        String refPackageName = attribute.getRefPackage();
        String refEntityName = attribute.getRefEntity();

        PluginPackageEntities latestRefEntitiesEntity = pluginPackageEntitiesMapper
                .selectLatestByPackageNameAndEntityName(refPackageName, refEntityName);

        if (latestRefEntitiesEntity == null) {
            log.error("cannot find reference entity for {} {} {}", refPackageName, refEntityName, refAttr);
            String errMsg = String.format("Cannot find reference entity for %s:%s:%s", refPackageName, refEntityName,
                    refAttr);
            throw new WecubeCoreException(errMsg);
        }

        PluginPackageAttributes latestRefAttr = pluginPackageAttributesMapper
                .selectLatestAttributeByPackageAndEntityAndAttr(refPackageName, refEntityName, refAttr);

        if (latestRefAttr == null) {
            return null;
        }

        return latestRefAttr.getId();
    }

    private void processSystemVaraibles(SystemParametersType xmlSystemParameters, PackageType xmlPackage,
            PluginPackages pluginPackageEntity) {
        if (xmlSystemParameters == null) {
            return;
        }

        List<SystemParameterType> xmlSystemParameterList = xmlSystemParameters.getSystemParameter();
        if (xmlSystemParameterList == null || xmlSystemParameterList.isEmpty()) {
            return;
        }

        for (SystemParameterType xmlSystemParameter : xmlSystemParameterList) {
            SystemVariables systemVariableEntity = new SystemVariables();
            systemVariableEntity.setId(LocalIdGenerator.generateId());
            systemVariableEntity.setName(xmlSystemParameter.getName());
            systemVariableEntity.setPackageName(xmlPackage.getName());
            String scopeType = xmlSystemParameter.getScopeType();
            if (!SystemVariables.SCOPE_GLOBAL.equalsIgnoreCase(scopeType)) {
                scopeType = xmlPackage.getName();
            }
            systemVariableEntity.setScope(scopeType);
            systemVariableEntity.setDefaultValue(xmlSystemParameter.getDefaultValue());
            systemVariableEntity.setValue(xmlSystemParameter.getValue());
            systemVariableEntity.setStatus(SystemVariables.INACTIVE);
            systemVariableEntity.setSource(PluginPackages.buildSystemVariableSource(pluginPackageEntity));
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
            processSinglePlugin(xmlPlugin, pluginPackageEntity, xmlPackage);
        }
    }

    private void processSinglePlugin(PluginType xmlPlugin, PluginPackages pluginPackageEntity, PackageType xmlPackage) {
        if (log.isInfoEnabled()) {
            log.info("process single plugin:{}", xmlPlugin.getName());
        }
        PluginConfigs pluginConfigEntity = new PluginConfigs();
        pluginConfigEntity.setId(LocalIdGenerator.generateId());
        pluginConfigEntity.setName(xmlPlugin.getName());
        pluginConfigEntity.setPluginPackageId(pluginPackageEntity.getId());
        pluginConfigEntity.setRegisterName(xmlPlugin.getRegisterName());
        pluginConfigEntity.setStatus(PluginConfigs.DISABLED);
        pluginConfigEntity.setTargetEntity(xmlPlugin.getTargetEntity());

        String targetEntityFilterRule = xmlPlugin.getTargetEntityFilterRule() == null
                ? DEFAULT_TARGET_ENTITY_FILTER_RULE
                : xmlPlugin.getTargetEntityFilterRule();
        pluginConfigEntity.setTargetEntityFilterRule(targetEntityFilterRule);
        pluginConfigEntity.setTargetPackage(xmlPlugin.getTargetPackage());

        pluginConfigsMapper.insert(pluginConfigEntity);

        List<InterfaceType> xmlInterfaces = xmlPlugin.getInterface();

        if (xmlInterfaces != null) {
            for (InterfaceType xmlIntf : xmlInterfaces) {
                processDefinedInterfaces(xmlIntf, pluginPackageEntity, pluginConfigEntity);
            }
        }

        RoleBindsType xmlRoleBinds = xmlPlugin.getRoleBinds();
        processRoleBinds(xmlRoleBinds, pluginConfigEntity);
        
        processParamObjects(xmlPackage.getParamObjects(), xmlPackage.getName(), xmlPackage.getVersion(), pluginConfigEntity.getId());

    }

    private void processDefinedInterfaces(InterfaceType xmlIntf, PluginPackages pluginPackageEntity,
            PluginConfigs pluginConfigEntity) {
        if (log.isInfoEnabled()) {
            log.info("process interfaces...");
        }
        if (xmlIntf == null) {
            return;
        }

        PluginConfigInterfaces intfEntity = new PluginConfigInterfaces();
        intfEntity.setId(LocalIdGenerator.generateId());
        intfEntity.setAction(xmlIntf.getAction());
        intfEntity.setDescription(xmlIntf.getDescription());

        String filterRule = xmlIntf.getFilterRule();
        if (StringUtils.isBlank(filterRule)) {
            filterRule = DEFAULT_FILTER_RULE_FOR_INTERFACE;
        }
        intfEntity.setFilterRule(filterRule);

        String httpMethod = xmlIntf.getHttpMethod();
        if (StringUtils.isBlank(httpMethod)) {
            httpMethod = "POST";
        }
        intfEntity.setHttpMethod(httpMethod);
        String asyncProcessing = xmlIntf.getIsAsyncProcessing();
        if (StringUtils.isBlank(asyncProcessing)) {
            asyncProcessing = PluginConfigInterfaces.DEFAULT_IS_ASYNC_PROCESSING_VALUE;
        }
        intfEntity.setIsAsyncProcessing(asyncProcessing);
        intfEntity.setPath(xmlIntf.getPath());
        intfEntity.setPluginConfigId(pluginConfigEntity.getId());

        String interfaceType = xmlIntf.getType();
        if (StringUtils.isBlank(interfaceType)) {
            interfaceType = PluginConfigInterfaces.DEFAULT_INTERFACE_TYPE;
        }
        intfEntity.setType(interfaceType);
        //
        String serviceName = intfEntity.generateServiceName(pluginPackageEntity, pluginConfigEntity);
        intfEntity.setServiceName(serviceName);
        intfEntity.setServiceDisplayName(serviceName);

        pluginConfigInterfacesMapper.insert(intfEntity);

        InputParametersType xmlInputParameters = xmlIntf.getInputParameters();
        if (xmlInputParameters != null) {
            processInputParameters(xmlInputParameters, pluginPackageEntity, pluginConfigEntity, intfEntity);
        }

        OutputParametersType xmlOutputParameters = xmlIntf.getOutputParameters();
        if (xmlOutputParameters != null) {
            processOutputParameters(xmlOutputParameters, pluginPackageEntity, pluginConfigEntity, intfEntity);
        }
    }

    private void processOutputParameters(OutputParametersType xmlOutputParameters, PluginPackages pluginPackageEntity,
            PluginConfigs pluginConfigEntity, PluginConfigInterfaces intfEntity) {

        if (xmlOutputParameters == null) {
            return;
        }

        List<OutputParameterType> xmlParameterList = xmlOutputParameters.getParameter();
        if (xmlParameterList == null || xmlParameterList.isEmpty()) {
            return;
        }

        for (OutputParameterType xmlParameter : xmlParameterList) {
            PluginConfigInterfaceParameters paramEntity = new PluginConfigInterfaceParameters();
            paramEntity.setDataType(xmlParameter.getDatatype());
            paramEntity.setId(LocalIdGenerator.generateId());
            paramEntity.setMappingEntityExpression(xmlParameter.getMappingEntityExpression());
            paramEntity.setMappingType(xmlParameter.getMappingType());
            paramEntity.setName(xmlParameter.getValue());
            paramEntity.setDescription(xmlParameter.getDescription());
            paramEntity.setPluginConfigInterface(intfEntity);
            paramEntity.setPluginConfigInterfaceId(intfEntity.getId());

            String sensitiveData = xmlParameter.getSensitiveData();
            if (StringUtils.isBlank(sensitiveData)) {
                sensitiveData = DEFAULT_SENSITIVE_DATA;
            }
            paramEntity.setSensitiveData(sensitiveData);
            paramEntity.setType(PluginConfigInterfaceParameters.TYPE_OUTPUT);

            pluginConfigInterfaceParameters.insert(paramEntity);

            intfEntity.getOutputParameters().add(paramEntity);
        }
    }

    private void processInputParameters(InputParametersType xmlInputParameters, PluginPackages pluginPackageEntity,
            PluginConfigs pluginConfigEntity, PluginConfigInterfaces intfEntity) {
        if (xmlInputParameters == null) {
            return;
        }

        List<InputParameterType> xmlParameterList = xmlInputParameters.getParameter();
        if (xmlParameterList == null || xmlParameterList.isEmpty()) {
            return;
        }

        for (InputParameterType xmlParameter : xmlParameterList) {
            PluginConfigInterfaceParameters paramEntity = new PluginConfigInterfaceParameters();
            paramEntity.setDataType(xmlParameter.getDatatype());
            paramEntity.setId(LocalIdGenerator.generateId());
            paramEntity.setMappingEntityExpression(xmlParameter.getMappingEntityExpression());
            paramEntity.setMappingSystemVariableName(xmlParameter.getMappingSystemVariableName());
            paramEntity.setMappingType(xmlParameter.getMappingType());
            paramEntity.setName(xmlParameter.getValue());
            paramEntity.setPluginConfigInterface(intfEntity);
            paramEntity.setPluginConfigInterfaceId(intfEntity.getId());
            paramEntity.setDescription(xmlParameter.getDescription());
            String required = xmlParameter.getRequired();
            if (StringUtils.isBlank(required)) {
                required = DEFAULT_REQUIRED;
            }
            paramEntity.setRequired(required);

            String sensitiveData = xmlParameter.getSensitiveData();
            if (StringUtils.isBlank(sensitiveData)) {
                sensitiveData = DEFAULT_SENSITIVE_DATA;
            }
            paramEntity.setSensitiveData(sensitiveData);
            paramEntity.setType(PluginConfigInterfaceParameters.TYPE_INPUT);

            pluginConfigInterfaceParameters.insert(paramEntity);

            intfEntity.getInputParameters().add(paramEntity);
        }
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
            PluginPackages pluginPackageEntity) throws IOException {
        File pluginUiPackageFile = new File(localFilePath, pluginProperties.getUiFile());
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
        } else {
            pluginPackageEntity.setUiPackageIncluded(false);
        }

        for (PluginPackageResourceFiles fileEntity : pluginPackageResourceFilesEntities) {
            pluginPackageResourceFilesMapper.insert(fileEntity);
        }

        log.info("total {} resource files saved for {}", pluginPackageResourceFilesEntities.size(),
                xmlPackage.getName());

        return pluginPackageResourceFilesEntities;
    }

    private List<PluginPackageResourceFiles> parseAllPluginPackageResourceFile(PluginPackages pluginPackageEntity,
            String sourceZipFile, String sourceZipFileName) throws IOException {
        List<PluginPackageResourceFiles> pluginPackageResourceFilesEntities = new ArrayList<>();
        try (ZipFile zipFile = new ZipFile(sourceZipFile)) {
            Enumeration<?> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
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

    private void checkLocalFilePath(File localFilePath) {
        if (!localFilePath.exists()) {
            if (localFilePath.mkdirs()) {
                log.info("Create directory [{}] successful", localFilePath.getAbsolutePath());
            } else {
                String msg = String.format("Create directory [%s] failed", localFilePath.getAbsolutePath());
                throw new WecubeCoreException("3103", msg, localFilePath.getAbsolutePath());
            }
        }
    }

    private String buildArtifactUrl(String releaseFileUrl, String keyName) {
        int index = releaseFileUrl.lastIndexOf("/");
        return String.format("%s%s", releaseFileUrl.substring(0, index + 1), keyName);
    }

    private List<S3PluginActifactDto> parseReleaseFile(String releaseFileUrl) throws IOException {
        byte[] contents = restTemplate.getForObject(releaseFileUrl, byte[].class);

        ByteArrayInputStream bais = new ByteArrayInputStream(contents);

        BufferedReader br = new BufferedReader(new InputStreamReader(bais));
        String line = null;
        List<S3PluginActifactDto> results = new ArrayList<>();
        while ((line = br.readLine()) != null) {
            S3PluginActifactDto dto = new S3PluginActifactDto();
            dto.setBucketName(line);
            dto.setKeyName(line);

            results.add(dto);
        }

        return results;
    }

    private S3PluginActifactPullRequestDto buildS3PluginActifactPullRequestDto(PluginArtifactPullReq req) {
        S3PluginActifactPullRequestDto dto = new S3PluginActifactPullRequestDto();
        dto.setBucketName(req.getBucketName());
        dto.setKeyName(req.getKeyName());
        dto.setState(req.getState());
        dto.setRequestId(req.getId());
        dto.setTotalSize(req.getTotalSize());
        dto.setErrorMessage(req.getErrMsg());
        dto.setPackageId(req.getPkgId());
        return dto;
    }

    private PluginArtifactPullReq getPluginArtifactPullRequestEntity(PluginArtifactPullContext ctx) {
        PluginArtifactPullReq reqEntity = pluginArtifactPullReqMapper.selectByPrimaryKey(ctx.getRequestId());

        if (reqEntity == null) {
            reqEntity = ctx.getEntity();
        }
        if (reqEntity == null) {
            throw new WecubeCoreException("3102", String.format("Request entity %s does not exist", ctx.getRequestId()),
                    ctx.getRequestId());
        }

        return reqEntity;
    }

    private String calculatePluginPackageFileName(PluginArtifactPullContext ctx) {
        String keyName = ctx.getKeyName();
        int index = keyName.lastIndexOf("/");
        if (index >= 0) {
            return keyName.substring(index);
        } else {
            return keyName;
        }
    }

}
