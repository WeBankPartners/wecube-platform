package com.webank.wecube.platform.core.service.plugin;

import static com.google.common.collect.Sets.newLinkedHashSet;
import static com.webank.wecube.platform.core.domain.plugin.PluginPackage.Status.DECOMMISSIONED;
import static com.webank.wecube.platform.core.domain.plugin.PluginPackage.Status.REGISTERED;
import static com.webank.wecube.platform.core.domain.plugin.PluginPackage.Status.UNREGISTERED;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.FileUtils;
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

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.webank.wecube.platform.core.commons.ApplicationProperties.PluginProperties;
import com.webank.wecube.platform.core.commons.AuthenticationContextHolder;
import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.domain.MenuItem;
import com.webank.wecube.platform.core.domain.SystemVariable;
import com.webank.wecube.platform.core.domain.plugin.PluginArtifactPullRequestEntity;
import com.webank.wecube.platform.core.domain.plugin.PluginConfig;
import com.webank.wecube.platform.core.domain.plugin.PluginInstance;
import com.webank.wecube.platform.core.domain.plugin.PluginPackage;
import com.webank.wecube.platform.core.domain.plugin.PluginPackageAuthority;
import com.webank.wecube.platform.core.domain.plugin.PluginPackageDependency;
import com.webank.wecube.platform.core.domain.plugin.PluginPackageMenu;
import com.webank.wecube.platform.core.domain.plugin.PluginPackageResourceFile;
import com.webank.wecube.platform.core.domain.plugin.PluginPackageRuntimeResourcesDocker;
import com.webank.wecube.platform.core.domain.plugin.PluginPackageRuntimeResourcesMysql;
import com.webank.wecube.platform.core.domain.plugin.PluginPackageRuntimeResourcesS3;
import com.webank.wecube.platform.core.dto.MenuItemDto;
import com.webank.wecube.platform.core.dto.PluginConfigDto;
import com.webank.wecube.platform.core.dto.PluginConfigGroupByNameDto;
import com.webank.wecube.platform.core.dto.PluginPackageDataModelDto;
import com.webank.wecube.platform.core.dto.PluginPackageDependencyDto;
import com.webank.wecube.platform.core.dto.PluginPackageDto;
import com.webank.wecube.platform.core.dto.PluginPackageRuntimeResouceDto;
import com.webank.wecube.platform.core.dto.S3PluginActifactDto;
import com.webank.wecube.platform.core.dto.S3PluginActifactPullRequestDto;
import com.webank.wecube.platform.core.dto.user.RoleDto;
import com.webank.wecube.platform.core.jpa.MenuItemRepository;
import com.webank.wecube.platform.core.jpa.PluginArtifactPullRequestRepository;
import com.webank.wecube.platform.core.jpa.PluginConfigRepository;
import com.webank.wecube.platform.core.jpa.PluginPackageDependencyRepository;
import com.webank.wecube.platform.core.jpa.PluginPackageRepository;
import com.webank.wecube.platform.core.jpa.PluginPackageResourceFileRepository;
import com.webank.wecube.platform.core.jpa.SystemVariableRepository;
import com.webank.wecube.platform.core.parser.PluginConfigXmlValidator;
import com.webank.wecube.platform.core.parser.PluginPackageDataModelDtoValidator;
import com.webank.wecube.platform.core.parser.PluginPackageValidator;
import com.webank.wecube.platform.core.parser.PluginPackageXmlParser;
import com.webank.wecube.platform.core.service.CommandService;
import com.webank.wecube.platform.core.service.PluginPackageDataModelService;
import com.webank.wecube.platform.core.service.ScpService;
import com.webank.wecube.platform.core.service.plugin.PluginArtifactOperationExecutor.PluginArtifactPullContext;
import com.webank.wecube.platform.core.service.user.RoleMenuService;
import com.webank.wecube.platform.core.service.user.UserManagementService;
import com.webank.wecube.platform.core.support.S3Client;
import com.webank.wecube.platform.core.support.authserver.AsAuthorityDto;
import com.webank.wecube.platform.core.support.authserver.AsRoleAuthoritiesDto;
import com.webank.wecube.platform.core.support.authserver.AuthServerRestClient;
import com.webank.wecube.platform.core.utils.StringUtilsEx;
import com.webank.wecube.platform.core.utils.SystemUtils;

@Service
@Transactional
public class PluginPackageService {
	public static final Set<String> ACCEPTED_FILES = Sets.newHashSet("register.xml", "image.tar", "ui.zip", "init.sql",
			"upgrade.sql");

	public static final String SYS_VAR_PUBLIC_PLUGIN_ARTIFACTS_RELEASE_URL = "PLUGIN_ARTIFACTS_RELEASE_URL";

	public static final Logger log = LoggerFactory.getLogger(PluginPackageService.class);

	private static final String DEFAULT_USER = "sys";

	@Autowired
	private PluginPackageRepository pluginPackageRepository;

	@Autowired
	private PluginPackageDataModelService pluginPackageDataModelService;

	@Autowired
	private PluginPackageDependencyRepository pluginPackageDependencyRepository;

	@Autowired
	private MenuItemRepository menuItemRepository;

	@Autowired
	private PluginConfigRepository pluginConfigRepository;

	@Autowired
	private PluginPackageResourceFileRepository pluginPackageResourceFileRepository;

	@Autowired
	private PluginPackageValidator pluginPackageValidator;

	@Autowired
	private PluginPackageDataModelDtoValidator dataModelValidator;

	@Autowired
	private PluginProperties pluginProperties;

	@Autowired
	private S3Client s3Client;
	@Autowired
	private ScpService scpService;
	@Autowired
	private CommandService commandService;

	@Autowired
	private PluginInstanceService instanceService;

	@Autowired
	private PluginConfigService pluginConfigService;

	@Autowired
	private UserManagementService userManagementService;

	@Autowired
	private SystemVariableRepository systemVariableRepository;

	@Autowired
	private RoleMenuService roleMenuService;

	@Autowired
	private AuthServerRestClient authServerRestClient;

	@Autowired
	private PluginArtifactPullRequestRepository pluginArtifactPullRequestRepository;

	@Autowired
	private PluginArtifactOperationExecutor pluginArtifactOperationExecutor;

	@Autowired
	private RestTemplate restTemplate;
	
	public String exportPluginRegistersForOnePackage(String pluginPackageId){
	    if(StringUtils.isBlank(pluginPackageId)){
	        throw new WecubeCoreException("Plugin package ID cannot be blank.");
	    }
	    
	    //TODO
	    return null;
	}

	public List<S3PluginActifactDto> listS3PluginActifacts() {
		String releaseFileUrl = getGlobalSystemVariableByName(SYS_VAR_PUBLIC_PLUGIN_ARTIFACTS_RELEASE_URL);

		if (org.apache.commons.lang3.StringUtils.isBlank(releaseFileUrl)) {
			throw new WecubeCoreException("The remote plugin artifacts release file is not properly provided.");
		}

		try {
			List<S3PluginActifactDto> results = parseReleaseFile(releaseFileUrl);
			return results;
		} catch (IOException e) {
			throw new WecubeCoreException("Cannot parse release file properly.Caused by " + e.getMessage());
		}
	}

	public S3PluginActifactPullRequestDto createS3PluginActifactPullRequest(S3PluginActifactDto pullRequestDto) {
		if (pullRequestDto == null) {
			throw new WecubeCoreException("Illegal argument.");
		}

		if (org.apache.commons.lang3.StringUtils.isBlank(pullRequestDto.getKeyName())) {
			throw new WecubeCoreException("Key name cannot be blank.");
		}

		// get system variables
		String releaseFileUrl = getGlobalSystemVariableByName(SYS_VAR_PUBLIC_PLUGIN_ARTIFACTS_RELEASE_URL);

		if (org.apache.commons.lang3.StringUtils.isBlank(releaseFileUrl)) {
			throw new WecubeCoreException("The remote plugin artifacts release file is not properly provided.");
		}

		PluginArtifactPullRequestEntity entity = new PluginArtifactPullRequestEntity();
		entity.setBucketName(null);
		entity.setKeyName(pullRequestDto.getKeyName());
		entity.setRev(0);
		entity.setState(PluginArtifactPullRequestEntity.STATE_IN_PROGRESS);
		entity.setCreatedTime(new Date());
		entity.setCreatedBy(AuthenticationContextHolder.getCurrentUsername());

		PluginArtifactPullRequestEntity savedEntity = pluginArtifactPullRequestRepository.saveAndFlush(entity);

		PluginArtifactPullContext ctx = new PluginArtifactPullContext();
		ctx.setAccessKey(null);
		ctx.setBucketName(null);
		ctx.setKeyName(pullRequestDto.getKeyName());
		ctx.setRemoteEndpoint(releaseFileUrl);
		ctx.setSecretKey(null);
		ctx.setRequestId(savedEntity.getId());

		pluginArtifactOperationExecutor.pullPluginArtifact(ctx);

		return buildS3PluginActifactPullRequestDto(savedEntity);
	}

	public void handlePullPluginArtifactFailure(PluginArtifactPullContext ctx, Exception e) {
		Optional<PluginArtifactPullRequestEntity> reqOpt = pluginArtifactPullRequestRepository
				.findById(ctx.getRequestId());
		if (!reqOpt.isPresent()) {
			log.warn("request entity {} does not exist", ctx.getRequestId());
			return;
		}

		PluginArtifactPullRequestEntity reqEntity = reqOpt.get();

		if (PluginArtifactPullRequestEntity.STATE_COMPLETED.equals(reqEntity.getState())) {
			return;
		}

		reqEntity.setErrorMsg(stripString(e.getMessage()));
		reqEntity.setUpdatedBy(DEFAULT_USER);
		reqEntity.setUpdatedTime(new Date());
		reqEntity.setState(PluginArtifactPullRequestEntity.STATE_FAULTED);

		pluginArtifactPullRequestRepository.saveAndFlush(reqEntity);
	}

	public void pullPluginArtifact(PluginArtifactPullContext ctx) throws Exception {

		PluginArtifactPullRequestEntity reqEntity = getPluginArtifactPullRequestEntity(ctx);

		if (PluginArtifactPullRequestEntity.STATE_COMPLETED.equals(reqEntity.getState())) {
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

		PluginPackage savedPluginPackage = parsePackageFile(dest, localFilePath);

		reqEntity.setUpdatedBy(DEFAULT_USER);
		reqEntity.setUpdatedTime(new Date());
		reqEntity.setTotalSize(downloadedFile.length());
		reqEntity.setPackageId(savedPluginPackage.getId());
		reqEntity.setState(PluginArtifactPullRequestEntity.STATE_COMPLETED);

		pluginArtifactPullRequestRepository.saveAndFlush(reqEntity);
	}

	public S3PluginActifactPullRequestDto queryS3PluginActifactPullRequest(String requestId) {
		if (org.apache.commons.lang3.StringUtils.isBlank(requestId)) {
			throw new WecubeCoreException("Request ID cannot be null.");
		}

		Optional<PluginArtifactPullRequestEntity> reqOpt = pluginArtifactPullRequestRepository.findById(requestId);
		if (!reqOpt.isPresent()) {
			throw new WecubeCoreException(String.format("Such request with %s does not exist.", requestId));
		}

		PluginArtifactPullRequestEntity req = reqOpt.get();
		return buildS3PluginActifactPullRequestDto(req);
	}

	@Transactional
	public PluginPackage uploadPackage(MultipartFile pluginPackageFile) throws Exception {
		String pluginPackageFileName = pluginPackageFile.getName();

		// 1. save package file to local
		String tmpFileName = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
		File localFilePath = new File(SystemUtils.getTempFolderPath() + tmpFileName + "/");
		log.info("tmpFilePath= {}", localFilePath.getName());
		if (!localFilePath.exists()) {
			if (localFilePath.mkdirs()) {
				log.info("Create directory [{}] successful", localFilePath.getAbsolutePath());
			} else {
				throw new WecubeCoreException("Create directory [{}] failed");
			}
		}
		File dest = new File(localFilePath + "/" + pluginPackageFileName);
		log.info("new file location: {}, filename: {}, canonicalpath: {}, canonicalfilename: {}",
				dest.getAbsoluteFile(), dest.getName(), dest.getCanonicalPath(), dest.getCanonicalFile().getName());
		pluginPackageFile.transferTo(dest);

		PluginPackage savedPluginPackage = parsePackageFile(dest, localFilePath);

		return savedPluginPackage;
	}

	public Iterable<PluginPackage> getPluginPackages() {
		return pluginPackageRepository.findAll();
	}

	public List<String> getAllDistinctPluginPackageNameList() {
		Optional<List<String>> allDistinctPackageNameListOpt = pluginPackageRepository.findAllDistinctPackage();
		return allDistinctPackageNameListOpt.orElseGet(ArrayList::new);
	}

	@Transactional
	public PluginPackage registerPluginPackage(String pluginPackageId) {
		ensurePluginPackageExists(pluginPackageId);

		PluginPackage pluginPackage = pluginPackageRepository.findById(pluginPackageId).get();

		ensurePluginPackageIsAllowedToRegister(pluginPackage);

		ensureNoMoreThanTwoActivePackages(pluginPackage);

		createRolesIfNotExistInSystem(pluginPackage);

		bindRoleToMenuWithAuthority(pluginPackage);

		updateSystemVariableStatus(pluginPackage);

		deployPluginUiResourcesIfRequired(pluginPackage);

		pluginPackage.setStatus(REGISTERED);

		return pluginPackageRepository.save(pluginPackage);
	}

	public void decommissionPluginPackage(String pluginPackageId) {
		ensurePluginPackageExists(pluginPackageId);

		ensureNoPluginInstanceIsRunningForPluginPackage(pluginPackageId);

		PluginPackage pluginPackage = pluginPackageRepository.findById(pluginPackageId).get();

		pluginConfigService.disableAllPluginsForPluginPackage(pluginPackageId);

		deactivateSystemVariables(pluginPackage);

		decommissionPluginPackageAndDisableAllItsPlugins(pluginPackage);

		removeLocalDockerImageFiles(pluginPackage);

		removePluginUiResourcesIfRequired(pluginPackage);
	}

	public void setS3Client(S3Client s3Client) {
		this.s3Client = s3Client;
	}

	public PluginPackage getPackageById(String packageId) throws WecubeCoreException {
		Optional<PluginPackage> packageFoundById = pluginPackageRepository.findById(packageId);
		if (!packageFoundById.isPresent()) {
			String msg = String.format("Cannot find package by id: [%s]", packageId);
			log.error(msg);
			throw new WecubeCoreException(msg);
		}
		return packageFoundById.get();
	}

	public PluginPackageDependencyDto getDependenciesById(String packageId) {
		PluginPackage packageFoundById = getPackageById(packageId);
		Set<PluginPackageDependency> dependencySet = packageFoundById.getPluginPackageDependencies();

		PluginPackageDependencyDto dependencyDto = new PluginPackageDependencyDto();
		dependencyDto.setPackageName(packageFoundById.getName());
		dependencyDto.setVersion(packageFoundById.getVersion());
		for (PluginPackageDependency pluginPackageDependency : dependencySet) {
			updateDependencyDto(pluginPackageDependency, dependencyDto);
		}
		return dependencyDto;
	}

	public List<MenuItemDto> getMenusById(String packageId) throws WecubeCoreException {
		List<MenuItemDto> returnMenuDto;

		// handling core's menus
		List<MenuItemDto> allSysMenus = getAllSysMenus();
		returnMenuDto = new ArrayList<>(allSysMenus);

		// handling package's menus
		PluginPackage packageFoundById = getPackageById(packageId);
		Set<PluginPackageMenu> packageMenus = packageFoundById.getPluginPackageMenus();

		for (PluginPackageMenu packageMenu : packageMenus) {
			MenuItem menuItem = menuItemRepository.findByCode(packageMenu.getCategory());
			if (null == menuItem) {
				String msg = String.format("Cannot find system menu item by package menu's category: [%s]",
						packageMenu.getCategory());
				log.error(msg);
				throw new WecubeCoreException(msg);
			}
			MenuItemDto packageMenuDto = MenuItemDto.fromPackageMenuItem(packageMenu, menuItem);
			returnMenuDto.add(packageMenuDto);
		}
		Collections.sort(returnMenuDto);
		return returnMenuDto;
	}

	public List<SystemVariable> getSystemVarsById(String packageId) {
		Optional<List<SystemVariable>> optionalSystemVariables = systemVariableRepository.findBySource(packageId);
		if (optionalSystemVariables.isPresent()) {
			return optionalSystemVariables.get();
		}
		return Collections.EMPTY_LIST;
	}

	public Set<PluginPackageAuthority> getAuthoritiesById(String packageId) {
		PluginPackage packageFoundById = getPackageById(packageId);
		return packageFoundById.getPluginPackageAuthorities();
	}

	public PluginPackageRuntimeResouceDto getResourcesById(String packageId) {
		PluginPackage packageFoundById = getPackageById(packageId);
		Set<PluginPackageRuntimeResourcesDocker> dockerSet = packageFoundById.getPluginPackageRuntimeResourcesDocker();
		Set<PluginPackageRuntimeResourcesMysql> mysqlSet = packageFoundById.getPluginPackageRuntimeResourcesMysql();
		Set<PluginPackageRuntimeResourcesS3> s3Set = packageFoundById.getPluginPackageRuntimeResourcesS3();
		return (new PluginPackageRuntimeResouceDto(dockerSet, mysqlSet, s3Set));
	}

	public List<PluginConfigGroupByNameDto> getPluginConfigsByPackageId(String packageId, boolean needInterfaceInfo) {
		List<PluginConfigGroupByNameDto> pluginConfigGroupByNameDtos = new ArrayList<PluginConfigGroupByNameDto>();
		List<PluginConfigDto> pluginConfigDtos = new ArrayList<PluginConfigDto>();
		Optional<PluginPackage> packageFoundById = pluginPackageRepository.findById(packageId);
		if (!packageFoundById.isPresent()) {
			return pluginConfigGroupByNameDtos;
		}
		Optional<List<PluginConfig>> configsOptional = pluginConfigRepository
				.findByPluginPackage_idOrderByName(packageId);
		if (!configsOptional.isPresent()) {
			return pluginConfigGroupByNameDtos;
		}
		List<PluginConfig> configs = configsOptional.get();
		if (null != configs && configs.size() > 0) {
			if (needInterfaceInfo) {
				configs.forEach(pluginConfig -> pluginConfigDtos.add(PluginConfigDto.fromDomain(pluginConfig)));
			} else {
				configs.forEach(pluginConfig -> pluginConfigDtos
						.add(PluginConfigDto.fromDomainWithoutInterfaces(pluginConfig)));
			}
		}

		for (PluginConfigDto cfgDto : pluginConfigDtos) {
			boolean continueFlag = false;
			for (PluginConfigGroupByNameDto cfgGroupByName : pluginConfigGroupByNameDtos) {
				if (cfgDto.getName().equals(cfgGroupByName.getPluginConfigName())) {
					cfgGroupByName.getPluginConfigDtoList().add(cfgDto);
					continueFlag = true;
				}
			}
			if (continueFlag)
				continue;
			pluginConfigGroupByNameDtos
					.add(new PluginConfigGroupByNameDto(cfgDto.getName(), Lists.newArrayList(cfgDto)));
		}
		return pluginConfigGroupByNameDtos;
	}

	public List<MenuItemDto> getAllSysMenus() {
		List<MenuItemDto> returnMenuDto = new ArrayList<>();

		// handling core's menus
		Iterable<MenuItem> systemMenus = menuItemRepository.findAll();

		for (MenuItem systemMenu : systemMenus) {
			MenuItemDto systemMenuDto = MenuItemDto.fromSystemMenuItem(systemMenu);
			returnMenuDto.add(systemMenuDto);
		}
		return returnMenuDto;
	}

	private PluginArtifactPullRequestEntity getPluginArtifactPullRequestEntity(PluginArtifactPullContext ctx) {
		Optional<PluginArtifactPullRequestEntity> reqOpt = pluginArtifactPullRequestRepository
				.findById(ctx.getRequestId());
		if (!reqOpt.isPresent()) {
			throw new WecubeCoreException(String.format("Request entity %s does not exist", ctx.getRequestId()));
		}

		PluginArtifactPullRequestEntity reqEntity = reqOpt.get();

		return reqEntity;
	}

	private void checkLocalFilePath(File localFilePath) {
		if (!localFilePath.exists()) {
			if (localFilePath.mkdirs()) {
				log.info("Create directory [{}] successful", localFilePath.getAbsolutePath());
			} else {
				throw new WecubeCoreException("Create directory [{}] failed");
			}
		}
	}

	private void updateSystemVariableStatus(PluginPackage pluginPackage) {
		List<String> packageIdList = new ArrayList<String>();
		pluginPackageRepository.findAllByName(pluginPackage.getName()).forEach(pkg -> packageIdList.add(pkg.getId()));

		List<SystemVariable> pluginSystemVariables = systemVariableRepository.findAllBySourceIn(packageIdList);
		pluginSystemVariables.forEach(pluginSystemVariable -> {
			if (SystemVariable.ACTIVE.equals(pluginSystemVariable.getStatus())
					&& !pluginPackage.getId().equals(pluginSystemVariable.getSource())) {
				pluginSystemVariable.deactivate();
			}
			if (SystemVariable.INACTIVE.equals(pluginSystemVariable.getStatus())
					&& pluginPackage.getId().equals(pluginSystemVariable.getSource())) {
				pluginSystemVariable.activate();
			}
		});

		systemVariableRepository.saveAll(pluginSystemVariables);
	}

	void createRolesIfNotExistInSystem(PluginPackage pluginPackage) {
		Set<PluginPackageAuthority> pluginPackageAuthorities = pluginPackage.getPluginPackageAuthorities();
		if (null != pluginPackageAuthorities && pluginPackageAuthorities.size() > 0) {
			List<RoleDto> roleDtos = userManagementService.retrieveAllRoles();
			Set<String> existingRoleNames = new HashSet<>();
			if (null != roleDtos && roleDtos.size() > 0) {
				existingRoleNames = roleDtos.stream().map(roleDto -> roleDto.getName()).collect(Collectors.toSet());
			}
			Set<String> roleNamesInPlugin = pluginPackageAuthorities.stream().map(authority -> authority.getRoleName())
					.collect(Collectors.toSet());
			Set<String> roleNamesDefinedInPluginButNotExistInSystem = Sets.difference(roleNamesInPlugin,
					existingRoleNames);
			if (!roleNamesDefinedInPluginButNotExistInSystem.isEmpty()) {
				roleNamesDefinedInPluginButNotExistInSystem.forEach(it -> {
					RoleDto rd = new RoleDto();
					rd.setName(it);
					rd.setDisplayName(it);
					userManagementService.registerLocalRole(rd);
				});
			}
		}
	}

	void bindRoleToMenuWithAuthority(PluginPackage pluginPackage) throws WecubeCoreException {
		final Set<PluginPackageAuthority> pluginPackageAuthorities = pluginPackage.getPluginPackageAuthorities();
		final List<String> selfPkgMenuCodeList = pluginPackage.getPluginPackageMenus().stream()
				.map(PluginPackageMenu::getCode).collect(Collectors.toList());
		if (null != pluginPackageAuthorities && pluginPackageAuthorities.size() > 0) {
			pluginPackageAuthorities.forEach(pluginPackageAuthority -> {
				final String roleName = pluginPackageAuthority.getRoleName();
				final String menuCode = pluginPackageAuthority.getMenuCode();

				// create role menu binding
				if (!selfPkgMenuCodeList.contains(menuCode)) {
					String msg = String.format(
							"The declared menu code: [%s] in <authorities> field doesn't declared in <menus> field of register.xml",
							menuCode);
					log.error(msg);
					throw new WecubeCoreException(msg);
				}
				this.roleMenuService.createRoleMenuBinding(roleName, menuCode);

				// grant authority to role and send request to auth server
				AsAuthorityDto grantAuthority = new AsAuthorityDto();
				grantAuthority.setCode(menuCode);
				AsRoleAuthoritiesDto grantAuthorityToRole = new AsRoleAuthoritiesDto();
				grantAuthorityToRole.setRoleName(roleName);
				grantAuthorityToRole.setAuthorities(Collections.singletonList(grantAuthority));
				this.authServerRestClient.configureRoleAuthoritiesWithRoleName(grantAuthorityToRole);
			});
		}
	}

	private void ensurePluginPackageIsAllowedToRegister(PluginPackage pluginPackage) {
		if (UNREGISTERED != pluginPackage.getStatus()) {
			String errorMessage = String.format(
					"Failed to register PluginPackage[%s/%s] as it is not in UNREGISTERED status [%s]",
					pluginPackage.getName(), pluginPackage.getVersion(), pluginPackage.getStatus());
			log.warn(errorMessage);
			throw new WecubeCoreException(errorMessage);
		}
	}

	private void ensureNoMoreThanTwoActivePackages(PluginPackage pluginPackage) {
		Optional<List<PluginPackage>> allByNameAndStatus = pluginPackageRepository
				.findAllActiveByNameOrderByUploadTimestampAsc(pluginPackage.getName());
		if (allByNameAndStatus.isPresent()) {
			List<PluginPackage> pluginPackages = allByNameAndStatus.get();
			if (pluginPackages.size() > 1) {
				String activePackagesString = pluginPackages.stream()
						.map(it -> String.join(":", it.getName(), it.getVersion(), it.getStatus().name()))
						.collect(Collectors.joining(","));
				throw new WecubeCoreException(String.format(
						"Not allowed to register more packages. Current active packages: [%s]", activePackagesString));
			}
		}
	}

	private void deactivateSystemVariables(PluginPackage pluginPackage) {
		Optional<List<SystemVariable>> systemVariablesOptional = systemVariableRepository
				.findBySource(pluginPackage.getId());
		if (systemVariablesOptional.isPresent()) {
			List<SystemVariable> systemVariablesFromDb = systemVariablesOptional.get();
			if (systemVariablesFromDb.size() > 0) {
				Set<SystemVariable> systemVariables = systemVariablesFromDb.stream()
						.filter(systemVariable -> SystemVariable.ACTIVE.equals(systemVariable.getStatus())
								&& pluginPackage.getId().equals(systemVariable.getSource())
								&& pluginPackage.getName().equals(systemVariable.getScope()))
						.map(systemVariable -> systemVariable.deactivate()).collect(Collectors.toSet());
				systemVariableRepository.saveAll(systemVariables);
			}
		}
	}

	private void removeLocalDockerImageFiles(PluginPackage pluginPackage) {
		// Remove related docker image file
		String versionPath = SystemUtils.getTempFolderPath() + pluginPackage.getName() + "-"
				+ pluginPackage.getVersion() + "/";
		File versionDirectory = new File(versionPath);
		try {
			log.info("Delete directory: {}", versionPath);
			FileUtils.deleteDirectory(versionDirectory);
		} catch (IOException e) {
			log.error("Remove plugin package file failed: {}", e);
			throw new WecubeCoreException("Remove plugin package file failed.");
		}
	}

	private PluginPackage decommissionPluginPackageAndDisableAllItsPlugins(PluginPackage pluginPackage) {

		pluginPackage.setStatus(DECOMMISSIONED);

		pluginPackageRepository.save(pluginPackage);

		return pluginPackage;
	}

	private void ensureNoPluginInstanceIsRunningForPluginPackage(String pluginPackageId) {
		List<PluginInstance> pluginInstances = instanceService.getAvailableInstancesByPackageId(pluginPackageId);
		if (null != pluginInstances && pluginInstances.size() > 0) {
			throw new WecubeCoreException(String.format(
					"Decommission plugin package [%s] failure. There are still %d plugin instance%s running",
					pluginPackageId, pluginInstances.size(), pluginInstances.size() > 1 ? "es" : ""));
		}
	}

	private void ensurePluginPackageExists(String pluginPackageId) {
		if (!pluginPackageRepository.existsById(pluginPackageId)) {
			throw new WecubeCoreException(String.format("Plugin package id not found for id [%s] ", pluginPackageId));
		}
	}

	private void unzipLocalFile(String sourceZipFile, String destFilePath) throws Exception {
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

	private Optional<Set<PluginPackageResourceFile>> getAllPluginPackageResourceFile(PluginPackage pluginPackage,
			String sourceZipFile, String sourceZipFileName) throws Exception {
		Optional<Set<PluginPackageResourceFile>> pluginPackageResourceFilesOptional = Optional.empty();
		try (ZipFile zipFile = new ZipFile(sourceZipFile)) {
			Enumeration entries = zipFile.entries();
			Set<PluginPackageResourceFile> pluginPackageResourceFiles = null;
			if (entries.hasMoreElements()) {
				pluginPackageResourceFiles = newLinkedHashSet();
			}
			for (; entries.hasMoreElements();) {
				ZipEntry entry = (ZipEntry) entries.nextElement();
				if (!entry.isDirectory()) {
					String zipEntryName = entry.getName();
					PluginPackageResourceFile pluginPackageResourceFile = new PluginPackageResourceFile();
					pluginPackageResourceFile.setPluginPackage(pluginPackage);
					pluginPackageResourceFile.setSource(sourceZipFileName);
					pluginPackageResourceFile.setRelatedPath("/ui-resources/" + pluginPackage.getName() + File.separator
							+ pluginPackage.getVersion() + File.separator + zipEntryName);

					log.info("File in ui package [{}] : {}", sourceZipFileName, zipEntryName);

					pluginPackageResourceFiles.add(pluginPackageResourceFile);
				}
			}
			pluginPackageResourceFilesOptional = Optional.ofNullable(pluginPackageResourceFiles);
		}

		return pluginPackageResourceFilesOptional;
	}

	private void updateDependencyDto(PluginPackageDependency pluginPackageDependency,
			PluginPackageDependencyDto pluginPackageDependencyDto) {
		// create new dependencyDto according to input dependency
		String dependencyName = pluginPackageDependency.getDependencyPackageName();
		String dependencyVersion = pluginPackageDependency.getDependencyPackageVersion();
		PluginPackageDependencyDto dependencyDto = new PluginPackageDependencyDto();
		dependencyDto.setPackageName(dependencyName);
		dependencyDto.setVersion(dependencyVersion);

		// update the current dto recursively
		pluginPackageDependencyDto.getDependencies().add(dependencyDto);
		Optional<List<PluginPackageDependency>> dependencySetFoundByNameAndVersion = pluginPackageDependencyRepository
				.findAllByPluginPackageNameAndPluginPackageVersion(dependencyName, dependencyVersion);
		dependencySetFoundByNameAndVersion.ifPresent(pluginPackageDependencies -> {
			for (PluginPackageDependency dependency : pluginPackageDependencies) {
				updateDependencyDto(dependency, dependencyDto);
			}
		});
	}

	private void deployPluginUiResourcesIfRequired(PluginPackage pluginPackage) {
		if (!pluginPackage.isUiPackageIncluded()) {
			return;
		}
		// download UI package from MinIO
		String tmpFolderName = SystemUtils.getTempFolderPath()
				+ new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
		String downloadUiZipPath = tmpFolderName + File.separator + pluginProperties.getUiFile();
		log.info("Download UI.zip from S3 to " + downloadUiZipPath);

		String s3UiPackagePath = pluginPackage.getName() + File.separator + pluginPackage.getVersion() + File.separator
				+ pluginProperties.getUiFile();
		s3Client.downFile(pluginProperties.getPluginPackageBucketName(), s3UiPackagePath, downloadUiZipPath);

		String remotePath = pluginProperties.getStaticResourceServerPath() + File.separator + pluginPackage.getName()
				+ File.separator + pluginPackage.getVersion() + File.separator;
		log.info("Upload UI.zip from local to static server:" + remotePath);

		// get all static resource hosts
		List<String> staticResourceIps = StringUtilsEx.splitByComma(pluginProperties.getStaticResourceServerIp());

		for (String remoteIp : staticResourceIps) {

			// mkdir at remote host
			if (!remotePath.equals("/") && !remotePath.equals(".")) {
				String mkdirCmd = String.format("rm -rf %s && mkdir -p %s", remotePath, remotePath);

				try {
					commandService.runAtRemote(remoteIp, pluginProperties.getStaticResourceServerUser(),
							pluginProperties.getStaticResourceServerPassword(),
							pluginProperties.getStaticResourceServerPort(), mkdirCmd);
				} catch (Exception e) {
					log.error("Run command [mkdir] meet error: ", e.getMessage());
					throw new WecubeCoreException(String.format("Run remote command meet error: %s", e.getMessage()));
				}
			}

			// scp UI.zip to Static Resource Server
			try {
				scpService.put(remoteIp, pluginProperties.getStaticResourceServerPort(),
						pluginProperties.getStaticResourceServerUser(),
						pluginProperties.getStaticResourceServerPassword(), downloadUiZipPath, remotePath);
			} catch (Exception e) {
				throw new WecubeCoreException("Put file to remote host meet error: " + e.getMessage());
			}
			log.info("scp UI.zip to Static Resource Server - Done");

			// unzip file
			String unzipCmd = String.format("cd %s && unzip %s", remotePath, pluginProperties.getUiFile());
			try {
				commandService.runAtRemote(remoteIp, pluginProperties.getStaticResourceServerUser(),
						pluginProperties.getStaticResourceServerPassword(),
						pluginProperties.getStaticResourceServerPort(), unzipCmd);
			} catch (Exception e) {
				log.error("Run command [unzip] meet error: ", e.getMessage());
				throw new WecubeCoreException(String.format("Run remote command meet error: %s", e.getMessage()));
			}
		}

		log.info("UI package deployment has done...");

	}

	private void removePluginUiResourcesIfRequired(PluginPackage pluginPackage) {
		if (!pluginPackage.isUiPackageIncluded()) {
			return;
		}

		String remotePath = pluginProperties.getStaticResourceServerPath() + File.separator + pluginPackage.getName()
				+ File.separator + pluginPackage.getVersion() + File.separator;

		if (!remotePath.equals("/") && !remotePath.equals(".")) {
			String mkdirCmd = String.format("rm -rf %s", remotePath);
			try {
				List<String> staticResourceIps = StringUtilsEx.splitByComma(pluginProperties.getStaticResourceServerIp());
				for (String staticResourceIp : staticResourceIps) {
					commandService.runAtRemote(staticResourceIp, pluginProperties.getStaticResourceServerUser(),
							pluginProperties.getStaticResourceServerPassword(),
							pluginProperties.getStaticResourceServerPort(), mkdirCmd);
				}
			} catch (Exception e) {
				log.error("Run command [rm] meet error: ", e.getMessage());
				throw new WecubeCoreException(String.format("Run command [rm] meet error: %s", e.getMessage()));
			}
		}
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

	private String calculatePluginPackageFileName(PluginArtifactPullContext ctx) {
		String keyName = ctx.getKeyName();
		int index = keyName.lastIndexOf("/");
		if (index >= 0) {
			return keyName.substring(index);
		} else {
			return keyName;
		}
	}

	private String buildArtifactUrl(String releaseFileUrl, String keyName) {
		int index = releaseFileUrl.lastIndexOf("/");
		return String.format("%s%s", releaseFileUrl.substring(0, index + 1), keyName);
	}

	private PluginPackage parsePackageFile(File dest, File localFilePath) throws Exception {
		// 2. unzip local package file
		unzipLocalFile(dest.getCanonicalPath(), localFilePath.getCanonicalPath() + "/");

		// 3. read xml file in plugin package
		File registerXmlFile = new File(localFilePath.getCanonicalPath() + "/" + pluginProperties.getRegisterFile());
		if (!registerXmlFile.exists()) {
			throw new WecubeCoreException(String.format("Plugin package definition file: [%s] does not exist.",
					pluginProperties.getRegisterFile()));
		}

		new PluginConfigXmlValidator().validate(new FileInputStream(registerXmlFile));

		PluginPackageDto pluginPackageDto = PluginPackageXmlParser.newInstance(new FileInputStream(registerXmlFile))
				.parsePluginPackage();
		PluginPackage pluginPackage = pluginPackageDto.getPluginPackage();

		pluginPackageValidator.validate(pluginPackage);
		dataModelValidator.validate(pluginPackageDto.getPluginPackageDataModelDto());

		if (isPluginPackageExists(pluginPackage.getName(), pluginPackage.getVersion())) {
			throw new WecubeCoreException(String.format("Plugin package [name=%s, version=%s] exists.",
					pluginPackage.getName(), pluginPackage.getVersion()));
		}

		processPluginDockerImageFile(localFilePath, pluginPackageDto);

		Optional<Set<PluginPackageResourceFile>> pluginPackageResourceFilesOptional = processPluginUiPackageFile(
				localFilePath, pluginPackageDto, pluginPackage);
		processPluginInitSqlFile(localFilePath, pluginPackageDto);
		processPluginUpgradeSqlFile(localFilePath, pluginPackageDto);

		PluginPackage savedPluginPackage = pluginPackageRepository.save(pluginPackage);

		if (null != pluginPackage.getSystemVariables() && pluginPackage.getSystemVariables().size() > 0) {
			pluginPackage.getSystemVariables().stream()
					.forEach(systemVariable -> systemVariable.setSource(savedPluginPackage.getId()));
			systemVariableRepository.saveAll(pluginPackage.getSystemVariables());
		}

		PluginPackageDataModelDto pluginPackageDataModelDto = pluginPackageDataModelService
				.register(pluginPackageDto.getPluginPackageDataModelDto());

		savedPluginPackage.setPluginPackageDataModel(PluginPackageDataModelDto.toDomain(pluginPackageDataModelDto));
		if (pluginPackageResourceFilesOptional.isPresent()) {
			Set<PluginPackageResourceFile> pluginPackageResourceFiles = newLinkedHashSet(
					pluginPackageResourceFileRepository.saveAll(pluginPackageResourceFilesOptional.get()));
			savedPluginPackage.setPluginPackageResourceFiles(pluginPackageResourceFiles);
		}

		return savedPluginPackage;
	}

	private void processPluginDockerImageFile(File localFilePath, PluginPackageDto pluginPackageDto) {
		File pluginDockerImageFile = new File(localFilePath + "/" + pluginProperties.getImageFile());
		log.info("pluginDockerImageFile: {}", pluginDockerImageFile.getAbsolutePath());

		if (pluginDockerImageFile.exists()) {
			String keyName = pluginPackageDto.getName() + "/" + pluginPackageDto.getVersion() + "/"
					+ pluginDockerImageFile.getName();
			log.info("keyName : {}", keyName);

			String dockerImageUrl = s3Client.uploadFile(pluginProperties.getPluginPackageBucketName(), keyName,
					pluginDockerImageFile);
			log.info("Plugin Package has uploaded to MinIO {}", dockerImageUrl.split("\\?")[0]);
		}
	}

	private Optional<Set<PluginPackageResourceFile>> processPluginUiPackageFile(File localFilePath,
			PluginPackageDto pluginPackageDto, PluginPackage pluginPackage) throws Exception {
		File pluginUiPackageFile = new File(localFilePath + "/" + pluginProperties.getUiFile());
		log.info("pluginUiPackageFile: {}", pluginUiPackageFile.getAbsolutePath());
		String uiPackageUrl = "";
		Optional<Set<PluginPackageResourceFile>> pluginPackageResourceFilesOptional = Optional.empty();
		if (pluginUiPackageFile.exists()) {

			String keyName = pluginPackageDto.getName() + "/" + pluginPackageDto.getVersion() + "/"
					+ pluginUiPackageFile.getName();
			log.info("keyName : {}", keyName);

			pluginPackageResourceFilesOptional = getAllPluginPackageResourceFile(pluginPackage,
					pluginUiPackageFile.getAbsolutePath(), pluginUiPackageFile.getName());
			uiPackageUrl = s3Client.uploadFile(pluginProperties.getPluginPackageBucketName(), keyName,
					pluginUiPackageFile);
			pluginPackage.setUiPackageIncluded(true);
			log.info("UI static package file has uploaded to MinIO {}", uiPackageUrl.split("\\?")[0]);
		}

		return pluginPackageResourceFilesOptional;
	}

	private void processPluginInitSqlFile(File localFilePath, PluginPackageDto pluginPackageDto) {
		File pluginInitSqlFile = new File(localFilePath + File.separator + pluginProperties.getInitDbSql());
		if (pluginInitSqlFile.exists()) {
			String keyName = pluginPackageDto.getName() + "/" + pluginPackageDto.getVersion() + "/"
					+ pluginProperties.getInitDbSql();
			log.info("Uploading init sql {} to MinIO {}", pluginInitSqlFile.getAbsolutePath(), keyName);
			String initSqlUrl = s3Client.uploadFile(pluginProperties.getPluginPackageBucketName(), keyName,
					pluginInitSqlFile);
			log.info("Init sql {} has been uploaded to MinIO {}", pluginProperties.getInitDbSql(), initSqlUrl);
		} else {
			log.info("Init sql {} is not included in package.", pluginProperties.getInitDbSql());
		}
	}

	private void processPluginUpgradeSqlFile(File localFilePath, PluginPackageDto pluginPackageDto) {
		File pluginUpgradeSqlFile = new File(localFilePath + File.separator + pluginProperties.getUpgradeDbSql());
		if (pluginUpgradeSqlFile.exists()) {
			String keyName = pluginPackageDto.getName() + "/" + pluginPackageDto.getVersion() + "/"
					+ pluginProperties.getUpgradeDbSql();
			log.info("Uploading upgrade sql {} to MinIO {}", pluginUpgradeSqlFile.getAbsolutePath(), keyName);
			String upgradeSqlUrl = s3Client.uploadFile(pluginProperties.getPluginPackageBucketName(), keyName,
					pluginUpgradeSqlFile);
			log.info("Upgrade sql {} has been uploaded to MinIO {}", pluginProperties.getUpgradeDbSql(), upgradeSqlUrl);
		} else {
			log.info("Upgrade sql {} is not included in package.", pluginProperties.getUpgradeDbSql());
		}
	}

	private String getGlobalSystemVariableByName(String varName) {
		List<SystemVariable> vars = systemVariableRepository.findByNameAndScopeAndStatus(varName,
				SystemVariable.SCOPE_GLOBAL, SystemVariable.ACTIVE);
		if (vars == null || vars.isEmpty()) {
			return null;
		}

		SystemVariable var = vars.get(0);
		return var.getValue();
	}

	private boolean isPluginPackageExists(String name, String version) {
		return pluginPackageRepository.countByNameAndVersion(name, version) > 0;
	}

	private S3PluginActifactPullRequestDto buildS3PluginActifactPullRequestDto(PluginArtifactPullRequestEntity req) {
		S3PluginActifactPullRequestDto dto = new S3PluginActifactPullRequestDto();
		dto.setBucketName(req.getBucketName());
		dto.setKeyName(req.getKeyName());
		dto.setState(req.getState());
		dto.setRequestId(req.getId());
		dto.setTotalSize(req.getTotalSize());
		dto.setErrorMessage(req.getErrorMsg());
		dto.setPackageId(req.getPackageId());
		return dto;
	}
	
	private String stripString(String s) {
		if(s == null) {
			return null;
		}
		
		if(s.length() > 250) {
			return s.substring(0, 250);
		}
		
		return s;
	}

}
