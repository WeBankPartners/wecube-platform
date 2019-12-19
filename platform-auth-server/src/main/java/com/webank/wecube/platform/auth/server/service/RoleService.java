package com.webank.wecube.platform.auth.server.service;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.webank.wecube.platform.auth.server.dto.CreateRoleDto;
import com.webank.wecube.platform.auth.server.dto.CreateUserDto;
import com.webank.wecube.platform.auth.server.entity.SysRoleEntity;
import com.webank.wecube.platform.auth.server.entity.SysUserEntity;
import com.webank.wecube.platform.auth.server.repository.RoleRepository;
import com.webank.wecube.platform.auth.server.repository.UserRepository;
import com.webank.wecube.platform.auth.server.service.impl.SubSystemInfoDataServiceImpl;

@Service("roleService")
public class RoleService {

	private static final Logger log = LoggerFactory.getLogger(RoleService.class);

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	PasswordEncoder passwordEncoder;

	public SysRoleEntity create(CreateRoleDto createRoleDto) throws Exception {

		SysRoleEntity existedRole = roleRepository.findOneByName(createRoleDto.getName());

		log.info("existUser = {}", existedRole);
		if (!(null == existedRole))
			throw new Exception(String.format("Role [%s] already existed", createRoleDto.getName()));

		SysRoleEntity role = new SysRoleEntity(createRoleDto.getName(), createRoleDto.getDisplayName());
		roleRepository.saveAndFlush(role);

		return role;
	}

	public List<SysRoleEntity> retrieve() {
		return roleRepository.findAll();
	}

	public void delete(Long id) {
		roleRepository.deleteById(id);
	}

	public SysRoleEntity getRoleByIdIfExisted(Long roleId) throws Exception {
		Optional<SysRoleEntity> roleEntityOptional = roleRepository.findById(roleId);
		if (!roleEntityOptional.isPresent())
			throw new Exception(String.format("Role ID [%d] does not exist", roleId));
		return roleEntityOptional.get();
	}
}
