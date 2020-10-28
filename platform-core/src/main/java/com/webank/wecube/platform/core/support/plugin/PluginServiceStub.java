package com.webank.wecube.platform.core.support.plugin;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.webank.wecube.platform.core.support.plugin.dto.PluginRequest;
import com.webank.wecube.platform.core.support.plugin.dto.PluginRequest.DefaultPluginRequest;
import com.webank.wecube.platform.core.support.plugin.dto.PluginResponse;
import com.webank.wecube.platform.core.support.plugin.dto.PluginResponse.DefaultPluginResponse;
import com.webank.wecube.platform.core.support.plugin.dto.PluginResponse.PluginRunScriptResponse;
import com.webank.wecube.platform.core.support.plugin.dto.PluginResponse.ResultData;
import com.webank.wecube.platform.core.support.plugin.dto.PluginRunScriptOutput;

@Service
public class PluginServiceStub {

    private static final Logger log = LoggerFactory.getLogger(PluginServiceStub.class);

    @Autowired
    @Qualifier("userJwtSsoTokenRestTemplate")
    private RestTemplate userJwtSsoTokenRestTemplate;

    private static final String INF_RELEASED_PACKAGE_LIST_DIR = "/v1/deploy/released-package/listCurrentDir";
    private static final String INF_RELEASED_PACKAGE_PROPERTY_KEY = "/v1/deploy/released-package/getConfigFileKey";
    private static final String INF_RUN_SCRIPT_PATH = "/v1/deploy/script/run";
    private static final String INF_SEARCH_TEXT_PATH = "/v1/deploy/text-processor/search";
    private static final String INF_GET_TEXT_CONTEXT_PATH = "/v1/deploy/text-processor/getContext";

    public ResultData<Object> getPluginReleasedPackageFilesByCurrentDir(String instanceAddress,
            PluginRequest<Map<String, Object>> request) {
        return callPluginInterface(asPluginServerUrl(instanceAddress, INF_RELEASED_PACKAGE_LIST_DIR), request);
    }

    public ResultData<Object> searchText(String instanceAddress, PluginRequest<Map<String, Object>> request) {
        return callPluginInterface(asPluginServerUrl(instanceAddress, INF_SEARCH_TEXT_PATH), request);
    }

    public ResultData<Object> getTextContext(String instanceAddress, PluginRequest<Map<String, Object>> request) {
        return callPluginInterface(asPluginServerUrl(instanceAddress, INF_GET_TEXT_CONTEXT_PATH), request);
    }

    public ResultData<Object> getPluginReleasedPackagePropertyKeysByFilePath(String instanceAddress,
            PluginRequest<Map<String, Object>> request) {
        return callPluginInterface(asPluginServerUrl(instanceAddress, INF_RELEASED_PACKAGE_PROPERTY_KEY), request);
    }

    public ResultData<PluginRunScriptOutput> callPluginRunScript(String instanceAddress,
            PluginRequest<Map<String, Object>> request) {
        String targetUrl = asPluginServerUrl(instanceAddress, INF_RUN_SCRIPT_PATH);
        log.info(targetUrl);
        PluginResponse<PluginRunScriptOutput> response = userJwtSsoTokenRestTemplate.postForObject(targetUrl, request,
                PluginRunScriptResponse.class);
        validatePluginResponse(response, false);
        return response.getResultData();
    }
    
    public ResultData<Object> callPluginInterface(String instanceAddress, String path,
            List<Map<String, Object>> parameters, String requestId) {
        return callPluginInterface(asPluginServerUrl(instanceAddress, path),
                new DefaultPluginRequest().withInputs(parameters).withRequestId(requestId));
    }

    private ResultData<Object> callPluginInterface(String targetUrl, PluginRequest<?> parameters) {
        log.info("About to call {} with parameters: {} ", targetUrl, parameters);
        PluginResponse<Object> response = userJwtSsoTokenRestTemplate.postForObject(targetUrl, parameters,
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
