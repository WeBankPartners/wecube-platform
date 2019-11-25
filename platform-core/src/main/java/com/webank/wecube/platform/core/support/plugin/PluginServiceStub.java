package com.webank.wecube.platform.core.support.plugin;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.webank.wecube.platform.core.support.plugin.dto.PluginRequest;
import com.webank.wecube.platform.core.support.plugin.dto.PluginRequest.DefaultPluginRequest;
import com.webank.wecube.platform.core.support.plugin.dto.PluginResponse;
import com.webank.wecube.platform.core.support.plugin.dto.PluginResponse.DefaultPluginResponse;
import com.webank.wecube.platform.core.support.plugin.dto.PluginResponse.ResultData;

@Service
public class PluginServiceStub {

    private static final Logger log = LoggerFactory.getLogger(PluginServiceStub.class);

    @Autowired
    RestTemplate restTemplate;

    public ResultData<Object> callPluginInterface(String instanceAddress, String path,
            List<Map<String, Object>> parameters) {
        return callPluginInterface(asPluginServerUrl(instanceAddress, path),
                new DefaultPluginRequest().withInputs(parameters));
    }

    private ResultData<Object> callPluginInterface(String targetUrl, PluginRequest<?> parameters) {
        log.info("About to call {} with parameters: {} ", targetUrl, parameters);
        PluginResponse<Object> response = restTemplate.postForObject(targetUrl, parameters,
                DefaultPluginResponse.class);
        log.info("Plugin response: {} ", response);
        validatePluginResponse(response, false);

        return response.getResultData();
    }

    private void validatePluginResponse(PluginResponse<?> pluginResponse, boolean dataRequired) {
        if (pluginResponse == null) {
            throw new PluginRemoteCallException("Plugin call failure due to no response.");
        }
        if (!PluginResponse.RESULT_CODE_OK.equalsIgnoreCase(pluginResponse.getResultCode())) {
            throw new PluginRemoteCallException("Plugin call error: " + pluginResponse.getResultMessage(),
                    pluginResponse);
        }
        if (dataRequired && pluginResponse.getOutputs() == null) {
            throw new PluginRemoteCallException("Plugin call failure due to unexpected empty response.",
                    pluginResponse);
        }
    }

    private String asPluginServerUrl(String instanceAddress, String originPath, Object... pathVariables) {
        String solvedPath = originPath;
        if (pathVariables != null && pathVariables.length > 0) {
            solvedPath = String.format(originPath, pathVariables);
        }
        return "http://" + instanceAddress + solvedPath;
    }
}
