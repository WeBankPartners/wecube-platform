package com.webank.wecube.platform.core.service.plugin;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestTemplate;

import com.webank.wecube.platform.core.commons.AuthenticationContextHolder;
import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.domain.plugin.PluginArtifactPullRequestEntity;
import com.webank.wecube.platform.core.domain.plugin.PluginPackage;
import com.webank.wecube.platform.core.dto.S3PluginActifactDto;
import com.webank.wecube.platform.core.dto.S3PluginActifactPullRequestDto;
import com.webank.wecube.platform.core.entity.plugin.PluginArtifactPullReq;
import com.webank.wecube.platform.core.repository.plugin.PluginArtifactPullReqMapper;
import com.webank.wecube.platform.core.service.plugin.PluginArtifactOperationExecutor.PluginArtifactPullContext;
import com.webank.wecube.platform.core.utils.SystemUtils;
import com.webank.wecube.platform.workflow.commons.LocalIdGenerator;

@Service
public class PluginArtifactsMgmtService extends AbstractPluginMgmtService{
    private static final Logger log = LoggerFactory.getLogger(PluginArtifactsMgmtService.class);
    
    public static final String SYS_VAR_PUBLIC_PLUGIN_ARTIFACTS_RELEASE_URL = "PLUGIN_ARTIFACTS_RELEASE_URL";
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Autowired
    private PluginArtifactOperationExecutor pluginArtifactOperationExecutor;
    
    @Autowired
    private PluginArtifactPullReqMapper pluginArtifactPullReqMapper;
    
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

        PluginPackage savedPluginPackage = parsePackageFile(dest, localFilePath);

        reqEntity.setUpdatedBy(DEFAULT_USER);
        reqEntity.setUpdatedTime(new Date());
        reqEntity.setTotalSize(downloadedFile.length());
        reqEntity.setPackageId(savedPluginPackage.getId());
        reqEntity.setState(PluginArtifactPullRequestEntity.STATE_COMPLETED);

        pluginArtifactPullRequestRepository.saveAndFlush(reqEntity);
    }

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
        entity.setState(PluginArtifactPullRequestEntity.STATE_IN_PROGRESS);
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
        PluginArtifactPullReq reqEntity = pluginArtifactPullReqMapper
                .selectByPrimaryKey(ctx.getRequestId());

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
