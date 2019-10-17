package com.webank.wecube.platform.core.service.plugin;

import com.webank.wecube.platform.core.commons.ApplicationProperties;
import com.webank.wecube.platform.core.commons.ApplicationProperties.PluginProperties;
import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.domain.plugin.PluginConfig;
import com.webank.wecube.platform.core.domain.plugin.PluginPackage;
import com.webank.wecube.platform.core.dto.PluginPackageDto;
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
import java.util.Date;
import java.util.Enumeration;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Service
@Transactional
public class PluginPackageService {
    public final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    PluginPackageRepository pluginPackageRepository;

    @Autowired
    private PluginProperties pluginProperties;

    @Autowired
    ApplicationProperties.S3Properties s3Properties;


    public PluginPackageDto uploadPackage(MultipartFile pluginPackageFile) throws Exception {
        String pluginPackageFileName = pluginPackageFile.getName();

        // 1. save package file to local
        String tmpFileName = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
        File  localFilePath  = new File(SystemUtils.getTempFolderPath() + tmpFileName + "/");
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
        if (!StringUtils.containsOnlyAlphanumericOrHyphen(pluginPackageDto.getName())) {
            throw new WecubeCoreException(String.format("Invalid plugin package name [%s] - Only alphanumeric and hyphen('-') is allowed. ", pluginPackageDto.getName()));
        }
        if (isPluginPackageExists(pluginPackageDto.getName(), pluginPackageDto.getVersion())) {
            throw new WecubeCoreException(String.format("Plugin package [name=%s, version=%s] exists.", pluginPackageDto.getName(), pluginPackageDto.getVersion()));
        }
        // 4.
        File pluginFile = new File(localFilePath + pluginPackageDto.getDockerImageFile());
        String keyName = pluginPackageDto.getName() + "/" + pluginPackageDto.getVersion() + "/" + pluginFile.getName();
        log.info("keyname : {}", keyName);
//        String url = uploadFileToMinIO(pluginProperties.getPluginPackageBucketName(), keyName, pluginFile);
//        log.info("Plugin Package has uploaded to MinIO {}", url);
//
        pluginPackageRepository.save(pluginPackageDto.getPluginPackage());

        return pluginPackageDto;
    }

    private boolean isPluginPackageExists(String name, String version) {
        return pluginPackageRepository.countByNameAndVersion(name, version) > 0;
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

    private String uploadFileToMinIO(String bucketName, String key, File file) {
        S3Client s3 = new S3Client(s3Properties.getEndpoint(), s3Properties.getAccessKey(), s3Properties.getSecretKey());
        return s3.uploadFile(bucketName, key, file);
    }
}
