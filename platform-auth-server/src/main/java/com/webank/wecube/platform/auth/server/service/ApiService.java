package com.webank.wecube.platform.auth.server.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.webank.wecube.platform.auth.server.dto.CreateApiDto;
import com.webank.wecube.platform.auth.server.dto.CreateUserDto;
import com.webank.wecube.platform.auth.server.entity.SysApiEntity;
import com.webank.wecube.platform.auth.server.entity.SysUserEntity;
import com.webank.wecube.platform.auth.server.repository.ApiRepository;
import com.webank.wecube.platform.auth.server.repository.UserRepository;

@Service("apiService")
public class ApiService {

	private static final Logger log = LoggerFactory.getLogger(ApiService.class);

	@Autowired
	private ApiRepository apiRepository;

	@Autowired
	PasswordEncoder passwordEncoder;

	public SysApiEntity create(CreateApiDto createApiDto) throws Exception {

		SysApiEntity existedApir = apiRepository.findOneByHttpMethodAndApiUrl(createApiDto.getHttpMethod(),
				createApiDto.getApiUrl());

		log.info("existedApir = {}", existedApir);
		if (!(null == existedApir))
			throw new Exception(String.format("Api [HttpMethod=%s, Url=%s] already existed",
					existedApir.getHttpMethod(), existedApir.getApiUrl()));

		SysApiEntity api = new SysApiEntity(createApiDto.getName(), createApiDto.getApiUrl(),
				createApiDto.getHttpMethod(), createApiDto.getSystemId(), createApiDto.getSystemName(),
				createApiDto.getSystemCode());
		apiRepository.saveAndFlush(api);

		return api;
	}

	public List<SysApiEntity> retrieve() {
		return apiRepository.findAll();
	}

	public void delete(Long id) {
		apiRepository.deleteById(id);
	}
}
