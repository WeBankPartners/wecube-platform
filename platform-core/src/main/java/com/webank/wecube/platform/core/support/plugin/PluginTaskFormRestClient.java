package com.webank.wecube.platform.core.support.plugin;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.webank.wecube.platform.core.commons.ApplicationProperties;
import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.support.RestClient;
import com.webank.wecube.platform.core.support.plugin.dto.TaskFormMetaDto;
import com.webank.wecube.platform.core.support.plugin.dto.TaskFormMetaResponseDto;

@Service("pluginTaskFormRestClient")
public class PluginTaskFormRestClient implements RestClient {

    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    public static final String TASK_FORM_META_PATH_SUFFIX = "meta";

    @Autowired
    @Qualifier(value = "jwtSsoRestTemplate")
    private RestTemplate jwtSsoRestTemplate;

    @Autowired
    private ApplicationProperties applicationProperties;

    public TaskFormMetaDto getRemoteTaskFormMeta(String intfDefPath, Map<String, Object> params) {
        String url = buildTaskFormUrl(intfDefPath);
        String urlWithQueries = appendQueries(url, params);

        log.info("try to get task form meta with URL:{}", urlWithQueries);

        TaskFormMetaResponseDto responseDto = jwtSsoRestTemplate.getForObject(urlWithQueries,
                TaskFormMetaResponseDto.class);

        if (TaskFormMetaResponseDto.STATUS_OK.equals(responseDto.getStatus())) {
            return responseDto.getData();
        }

        throw new WecubeCoreException("Failed to get task form metadata for " + intfDefPath);
    }

    protected String appendQueries(String url, Map<String, Object> params) {
        if (params == null || params.isEmpty()) {
            return url;
        }

        StringBuilder sb = new StringBuilder();
        boolean isFirst = true;
        for (Map.Entry<String, Object> param : params.entrySet()) {
            String paramPair = String.format("%s=%s", param.getKey(), String.valueOf(param.getValue()));
            if (isFirst) {
                sb.append(paramPair);
                isFirst = false;
            } else {
                sb.append("&").append(paramPair);
            }
        }

        return url + "?" + sb.toString();
    }

    protected String buildTaskFormUrl(String path) {
        String fullPath = buildTaskFormPath(path);
        String hostAndPort = applicationProperties.getGatewayUrl();
        if (hostAndPort.endsWith("/")) {
            hostAndPort = hostAndPort.substring(0, hostAndPort.length() - 1);
        }

        if (fullPath.startsWith("/")) {
            fullPath = fullPath.substring(1);
        }
        
        String url = String.format("http://%s/%s", hostAndPort, fullPath);

        return url;
    }

    protected String buildTaskFormPath(String path) {
        if (path.endsWith("/")) {
            return path + TASK_FORM_META_PATH_SUFFIX;
        } else {
            return path + "/" + TASK_FORM_META_PATH_SUFFIX;
        }
    }

}
