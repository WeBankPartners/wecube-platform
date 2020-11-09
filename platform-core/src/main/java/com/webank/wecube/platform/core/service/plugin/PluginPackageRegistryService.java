package com.webank.wecube.platform.core.service.plugin;

import static com.google.common.collect.Sets.newLinkedHashSet;

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
import java.util.Optional;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

import com.google.common.collect.Sets;
import com.webank.wecube.platform.core.commons.ApplicationProperties.PluginProperties;
import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.domain.plugin.PluginConfig;
import com.webank.wecube.platform.core.domain.plugin.PluginPackage;
import com.webank.wecube.platform.core.domain.plugin.PluginPackageResourceFile;
import com.webank.wecube.platform.core.dto.PluginPackageDataModelDto;
import com.webank.wecube.platform.core.dto.PluginPackageDto;
import com.webank.wecube.platform.core.dto.plugin.UploadPackageResultDto;
import com.webank.wecube.platform.core.entity.plugin.PluginPackageResourceFiles;
import com.webank.wecube.platform.core.entity.plugin.PluginPackages;
import com.webank.wecube.platform.core.parser.PluginConfigXmlValidator;
import com.webank.wecube.platform.core.parser.PluginPackageDataModelValidator;
import com.webank.wecube.platform.core.parser.PluginPackageValidator;
import com.webank.wecube.platform.core.repository.plugin.PluginPackagesMapper;
import com.webank.wecube.platform.core.service.CommandService;
import com.webank.wecube.platform.core.service.ScpService;
import com.webank.wecube.platform.core.service.plugin.xml.register.PackageType;
import com.webank.wecube.platform.core.support.S3Client;
import com.webank.wecube.platform.core.utils.JaxbUtils;
import com.webank.wecube.platform.core.utils.SystemUtils;
import com.webank.wecube.platform.workflow.commons.LocalIdGenerator;

public class PluginPackageRegistryService {
    private static final Logger log = LoggerFactory.getLogger(PluginPackageRegistryService.class);

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

        UploadPackageResultDto result = parsePackageFile(dest, localFilePath);

        return result;
    }

    private UploadPackageResultDto parsePackageFile(File dest, File localFilePath) {
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
        try{
            registerXmlFileFis = new FileInputStream(registerXmlFile);
            registerXmlDataAsStr = IOUtils.toString(registerXmlFileFis, Charset.forName("utf-8"));
        }finally {
            closeSilently(registerXmlFileFis);
        }
        
        PackageType xmlPackage = JaxbUtils.convertToObject(registerXmlDataAsStr, PackageType.class);

        pluginPackageValidator.validatePackage(xmlPackage);
        dataModelValidator.validateDataModel(xmlPackage.getDataModel());

        if (isPluginPackageExists(xmlPackage.getName(), xmlPackage.getVersion())) {
            throw new WecubeCoreException("3115", String.format("Plugin package [name=%s, version=%s] exists.",
                    xmlPackage.getName(), xmlPackage.getVersion()), xmlPackage.getName(),
                    xmlPackage.getVersion());
        }

        processPluginDockerImageFile(localFilePath, xmlPackage);
        
        PluginPackages pluginPackageEntity = new PluginPackages();
        pluginPackageEntity.setId(LocalIdGenerator.generateId());
        pluginPackageEntity.setName(xmlPackage.getName());
        pluginPackageEntity.setVersion(xmlPackage.getVersion());

         List<PluginPackageResourceFiles> pluginPackageResourceFilesEntities = processPluginUiPackageFile(
                localFilePath, xmlPackage, pluginPackageEntity);
         
        processPluginInitSqlFile(localFilePath, pluginPackageDto);
        processPluginUpgradeSqlFile(localFilePath, pluginPackageDto);

        PluginPackage savedPluginPackage = pluginPackageRepository.save(pluginPackage);

        log.info("start to process role binds");
        Set<PluginConfig> pluginConfigs = savedPluginPackage.getPluginConfigs();
        for (PluginConfig plgCfg : pluginConfigs) {
            log.info("process plgCfg id={} , name={} , regName={}", plgCfg.getId(), plgCfg.getName(),
                    plgCfg.getRegisterName());

            saveRoleBinds(plgCfg);
        }

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
    
    private List<PluginPackageResourceFiles> processPluginUiPackageFile(File localFilePath,
            PackageType xmlPackage, PluginPackages pluginPackageEntity) throws Exception {
        File pluginUiPackageFile = new File(localFilePath + "/" + pluginProperties.getUiFile());
        log.info("pluginUiPackageFile: {}", pluginUiPackageFile.getAbsolutePath());
        String uiPackageUrl = "";
        List<PluginPackageResourceFiles> pluginPackageResourceFilesEntities = new ArrayList<>();
        if (pluginUiPackageFile.exists()) {

            String keyName = xmlPackage.getName() + "/" + xmlPackage.getVersion() + "/"
                    + pluginUiPackageFile.getName();
            log.info("keyName : {}", keyName);

            pluginPackageResourceFilesEntities = getAllPluginPackageResourceFile(pluginPackageEntity,
                    pluginUiPackageFile.getAbsolutePath(), pluginUiPackageFile.getName());
            uiPackageUrl = s3Client.uploadFile(pluginProperties.getPluginPackageBucketName(), keyName,
                    pluginUiPackageFile);
            pluginPackageEntity.setUiPackageIncluded(true);
            log.info("UI static package file has uploaded to MinIO {}", uiPackageUrl.split("\\?")[0]);
        }

        return pluginPackageResourceFilesEntities;
    }
    
    private List<PluginPackageResourceFiles> getAllPluginPackageResourceFile(PluginPackages pluginPackageEntity,
            String sourceZipFile, String sourceZipFileName) throws Exception {
        Optional<Set<PluginPackageResourceFile>> pluginPackageResourceFilesOptional = Optional.empty();
        try (ZipFile zipFile = new ZipFile(sourceZipFile)) {
            Enumeration<?> entries = zipFile.entries();
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
