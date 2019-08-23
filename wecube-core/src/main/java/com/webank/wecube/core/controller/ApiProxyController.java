package com.webank.wecube.core.controller;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.HEAD;
import static org.springframework.web.bind.annotation.RequestMethod.OPTIONS;
import static org.springframework.web.bind.annotation.RequestMethod.PATCH;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

import java.security.Principal;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.webank.wecube.core.commons.ApplicationProperties.ApiProxyProperties;
import com.webank.wecube.core.commons.WecubeCoreException;
import com.webank.wecube.core.controller.helper.ProxyExchange;
import com.webank.wecube.core.domain.plugin.PluginInstance;
import com.webank.wecube.core.service.PluginInstanceService;

import lombok.extern.slf4j.Slf4j;

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
		log.info("http {} request comes: {}", request.getMethod(), proxyExchange.path());
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
		
		log.info("routing to : " + targetUri);

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

	// TODO:
	private void authorize(Principal principal, String pluginName) {
		log.info("//TODO: no authorization rule implemented, will be treated as ALL PERMITTED.");
	}

}