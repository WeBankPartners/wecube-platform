package com.webank.wecube.platform.auth.server.service;

import com.webank.wecube.platform.auth.server.common.util.ValidateUtils;
import com.webank.wecube.platform.auth.server.dto.CreateRoleDto;
import com.webank.wecube.platform.auth.server.entity.SysRoleEntity;
import com.webank.wecube.platform.auth.server.repository.RoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service("roleService")
public class RoleService {

    private static final Logger log = LoggerFactory.getLogger(RoleService.class);
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    private RoleRepository roleRepository;

    public SysRoleEntity create(CreateRoleDto createRoleDto) throws Exception {
        if (!ValidateUtils.isEmailValid(createRoleDto.getEmail())) {
            throw new Exception("Please input the correct E-mail address");
        }

        SysRoleEntity existedRole = roleRepository.findOneByName(createRoleDto.getName());

        log.info("existUser = {}", existedRole);
        if (!(null == existedRole)) {
            throw new Exception(String.format("Role [%s] already existed", createRoleDto.getName()));
        }

        SysRoleEntity role = new SysRoleEntity(createRoleDto.getName(), createRoleDto.getDisplayName(), createRoleDto.getEmail());
        roleRepository.saveAndFlush(role);

        return role;
    }

    public List<SysRoleEntity> retrieve() {
        return roleRepository.findAll();
    }

    public void delete(String roleId) {
        roleRepository.deleteById(roleId);
    }

    public SysRoleEntity getRoleByIdIfExisted(String roleId) throws Exception {
        Optional<SysRoleEntity> roleEntityOptional = roleRepository.findById(roleId);
        if (!roleEntityOptional.isPresent()) {
            throw new Exception(String.format("Role ID [%s] does not exist", roleId));
        }
        return roleEntityOptional.get();
    }
}
