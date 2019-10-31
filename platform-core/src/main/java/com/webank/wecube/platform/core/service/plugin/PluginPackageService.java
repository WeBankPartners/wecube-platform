package com.webank.wecube.platform.core.service.plugin;

import com.webank.wecube.platform.core.commons.ApplicationProperties.PluginProperties;
import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.domain.MenuItem;
import com.webank.wecube.platform.core.domain.SystemVariable;
import com.webank.wecube.platform.core.domain.plugin.*;
import com.webank.wecube.platform.core.dto.*;
import com.webank.wecube.platform.core.jpa.*;
import com.webank.wecube.platform.core.parser.PluginPackageXmlParser;
import com.webank.wecube.platform.core.service.CommandService;
import com.webank.wecube.platform.core.service.PluginPackageDataModelService;
import com.webank.wecube.platform.core.service.ScpService;
import com.webank.wecube.platform.core.support.S3Client;
import com.webank.wecube.platform.core.utils.StringUtils;
import com.webank.wecube.platform.core.utils.SystemUtils;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static com.google.common.collect.Sets.newLinkedHashSet;
import static com.webank.wecube.platform.core.domain.plugin.PluginPackage.Status.*;

@Service
@Transactional
public class PluginPackageService {
    public final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    PluginPackageRepository pluginPackageRepository;

    @Autowired
    PluginPackageDataModelService pluginPackageDataModelService;

    @Autowired
    PluginPackageDependencyRepository pluginPackageDependencyRepository;

    @Autowired
    MenuItemRepository menuItemRepository;

    @Autowired
    private PluginConfigRepository pluginConfigRepository;

    @Autowired
    private PluginPackageResourceFileRepository pluginPackageResourceFileRepository;

    @Autowired
    private PluginProperties pluginProperties;

    @Autowired
    private S3Client s3Client;
    @Autowired
    private ScpService scpService;
    @Autowired
    private CommandService commandService;

    @Autowired

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

        // 2. unzip local package file
        unzipLocalFile(dest.getCanonicalPath(), localFilePath.getCanonicalPath() + "/");

        // 3. read xml file in plugin package
        File registerXmlFile = new File(localFilePath.getCanonicalPath() + "/" + pluginProperties.getRegisterFile());
        if (!registerXmlFile.exists()) {
            throw new WecubeCoreException(String.format("Plugin package definition file: [%s] does not exist.",
                    pluginProperties.getRegisterFile()));
        }
        PluginPackageDto pluginPackageDto = PluginPackageXmlParser.newInstance(new FileInputStream(registerXmlFile))
                .parsePluginPackage();
        PluginPackage pluginPackage = pluginPackageDto.getPluginPackage();
        if (!StringUtils.containsOnlyAlphanumericOrHyphen(pluginPackage.getName())) {
            throw new WecubeCoreException(
                    String.format("Invalid plugin package name [%s] - Only alphanumeric and hyphen('-') is allowed. ",
                            pluginPackageDto.getName()));
        }
        if (isPluginPackageExists(pluginPackage.getName(), pluginPackage.getVersion())) {
            throw new WecubeCoreException(String.format("Plugin package [name=%s, version=%s] exists.",
                    pluginPackageDto.getName(), pluginPackageDto.getVersion()));
        }
        // 4.
        File pluginDockerImageFile = new File(localFilePath + "/" + pluginProperties.getImageFile());
        log.info("pluginDockerImageFile: {}", pluginDockerImageFile.getAbsolutePath());

        if (pluginDockerImageFile.exists()) {
            String keyName = pluginPackageDto.getName() + "/" + pluginPackageDto.getVersion() + "/"
                    + pluginDockerImageFile.getName();
            log.info("keyname : {}", keyName);

            String dockerImageUrl = s3Client.uploadFile(pluginProperties.getPluginPackageBucketName(), keyName,
                    pluginDockerImageFile);
            log.info("Plugin Package has uploaded to MinIO {}", dockerImageUrl.split("\\?")[0]);
        }

        File pluginUiPackageFile = new File(localFilePath + "/" + pluginPackage.getUiPackageFilename());
        log.info("pluginUiPackageFile: {}", pluginUiPackageFile.getAbsolutePath());
        String uiPackageUrl = "";
        Optional<Set<PluginPackageResourceFile>> pluginPackageResourceFilesOptional = Optional.empty();
        if (pluginUiPackageFile.exists()) {

            String keyName = pluginPackageDto.getName() + "/" + pluginPackageDto.getVersion() + "/"
                    + pluginUiPackageFile.getName();
            log.info("keyname : {}", keyName);

            pluginPackageResourceFilesOptional = getAllPluginPackageResourceFile(pluginPackage,
                    pluginUiPackageFile.getAbsolutePath(), pluginUiPackageFile.getName());
            uiPackageUrl = s3Client.uploadFile(pluginProperties.getPluginPackageBucketName(), keyName,
                    pluginUiPackageFile);
            log.info("UI static package file has uploaded to MinIO {}", uiPackageUrl.split("\\?")[0]);
        }

        PluginPackage savedPluginPackage = pluginPackageRepository.save(pluginPackage);

        List<PluginPackageEntityDto> pluginPackageEntityDtos = pluginPackageDataModelService
                .register(new ArrayList<>(pluginPackageDto.getPluginPackageEntities()));

        Set<PluginConfig> pluginConfigs = newLinkedHashSet();
        for (PluginConfig pluginConfig : savedPluginPackage.getPluginConfigs()) {
            for (PluginPackageEntityDto pluginPackageEntityDto : pluginPackageEntityDtos) {
                String entityNameFromPluginConfig = pluginConfig.getEntityName();
                String nameFromDto = pluginPackageEntityDto.getName();
                if (entityNameFromPluginConfig.equals(nameFromDto)) {
                    pluginConfig.setEntityId(pluginPackageEntityDto.getId());
                }
            }
            pluginConfigs.add(pluginConfig);
        }
        pluginConfigRepository.saveAll(pluginConfigs);

        savedPluginPackage.setPluginPackageEntities(pluginPackageEntityDtos.stream()
                .map(it -> it.toDomain(savedPluginPackage)).collect(Collectors.toSet()));
        if (pluginPackageResourceFilesOptional.isPresent()) {
            Set<PluginPackageResourceFile> pluginPackageResourceFiles = newLinkedHashSet(
                    pluginPackageResourceFileRepository.saveAll(pluginPackageResourceFilesOptional.get()));
            savedPluginPackage.setPluginPackageResourceFiles(pluginPackageResourceFiles);
        }

        return savedPluginPackage;
    }

    private boolean isPluginPackageExists(String name, String version) {
        return pluginPackageRepository.countByNameAndVersion(name, version) > 0;
    }

    public Iterable<PluginPackage> getPluginPackages() {
        return pluginPackageRepository.findAll();
    }

    public PluginPackage registerPluginPackage(int pluginPackageId) {
        if (!pluginPackageRepository.existsById(pluginPackageId)) {
            throw new WecubeCoreException(String.format("Plugin package id not found for id [%s]", pluginPackageId));
        }
        PluginPackage pluginPackage = pluginPackageRepository.findById(pluginPackageId).get();
        if (UNREGISTERED != pluginPackage.getStatus()) {
            String errorMessage = String.format(
                    "Failed to register PluginPackage[%s/%s] as it is not in UNREGISTERED status [%s]",
                    pluginPackage.getName(), pluginPackage.getVersion(), pluginPackage.getStatus());
            log.warn(errorMessage);
            throw new WecubeCoreException(errorMessage);
        }

        if (pluginPackage.getPluginPackageResourceFiles().size() != 0) {
            deployPluginUiResources(pluginPackage);
        }
        pluginPackage.setStatus(REGISTERED);
        return pluginPackageRepository.save(pluginPackage);
    }

    public void decommissionPluginPackage(int pluginPackageId) {
        if (!pluginPackageRepository.existsById(pluginPackageId)) {
            throw new WecubeCoreException(String.format("Plugin package id not found for id [%s] ", pluginPackageId));
        }
        PluginPackage pluginPackage = pluginPackageRepository.findById(pluginPackageId).get();
        if (RUNNING.equals(pluginPackage.getStatus())) {
            String errorMessage = String.format("Failed to decommission Plugin[%s/%s] due to package is RUNNING",
                    pluginPackage.getName(), pluginPackage.getVersion());
            log.warn(errorMessage);
            throw new WecubeCoreException(errorMessage);
        }
        pluginPackage.setStatus(DECOMMISSIONED);
        pluginPackageRepository.save(pluginPackage);

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

        removePluginUiResources(pluginPackage);
    }

    private void unzipLocalFile(String sourceZipFile, String destFilePath) throws Exception {
        try (ZipFile zipFile = new ZipFile(sourceZipFile)) {
            Enumeration entries = zipFile.entries();

            for (; entries.hasMoreElements();) {
                ZipEntry entry = (ZipEntry) entries.nextElement();
                String zipEntryName = entry.getName();
                if (entry.isDirectory() || !(zipEntryName.contains(".xml") || zipEntryName.contains(".tar")
                        || zipEntryName.contains(".zip") || zipEntryName.contains(".sql"))) {
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
                    pluginPackageResourceFile.setRelatedPath(pluginPackage.getName() + File.separator
                            + pluginPackage.getVersion() + File.separator + zipEntryName);

                    log.info("File in ui package [{}] : {}", sourceZipFileName, zipEntryName);

                    pluginPackageResourceFiles.add(pluginPackageResourceFile);
                }
            }
            pluginPackageResourceFilesOptional = Optional.ofNullable(pluginPackageResourceFiles);
        }

        return pluginPackageResourceFilesOptional;
    }

    public void setS3Client(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public PluginPackage getPackageById(Integer packageId) throws WecubeCoreException {
        Optional<PluginPackage> packageFoundById = pluginPackageRepository.findById(packageId);
        if (!packageFoundById.isPresent()) {
            String msg = String.format("Cannot find package by id: [%d]", packageId);
            log.error(msg);
            throw new WecubeCoreException(msg);
        }
        return packageFoundById.get();
    }

    public PluginPackageDependencyDto getDependenciesById(Integer packageId) {
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

    public List<MenuItemDto> getMenusById(Integer packageId) throws WecubeCoreException {
        List<MenuItemDto> returnMenuDto;

        // handling core's menus
        List<MenuItemDto> allSysMenus = getAllSysMenus();
        returnMenuDto = new ArrayList<>(allSysMenus);

        // update categoryToId mapping, which is system menu's category to its latest id
        Map<String, Integer> categoryToId = updateCategoryToIdMapping(returnMenuDto);

        // handling package's menus
        PluginPackage packageFoundById = getPackageById(packageId);
        Set<PluginPackageMenu> packageMenus = packageFoundById.getPluginPackageMenus();

        for (PluginPackageMenu packageMenu : packageMenus) {
            String transformedParentId = null;
            Integer parentId = menuItemRepository.findByCode(packageMenu.getCategory()).getId();
            if (parentId == null) {
                String msg = String.format("Cannot find system menu item by package menu's category: [%s]",
                        packageMenu.getCategory());
                log.error(msg);
                throw new WecubeCoreException(msg);
            }
            transformedParentId = parentId.toString();
            Integer foundTopMenuId = categoryToId.get(transformedParentId) + 1;
            MenuItemDto packageMenuDto = MenuItemDto.fromPackageMenuItem(packageMenu, transformedParentId,
                    foundTopMenuId);
            categoryToId.put(transformedParentId, foundTopMenuId);
            returnMenuDto.add(packageMenuDto);
        }
        Collections.sort(returnMenuDto);
        return returnMenuDto;
    }

    public Set<SystemVariable> getSystemVarsById(Integer packageId) {
        PluginPackage packageFoundById = getPackageById(packageId);
        return packageFoundById.getSystemVariables();
    }

    public Set<PluginPackageAuthority> getAuthoritiesById(Integer packageId) {
        PluginPackage packageFoundById = getPackageById(packageId);
        return packageFoundById.getPluginPackageAuthorities();
    }

    public PluginPackageRuntimeResouceDto getResourcesById(Integer packageId) {
        PluginPackage packageFoundById = getPackageById(packageId);
        Set<PluginPackageRuntimeResourcesDocker> dockerSet = packageFoundById.getPluginPackageRuntimeResourcesDocker();
        Set<PluginPackageRuntimeResourcesMysql> mysqlSet = packageFoundById.getPluginPackageRuntimeResourcesMysql();
        Set<PluginPackageRuntimeResourcesS3> s3Set = packageFoundById.getPluginPackageRuntimeResourcesS3();
        return (new PluginPackageRuntimeResouceDto(dockerSet, mysqlSet, s3Set));
    }

    public Set<PluginConfig> getPluginsById(Integer packageId) {
        PluginPackage packageFoundById = getPackageById(packageId);
        return packageFoundById.getPluginConfigs();
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

    public Map<String, Integer> updateCategoryToIdMapping(List<MenuItemDto> inputMenuItemDto) {
        Map<String, Integer> categoryToId = new HashMap<>();
        for (int i = 1; i <= 8; i++) {
            categoryToId.put(Integer.toString(i), 0);
        }
        for (MenuItemDto menuItemDto : inputMenuItemDto) {
            String menuCategory = menuItemDto.getCategory();
            Integer menuId = menuItemDto.getId();
            if (!org.springframework.util.StringUtils.isEmpty(menuCategory)) {
                if (menuId > categoryToId.get(menuCategory)) {
                    categoryToId.put(menuCategory, menuId + 1);
                }
            }

        }
        return categoryToId;
    }

    private void deployPluginUiResources(PluginPackage pluginPackage) {
        // download UI package from MinIO
        String tmpFolderName = SystemUtils.getTempFolderPath()
                + new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
        String downloadUiZipPath = tmpFolderName + File.separator + pluginProperties.getUiFile();

        String s3UiPackagePath = pluginPackage.getName() + File.separator + pluginPackage.getVersion() + File.separator
                + pluginProperties.getUiFile();
        s3Client.downFile(pluginProperties.getPluginPackageBucketName(), s3UiPackagePath, downloadUiZipPath);

        String remotePath = pluginProperties.getStaticResourceServerPath() + File.separator + pluginPackage.getName()
                + File.separator + pluginPackage.getVersion() + File.separator;

        // mkdir at remote host
        if (!remotePath.equals("/") && !remotePath.equals(".")) {
            String mkdirCmd = String.format("rm -rf %s && mkdir -p %s", remotePath, remotePath);
            try {
                commandService.runAtRemote(pluginProperties.getStaticResourceServerIp(),
                        pluginProperties.getStaticResourceServerUser(),
                        pluginProperties.getStaticResourceServerPassword(),
                        pluginProperties.getStaticResourceServerPort(), mkdirCmd);
            } catch (Exception e) {
                log.error("Run command [mkdir] meet error: ", e.getMessage());
                throw new WecubeCoreException(String.format("Run remote command meet error: %s", e.getMessage()));
            }
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
        String unzipCmd = String.format("cd %s && unzip %s", remotePath, pluginProperties.getUiFile());
        try {
            commandService.runAtRemote(pluginProperties.getStaticResourceServerIp(),
                    pluginProperties.getStaticResourceServerUser(), pluginProperties.getStaticResourceServerPassword(),
                    pluginProperties.getStaticResourceServerPort(), unzipCmd);
        } catch (Exception e) {
            log.error("Run command [unzip] meet error: ", e.getMessage());
            throw new WecubeCoreException(String.format("Run remote command meet error: %s", e.getMessage()));
        }
        log.info("UI package deployment has done...");
    }

    private void removePluginUiResources(PluginPackage pluginPackage) {
        String remotePath = pluginProperties.getStaticResourceServerPath() + File.separator + pluginPackage.getName()
                + File.separator + pluginPackage.getVersion() + File.separator;

        if (!remotePath.equals("/") && !remotePath.equals(".")) {
            String mkdirCmd = String.format("rm -rf %s", remotePath);
            try {
                commandService.runAtRemote(pluginProperties.getStaticResourceServerIp(),
                        pluginProperties.getStaticResourceServerUser(),
                        pluginProperties.getStaticResourceServerPassword(),
                        pluginProperties.getStaticResourceServerPort(), mkdirCmd);
            } catch (Exception e) {
                log.error("Run command [rm] meet error: ", e.getMessage());
                throw new WecubeCoreException(String.format("Run command [rm] meet error: %s", e.getMessage()));
            }
        }
    }
}
