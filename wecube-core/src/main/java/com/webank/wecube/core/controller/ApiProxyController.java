package com.webank.wecube.core.controller;

import com.webank.wecube.core.commons.ApplicationProperties.ApiProxyProperties;
import com.webank.wecube.core.commons.WecubeCoreException;
import com.webank.wecube.core.controller.helper.ProxyExchange;
import com.webank.wecube.core.domain.plugin.PluginInstance;
import com.webank.wecube.core.service.PluginInstanceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.Principal;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
@Slf4j
public class ApiProxyController {
	private static final String API_PROXY_PATH = "/api-proxy";

	@Autowired
	private ApiProxyProperties apiProxyProperties;

	@Autowired
	private PluginInstanceService pluginInstanceService;

	@Autowired
	private RestTemplate restTemplate;

	@RequestMapping(value = API_PROXY_PATH + "/**", method = { GET, DELETE, OPTIONS, HEAD })
	public void pluginApiProxy(HttpServletRequest request, HttpServletResponse response) {
		proxy(createProxyExchange(request, response), request);
	}

	@RequestMapping(value = API_PROXY_PATH + "/**", method = { POST, PUT, PATCH })
	public void pluginApiProxy(HttpServletRequest request, HttpServletResponse response, @RequestBody Object body) {
		proxy(createProxyExchange(request, response).body(body), request);
	}

	private ProxyExchange createProxyExchange(HttpServletRequest request, HttpServletResponse response) {
		ProxyExchange proxyExchange = new ProxyExchange(restTemplate, request, response);
		proxyExchange.customHttpHeaders(apiProxyProperties.getCustomHeaders());
		proxyExchange.sensitiveHeaders(apiProxyProperties.getSensitiveHeaders());
		return proxyExchange;
	}

	private void proxy(ProxyExchange proxyExchange, HttpServletRequest request) {
		routing(proxyExchange, request);

		proxyExchange.exchange();
	}

	private void routing(ProxyExchange proxyExchange, HttpServletRequest request) {
		String path = proxyExchange.path(API_PROXY_PATH);
		String pluginName = resolvePluginName(path);
		String apiUrl = path.substring(pluginName.length() + 1);

		List<PluginInstance> runningPluginInstances = pluginInstanceService.getRunningPluginInstances(pluginName);
		PluginInstance runningPluginInstance = pluginInstanceService.chooseOne(runningPluginInstances);
		if (runningPluginInstance == null)
			throw new WecubeCoreException("No running plugin instance found for plugin name: " + pluginName);

		String targetUri = deriveTargetUrl(request.getScheme(), runningPluginInstance, apiUrl);

		authorize(request.getUserPrincipal(), pluginName);

		proxyExchange.targetUri(targetUri);
	}

	private String resolvePluginName(String path) {
		int beginIndex = 1;
		int endIndex = path.indexOf("/", 1);
		if (endIndex == -1 || (endIndex - beginIndex) < 1)
			throw new WecubeCoreException("Can not resolve Plugin Name due to invalid url path: " + path);
		String pluginName = path.substring(beginIndex, endIndex);
		if (isEmpty(pluginName))
			throw new WecubeCoreException("Can not resolve Plugin Name due to invalid url path: " + path);
		return pluginName;
	}

	private String deriveTargetUrl(String scheme, PluginInstance instance, String path) {
		String instanceAddress = pluginInstanceService.getInstanceAddress(instance);
		return scheme + "://" + instanceAddress + path;
	}

	private void authorize(Principal principal, String pluginName) {
		// TODO:
	}

}