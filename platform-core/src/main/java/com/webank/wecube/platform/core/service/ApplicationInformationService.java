package com.webank.wecube.platform.core.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.webank.wecube.platform.core.support.authserver.AuthServerRestClient;

@Service
public class ApplicationInformationService {
	
	private static final Logger log = LoggerFactory.getLogger(ApplicationInformationService.class);
	
	@Autowired
	private PluginInstanceService pluginInstanceService;
	
	@Autowired
	private AuthServerRestClient authServerRestClient;

	public void healthCheck() {
		log.info("health checking...");
		List<String> hosts = pluginInstanceService.getAvailableContainerHosts();
		log.info("Health Check - HOSTS:{}", hosts);
		
		authServerRestClient.healthCheck();
	}
}
