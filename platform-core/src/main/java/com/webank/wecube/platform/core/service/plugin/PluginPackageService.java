package com.webank.wecube.platform.core.service.plugin;

import com.google.common.collect.Iterables;
import com.webank.wecube.platform.core.commons.ApplicationProperties.PluginProperties;
import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.domain.MenuItem;
import com.webank.wecube.platform.core.domain.SystemVariable;
import com.webank.wecube.platform.core.domain.plugin.*;
import com.webank.wecube.platform.core.domain.plugin.PluginConfig;
import com.webank.wecube.platform.core.dto.PluginPackageDependencyDto;
import com.webank.wecube.platform.core.dto.PluginPackageDto;
import com.webank.wecube.platform.core.dto.PluginPackageMenuDto;
import com.webank.wecube.platform.core.dto.PluginPackageRuntimeResouceDto;
import com.webank.wecube.platform.core.jpa.MenuItemRepository;
import com.webank.wecube.platform.core.jpa.PluginPackageDependencyRepository;
import com.webank.wecube.platform.core.jpa.PluginPackageEntityRepository;
import com.webank.wecube.platform.core.jpa.PluginPackageRepository;
import com.webank.wecube.platform.core.parser.PluginPackageXmlParser;
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

@Service
@Transactional
public class PluginPackageService {
    public final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    PluginPackageRepository pluginPackageRepository;

    @Autowired
    PluginPackageEntityRepository pluginPackageEntityRepository;

    @Autowired
    PluginPackageDependencyRepository pluginPackageDependencyRepository;

    @Autowired
    MenuItemRepository menuItemRepository;

    @Autowired
    private PluginProperties pluginProperties;

    @Autowired
    private S3Client s3Client;

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
        log.info("new file location: {}, filename: {}, canonicalpath: {}, canonicalfilename: {}", dest.getAbsoluteFile(), dest.getName(), dest.getCanonicalPath(), dest.getCanonicalFile().getName());
        pluginPackageFile.transferTo(dest);

        // 2. unzip local package file
        unzipLocalFile(dest.getCanonicalPath(), localFilePath.getCanonicalPath() + "/");

        // 3. read xml file in plugin package
        byte[] pluginConfigFile = FileUtils.readFileToByteArray(new File(localFilePath.getCanonicalPath() + "/" + pluginProperties.getRegisterFile()));
        PluginPackageDto pluginPackageDto = PluginPackageXmlParser.newInstance(new ByteArrayInputStream(pluginConfigFile)).parsePluginPackage();
        PluginPackage pluginPackage = pluginPackageDto.getPluginPackage();
        if (!StringUtils.containsOnlyAlphanumericOrHyphen(pluginPackage.getName())) {
            throw new WecubeCoreException(String.format("Invalid plugin package name [%s] - Only alphanumeric and hyphen('-') is allowed. ", pluginPackageDto.getName()));
        }
        if (isPluginPackageExists(pluginPackage.getName(), pluginPackage.getVersion())) {
            throw new WecubeCoreException(String.format("Plugin package [name=%s, version=%s] exists.", pluginPackageDto.getName(), pluginPackageDto.getVersion()));
        }
        // 4.
        File pluginDockerImageFile = new File(localFilePath + "/" + pluginPackage.getDockerImageFilename());
        log.info("pluginDockerImageFile: {}", pluginDockerImageFile.getAbsolutePath());
        String dockerImageUrl = "";
        if (pluginDockerImageFile.exists()) {
            String keyName = pluginPackageDto.getName() + "/" + pluginPackageDto.getVersion() + "/" + pluginDockerImageFile.getName();
            log.info("keyname : {}", keyName);
            dockerImageUrl = s3Client.uploadFile(pluginProperties.getPluginPackageBucketName(), keyName, pluginDockerImageFile);
            log.info("Plugin Package has uploaded to MinIO {}", dockerImageUrl);
            pluginPackage.setPluginPackageImageUrl(dockerImageUrl);
        }

        File pluginUiPackageFile = new File(localFilePath + "/" + pluginPackage.getUiPackageFilename());
        log.info("pluginDockerImageFile: {}", pluginUiPackageFile.getAbsolutePath());
        String uiPackageUrl = "";
        if (pluginUiPackageFile.exists()) {
            String keyName = pluginPackageDto.getName() + "/" + pluginPackageDto.getVersion() + "/" + pluginUiPackageFile.getName();
            log.info("keyname : {}", keyName);
            uiPackageUrl = s3Client.uploadFile(pluginProperties.getPluginPackageBucketName(), keyName, pluginUiPackageFile);
            log.info("UI static package file has uploaded to MinIO {}", dockerImageUrl);
            pluginPackage.setUiPackageUrl(uiPackageUrl);
        }

        PluginPackage savedPluginPackage = pluginPackageRepository.save(pluginPackageDto.getPluginPackage());
        Iterable<PluginPackageEntity> pluginPackageEntities = pluginPackageEntityRepository.saveAll(pluginPackageDto.getPluginPackageEntities().stream().map(it -> it.toDomain(savedPluginPackage)).collect(Collectors.toSet()));
        savedPluginPackage.setPluginPackageEntities(newLinkedHashSet(pluginPackageEntities));

        return savedPluginPackage;
    }

    private boolean isPluginPackageExists(String name, String version) {
        return pluginPackageRepository.countByNameAndVersion(name, version) > 0;
    }

    public Iterable<PluginPackage> getPluginPackages() {
        return pluginPackageRepository.findAll();
    }

    public void preconfigurePluginPackage(int pluginPackageId) {
        Optional<PluginPackage> pluginPackageOptional = pluginPackageRepository.findById(pluginPackageId);
        if (!pluginPackageOptional.isPresent())
            throw new WecubeCoreException("Plugin package not found, id=" + pluginPackageId);
        PluginPackage pluginPackage = pluginPackageOptional.get();
        Optional<PluginPackage> latestVersionPluginPackage = pluginPackageRepository.findLatestVersionByName(pluginPackage.getName(), pluginPackage.getVersion());
        if (latestVersionPluginPackage.isPresent()) {
            new PluginConfigCopyHelper().copyPluginConfigs(latestVersionPluginPackage.get(), pluginPackage);
            pluginPackageRepository.save(pluginPackage);
        } else {
            log.info("Latest plugin package not found. Ignored.");
        }
    }

    public void deletePluginPackage(int pluginPackageId) {
        Optional<PluginPackage> pluginPackageOptional = pluginPackageRepository.findById(pluginPackageId);
        if (!pluginPackageOptional.isPresent())
            throw new WecubeCoreException("Plugin package id not found, id = " + pluginPackageId);
        PluginPackage pluginPackage = pluginPackageOptional.get();
        for (PluginConfig config : pluginPackage.getPluginConfigs()) {
            if (PluginConfig.Status.ONLINE.equals(config.getStatus())) {
                String errorMessage = String.format("Failed to delete Plugin[%s/%s] due to [%s] is still in used. Please decommission it and try again.", pluginPackage.getName(), pluginPackage.getVersion(), config.getName());
                log.warn(errorMessage);
                throw new WecubeCoreException(errorMessage);
            }
        }
        pluginPackageRepository.deleteById(pluginPackageId);

        // Remove related docker image file
        String versionPath = SystemUtils.getTempFolderPath() + pluginPackage.getName() + "-" + pluginPackage.getVersion() + "/";
        File versionDirectory = new File(versionPath);
        try {
            log.info("Delete directory: {}", versionPath);
            FileUtils.deleteDirectory(versionDirectory);
        } catch (IOException e) {
            log.error("Remove plugin package file failed: {}", e);
            throw new WecubeCoreException("Remove plugin package file failed.");
        }
    }

    private void uploadFileToLocal(String path, InputStream inputStream, String inputFileName) throws WecubeCoreException, IOException {
        String fileName = path + inputFileName;
        File localFile = new File(fileName);
        if (!localFile.exists() && !localFile.createNewFile()) {
            throw new WecubeCoreException(String.format("File[%s] already exists", fileName));
        }

        try (BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(localFile))) {
            int length = 0;
            byte[] buffer = new byte[1024];
            while ((length = inputStream.read(buffer)) != -1) {
                stream.write(buffer, 0, length);
            }
            stream.flush();
            log.info("File save to temporary directory: " + localFile.getAbsolutePath());
        } catch (IOException e) {
            throw new WecubeCoreException("uploadFileToLocale meet IOException: ", e);
        }
    }

    private void unzipLocalFile(String sourceZipFile, String destFilePath) throws Exception {
        try (ZipFile zipFile = new ZipFile(sourceZipFile)) {
            Enumeration entries = zipFile.entries();

            for (; entries.hasMoreElements(); ) {
                ZipEntry entry = (ZipEntry) entries.nextElement();
                String zipEntryName = entry.getName();
                if (new File(destFilePath + zipEntryName).createNewFile()) {
                    log.info("Create new temporary file: {}", destFilePath + zipEntryName);
                }
                if (entry.isDirectory() || !(zipEntryName.contains(".xml") || zipEntryName.contains(".tar"))) {
                    continue;
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

    public PluginPackageDependencyDto getDependenciesById(Integer packageId) throws WecubeCoreException {
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

    public List<PluginPackageMenuDto> getMenusById(Integer packageId) throws WecubeCoreException {
        List<PluginPackageMenuDto> returnMenuDto = new ArrayList<>();

        // handling core's menus
        Iterable<MenuItem> systemMenus = menuItemRepository.findAll();

        for (MenuItem systemMenu : systemMenus) {
            PluginPackageMenuDto systemMenuDto = PluginPackageMenuDto.fromCoreMenuItem(systemMenu);
            returnMenuDto.add(systemMenuDto);
        }

        // handling package's menus
        PluginPackage packageFoundById = getPackageById(packageId);
        Set<PluginPackageMenu> packageMenus = packageFoundById.getPluginPackageMenus();

        for (PluginPackageMenu packageMenu : packageMenus) {
            PluginPackageMenuDto packageMenuDto = PluginPackageMenuDto.fromPackageMenuItem(packageMenu);
            returnMenuDto.add(packageMenuDto);
        }

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

    private void updateDependencyDto(PluginPackageDependency pluginPackageDependency, PluginPackageDependencyDto pluginPackageDependencyDto) {
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
}
