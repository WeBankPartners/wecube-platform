package com.webank.wecube.platform.core.service.plugin;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.webank.wecube.platform.core.commons.AuthenticationContextHolder;
import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.domain.plugin.PluginArtifactPullRequestEntity;
import com.webank.wecube.platform.core.dto.S3PluginActifactDto;
import com.webank.wecube.platform.core.dto.S3PluginActifactPullRequestDto;
import com.webank.wecube.platform.core.service.plugin.PluginArtifactOperationExecutor.PluginArtifactPullContext;

@Service
public class PluginArtifactsMgmtService extends AbstractPluginMgmtService{
    private static final Logger log = LoggerFactory.getLogger(PluginArtifactsMgmtService.class);
    
    public static final String SYS_VAR_PUBLIC_PLUGIN_ARTIFACTS_RELEASE_URL = "PLUGIN_ARTIFACTS_RELEASE_URL";
    
    @Autowired
    private RestTemplate restTemplate;

    public List<S3PluginActifactDto> listS3PluginActifacts() {
        String releaseFileUrl = getGlobalSystemVariableByName(SYS_VAR_PUBLIC_PLUGIN_ARTIFACTS_RELEASE_URL);

        if (org.apache.commons.lang3.StringUtils.isBlank(releaseFileUrl)) {
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
        ctx.setEntity(savedEntity);

        pluginArtifactOperationExecutor.pullPluginArtifact(ctx);

        return buildS3PluginActifactPullRequestDto(savedEntity);
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
    
    
}
