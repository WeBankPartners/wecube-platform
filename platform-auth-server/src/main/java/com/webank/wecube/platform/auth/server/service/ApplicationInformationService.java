package com.webank.wecube.platform.auth.server.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.webank.wecube.platform.auth.server.repository.SubSystemRepository;

@Service
public class ApplicationInformationService {
	private static final Logger log = LoggerFactory.getLogger(ApplicationInformationService.class);

	@Autowired
	private SubSystemRepository subSystemRepository;

	public void dbHealthCheck() {
		try {
			subSystemRepository.findOneBySystemCode("UNEXIST_SYSTEM_CODE");
		} catch (Exception e) {
			log.warn("DB health checking failed", e);
			throw new RuntimeException("Database is NOT available");
		}
	}
}
