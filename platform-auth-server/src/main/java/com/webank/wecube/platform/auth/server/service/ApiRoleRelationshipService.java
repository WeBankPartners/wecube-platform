package com.webank.wecube.platform.auth.server.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.webank.wecube.platform.auth.server.entity.ApiRoleRelationshipEntity;
import com.webank.wecube.platform.auth.server.entity.SysApiEntity;
import com.webank.wecube.platform.auth.server.entity.SysRoleEntity;
import com.webank.wecube.platform.auth.server.entity.SysUserEntity;
import com.webank.wecube.platform.auth.server.entity.UserRoleRelationshipEntity;
import com.webank.wecube.platform.auth.server.repository.ApiRepository;
import com.webank.wecube.platform.auth.server.repository.ApiRoleRelationshipRepository;
import com.webank.wecube.platform.auth.server.repository.UserRepository;
import com.webank.wecube.platform.auth.server.repository.UserRoleRelationshipRepository;

@Service("apiRoleRelationshipService")
public class ApiRoleRelationshipService {

	private static final Logger log = LoggerFactory.getLogger(ApiRoleRelationshipService.class);

	@Autowired
	private ApiRoleRelationshipRepository apiRoleRelationshipRepository;

	@Autowired
	private RoleService roleService;
	@Autowired
	private ApiService apiService;

	public List<SysApiEntity> getApisByRoleId(Long roleId) {
		List<SysApiEntity> apis = Lists.newArrayList();
		apiRoleRelationshipRepository.findByRoleId(roleId).forEach(apiRole -> {
			apis.add(apiRole.getApi());
		});
		return apis;
	}

	public List<SysRoleEntity> getRolesByApiId(Long apiId) {
		List<SysRoleEntity> roles = Lists.newArrayList();
		apiRoleRelationshipRepository.findByApiId(apiId).forEach(apiRole -> {
			roles.add(apiRole.getRole());
		});
		return roles;
	}

	public void grantRoleForApis(Long roleId, List<Long> apiIds) throws Exception {
		SysRoleEntity role = roleService.getRoleByIdIfExisted(roleId);
		for (Long apiId : apiIds) {
			SysApiEntity apiEntity = apiService.getApiByIdIfExisted(apiId);
			if (null == apiRoleRelationshipRepository.findOneByApiIdAndRoleId(apiId, roleId))
				apiRoleRelationshipRepository.save(new ApiRoleRelationshipEntity(apiEntity, role));
		}
	}

	public void revokeRoleForApis(Long roleId, List<Long> apiIds) throws Exception {
		roleService.getRoleByIdIfExisted(roleId);
		for (Long apiId : apiIds) {
			apiService.getApiByIdIfExisted(apiId);
			ApiRoleRelationshipEntity apiRoleRelationshipEntity = apiRoleRelationshipRepository
					.findOneByApiIdAndRoleId(apiId, roleId);
			if (null != apiRoleRelationshipEntity)
				apiRoleRelationshipRepository.delete(apiRoleRelationshipEntity);
		}
	}

}
