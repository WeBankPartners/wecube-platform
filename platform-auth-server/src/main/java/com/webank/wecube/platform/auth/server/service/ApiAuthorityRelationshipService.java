package com.webank.wecube.platform.auth.server.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.webank.wecube.platform.auth.server.entity.ApiAuthorityRelationshipEntity;
import com.webank.wecube.platform.auth.server.entity.SysApiEntity;
import com.webank.wecube.platform.auth.server.entity.SysAuthorityEntity;
import com.webank.wecube.platform.auth.server.repository.ApiAuthorityRelationshipRepository;

@Service("apiAuthorityRelationshipService")
public class ApiAuthorityRelationshipService {

	private static final Logger log = LoggerFactory.getLogger(ApiAuthorityRelationshipService.class);

	@Autowired
	private ApiAuthorityRelationshipRepository apiAuthorityRelationshipRepository;

	@Autowired
	private AuthorityService authorityService;
	@Autowired
	private ApiService apiService;

	public List<SysApiEntity> getApisByAuthorityId(Long authorityId) {
		List<SysApiEntity> apis = Lists.newArrayList();
		apiAuthorityRelationshipRepository.findByAuthorityId(authorityId).forEach(apiAuthority -> {
			apis.add(apiAuthority.getApi());
		});
		return apis;
	}

	public List<SysAuthorityEntity> getAuthoritysByApiId(Long apiId) {
		List<SysAuthorityEntity> authoritys = Lists.newArrayList();
		apiAuthorityRelationshipRepository.findByApiId(apiId).forEach(apiAuthority -> {
			authoritys.add(apiAuthority.getAuthority());
		});
		return authoritys;
	}

	public void grantAuthorityForApis(Long authorityId, List<Long> apiIds) throws Exception {
		SysAuthorityEntity authority = authorityService.getAuthorityByIdIfExisted(authorityId);
		for (Long apiId : apiIds) {
			SysApiEntity apiEntity = apiService.getApiByIdIfExisted(apiId);
			if (null == apiAuthorityRelationshipRepository.findOneByApiIdAndAuthorityId(apiId, authorityId))
				apiAuthorityRelationshipRepository.save(new ApiAuthorityRelationshipEntity(apiEntity, authority));
		}
	}

	public void revokeAuthorityForApis(Long authorityId, List<Long> apiIds) throws Exception {
		authorityService.getAuthorityByIdIfExisted(authorityId);
		for (Long apiId : apiIds) {
			apiService.getApiByIdIfExisted(apiId);
			ApiAuthorityRelationshipEntity apiAuthorityRelationshipEntity = apiAuthorityRelationshipRepository
					.findOneByApiIdAndAuthorityId(apiId, authorityId);
			if (null != apiAuthorityRelationshipEntity)
				apiAuthorityRelationshipRepository.delete(apiAuthorityRelationshipEntity);
		}
	}

}
