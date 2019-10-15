package com.webank.wecube.platform.auth.server.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.webank.wecube.platform.auth.server.entity.SysRoleEntity;
import com.webank.wecube.platform.auth.server.entity.SysUserEntity;
import com.webank.wecube.platform.auth.server.entity.UserRoleRelationshipEntity;
import com.webank.wecube.platform.auth.server.repository.UserRepository;
import com.webank.wecube.platform.auth.server.repository.UserRoleRelationshipRepository;

@Service("userRoleRelationshipService")
public class UserRoleRelationshipService {

	private static final Logger log = LoggerFactory.getLogger(UserRoleRelationshipService.class);

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private UserRoleRelationshipRepository userRoleRelationshipRepository;

	@Autowired
	private RoleService roleService;
	@Autowired
	private UserService userService;

	public List<SysUserEntity> getUsersByRoleId(Long roleId) {
		List<SysUserEntity> users = Lists.newArrayList();
		userRoleRelationshipRepository.findByRoleId(roleId).forEach(userRole -> {
			users.add(userRole.getUser());
		});
		return users;
	}

	public List<SysRoleEntity> getRolesByUserName(String userName) {
		List<SysRoleEntity> roles = Lists.newArrayList();
		userRoleRelationshipRepository.findByUserId(userRepository.findOneByUsername(userName).getId())
				.forEach(userRole -> {
					roles.add(userRole.getRole());
				});
		return roles;
	}

	public void grantRoleForUsers(Long roleId, List<Long> userIds) throws Exception {
		SysRoleEntity role = roleService.getRoleByIdIfExisted(roleId);
		for (Long userId : userIds) {
			log.info("userId={}", userId);
			SysUserEntity userEntity = userService.getUserByIdIfExisted(userId);
			if (null == userRoleRelationshipRepository.findOneByUserIdAndRoleId(userId, roleId))
				userRoleRelationshipRepository.save(new UserRoleRelationshipEntity(userEntity, role));
		}
	}

	public void revokeRoleForUsers(Long roleId, List<Long> userIds) throws Exception {
		roleService.getRoleByIdIfExisted(roleId);
		for (Long userId : userIds) {
			userService.getUserByIdIfExisted(userId);
			UserRoleRelationshipEntity userRoleRelationshipEntity = userRoleRelationshipRepository
					.findOneByUserIdAndRoleId(userId, roleId);
			if (null != userRoleRelationshipEntity)
				userRoleRelationshipRepository.delete(userRoleRelationshipEntity);
		}
	}

}
