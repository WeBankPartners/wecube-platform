package com.webank.wecube.core.service;

import com.amazonaws.services.s3.model.*;
import com.webank.wecube.core.commons.ApplicationProperties;
import com.webank.wecube.core.commons.ApplicationProperties.PluginProperties;
import com.webank.wecube.core.commons.WecubeCoreException;
import com.webank.wecube.core.domain.plugin.PluginConfig;
import com.webank.wecube.core.domain.plugin.PluginPackage;
import com.webank.wecube.core.jpa.PluginPackageRepository;
import com.webank.wecube.core.parser.PluginConfigXmlParser;
import com.webank.wecube.core.service.plugin.PluginConfigCopyHelper;
import com.webank.wecube.core.support.s3.S3Client;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static com.webank.wecube.core.domain.plugin.PluginConfig.Status.ONLINE;
import static com.webank.wecube.core.utils.SystemUtils.getTempFolderPath;

@Service
@Slf4j
@Transactional
public class PluginPackageService {

    @Autowired
    PluginPackageRepository pluginPackageRepository;

    @Autowired
    private PluginProperties pluginProperties;
    @Autowired
    ApplicationProperties.S3Properties s3Properties;

    public PluginPackage uploadPackage(InputStream inputStream, String inputFileName) throws Exception {
        String tmpFileName = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
        String tmpFilePath = getTempFolderPath() + tmpFileName + "/";
        log.info("tmpFilePath= {}", tmpFilePath);
        if (!(new File(tmpFilePath).exists())) {
            if ((new File(tmpFilePath).mkdirs())) {
                log.info("Create directory [{}] successful", new File(tmpFilePath).getAbsolutePath());
            } else {
                throw new WecubeCoreException("Create directory [{}] failed");
            }
        }

        uploadFileToLocal(tmpFilePath, inputStream, inputFileName);
        unzipLocalFile(tmpFilePath + inputFileName, tmpFilePath);
        byte[] pluginConfigFile = FileUtils.readFileToByteArray(new File(tmpFilePath + pluginProperties.getRegisterFile()));

        PluginPackage pluginPackage = PluginConfigXmlParser.newInstance(new ByteArrayInputStream(pluginConfigFile)).parsePluginPackage();
        if (isPluginPackageExists(pluginPackage.getName(), pluginPackage.getVersion())) {
            throw new WecubeCoreException(String.format("Plugin package [name=%s, version=%s] exists.", pluginPackage.getName(), pluginPackage.getVersion()));
        }

        File pluginFile = new File(tmpFilePath + pluginPackage.getDockerImageFile());
        String keyName = pluginPackage.getName() + "/" + pluginPackage.getVersion() + "/" + pluginFile.getName();
        String url = uploadFileToMinIO(pluginProperties.getPluginPackageBucketName(), keyName, pluginFile);
        log.info("Plugin Package has uploaded to MinIO {}", url);

        return pluginPackageRepository.save(pluginPackage);
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
            if (ONLINE.equals(config.getStatus())) {
                String errorMessage = String.format("Failed to delete Plugin[%s/%s] due to [%s] is still in used. Please decommission it and try again.", pluginPackage.getName(), pluginPackage.getVersion(), config.getName());
                log.warn(errorMessage);
                throw new WecubeCoreException(errorMessage);
            }
        }
        pluginPackageRepository.deleteById(pluginPackageId);

        // Remove related docker image file
        String versionPath = getTempFolderPath() + pluginPackage.getName() + "-" + pluginPackage.getVersion() + "/";
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
        if (!localFile.createNewFile()) {
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
