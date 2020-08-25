package com.webank.wecube.platform.auth.server.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.webank.wecube.platform.auth.server.common.AuthServerException;
import com.webank.wecube.platform.auth.server.dto.SimpleLocalRoleDto;
import com.webank.wecube.platform.auth.server.dto.SimpleLocalUserDto;
import com.webank.wecube.platform.auth.server.dto.SimpleLocalUserPassDto;
import com.webank.wecube.platform.auth.server.entity.SysRoleEntity;
import com.webank.wecube.platform.auth.server.entity.SysUserEntity;
import com.webank.wecube.platform.auth.server.entity.UserRoleRsEntity;
import com.webank.wecube.platform.auth.server.http.AuthenticationContextHolder;
import com.webank.wecube.platform.auth.server.repository.RoleRepository;
import com.webank.wecube.platform.auth.server.repository.UserRepository;
import com.webank.wecube.platform.auth.server.repository.UserRoleRsRepository;

@Service("userManagementService")
public class UserManagementService {
    private static final Logger log = LoggerFactory.getLogger(UserManagementService.class);
    public static final String MASK_PASSWORD = "*";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserRoleRsRepository userRoleRsRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RoleRepository roleRepository;

    public SimpleLocalUserDto modifyLocalUserPassword(SimpleLocalUserPassDto userPassDto) {
        String username = userPassDto.getUsername();
        if (StringUtils.isBlank(username)) {
            throw new AuthServerException("Username cannot be blank.");
        }

        String originalPassword = userPassDto.getOriginalPassword();
        String toChangePassword = userPassDto.getChangedPassword();

        if (StringUtils.isBlank(originalPassword) || StringUtils.isBlank(toChangePassword)) {
            throw new AuthServerException("Password cannot be blank.");
        }
        
        return doModifyLocalUserPassword(username, originalPassword, toChangePassword);
    }

    private SimpleLocalUserDto doModifyLocalUserPassword(String username, String originalPassword, String toChangePassword) {
        SysUserEntity user = userRepository.findNotDeletedUserByUsername(username);
        if (user == null) {
            log.debug("Such user does not exist with username {}", username);
            String msg = String.format("Failed to modify a none existed user with username {%s}.", username);
            throw new AuthServerException("3021", msg, username);
        }
        
        if(SysUserEntity.AUTH_SOURCE_UM.equalsIgnoreCase(user.getAuthSource())){
            throw new AuthServerException("Cannot modify password of UM user account.");
        }
        
        if(StringUtils.isBlank(user.getPassword())){
            throw new AuthServerException("The password of user to modify is blank.");
        }
        
        if(!passwordEncoder.matches(originalPassword, user.getPassword())){
            throw new AuthServerException("The password of user to modify is invalid.");
        }
        
        String encodedNewPassword = encodePassword(toChangePassword);
        user.setPassword(encodedNewPassword);
        
        userRepository.saveAndFlush(user);
        
        return convertToSimpleLocalUserDto(user);
    }
    
    

    @Transactional
    public void revokeUserRolesById(String roleId, List<SimpleLocalUserDto> userDtos) {
        Optional<SysRoleEntity> roleOpt = roleRepository.findById(roleId);
        if (!roleOpt.isPresent()) {
            log.debug("revoking user roles error:such role entity does not exist, role id {}", roleId);
            throw new AuthServerException("3018", "Such role entity to revoke does not exist.");
        }

        SysRoleEntity role = roleOpt.get();

        for (SimpleLocalUserDto userDto : userDtos) {
            UserRoleRsEntity userRole = userRoleRsRepository.findOneByUserIdAndRoleId(userDto.getId(), role.getId());
            if (userRole == null) {
                continue;
            }

            if (userRole.isDeleted()) {
                continue;
            }

            userRole.setDeleted(true);
            userRole.setUpdatedBy(AuthenticationContextHolder.getCurrentUsername());
            userRole.setUpdatedTime(new Date());
            userRoleRsRepository.save(userRole);
        }
    }

    @Transactional
    public void configureUserRolesById(String roleId, List<SimpleLocalUserDto> userDtos) {
        Optional<SysRoleEntity> roleOpt = roleRepository.findById(roleId);
        if (!roleOpt.isPresent()) {
            log.debug("configuring user with roles error:such role entity does not exist, role id {}", roleId);
            throw new AuthServerException("3012", "Such role entity does not exist.");
        }

        SysRoleEntity role = roleOpt.get();

        for (SimpleLocalUserDto userDto : userDtos) {
            Optional<SysUserEntity> userOpt = userRepository.findById(userDto.getId());
            if (!userOpt.isPresent()) {
                log.debug("configuring user with roles error:user entity does not exist, user id {}", userDto.getId());
                throw new AuthServerException("3019", "Such user entity does not exist.");
            }

            SysUserEntity user = userOpt.get();

            UserRoleRsEntity userRole = userRoleRsRepository.findOneByUserIdAndRoleId(userDto.getId(), roleId);
            if (userRole != null) {
                log.info("such user role configuration already exist,userId={},roleId={}", userDto.getId(), roleId);
                continue;
            } else {
                userRole = new UserRoleRsEntity();
                userRole.setCreatedBy(AuthenticationContextHolder.getCurrentUsername());
                userRole.setUserId(userDto.getId());
                userRole.setUsername(user.getUsername());
                userRole.setRoleId(roleId);
                userRole.setRoleName(role.getName());

                userRoleRsRepository.save(userRole);
            }
        }

    }

    public List<SimpleLocalRoleDto> getLocalRolesByUsername(String username) {
        List<SimpleLocalRoleDto> roleDtos = new ArrayList<>();
        if (StringUtils.isBlank(username)) {
            throw new AuthServerException("3020", "Username cannot be blank.");
        }
        SysUserEntity user = userRepository.findNotDeletedUserByUsername(username);

        if (user == null) {
            return roleDtos;
        }

        List<UserRoleRsEntity> userRoles = userRoleRsRepository.findAllByUserId(user.getId());

        if (userRoles == null || userRoles.isEmpty()) {
            return roleDtos;
        }

        for (UserRoleRsEntity userRole : userRoles) {
            Optional<SysRoleEntity> roleOpt = roleRepository.findById(userRole.getRoleId());
            if (!roleOpt.isPresent()) {
                log.debug("cannot find such role entity with role id {}", userRole.getRoleId());
                continue;
            }

            SysRoleEntity role = roleOpt.get();
            if (role.isDeleted()) {
                log.debug("such role entity is deleted,role id {}", role.getId());
                continue;
            }

            SimpleLocalRoleDto roleDto = new SimpleLocalRoleDto();
            roleDto.setId(role.getId());
            roleDto.setName(role.getName());
            roleDto.setDisplayName(role.getDisplayName());
            roleDto.setEmail(role.getEmailAddress());

            roleDtos.add(roleDto);
        }

        return roleDtos;
    }

    public List<SimpleLocalUserDto> getLocalUsersByRoleId(String roleId) {
        List<SimpleLocalUserDto> result = new ArrayList<>();

        List<UserRoleRsEntity> userRoles = userRoleRsRepository.findAllByRoleId(roleId);

        if (userRoles == null || userRoles.isEmpty()) {
            return result;
        }

        for (UserRoleRsEntity userRole : userRoles) {
            Optional<SysUserEntity> userOpt = userRepository.findById(userRole.getUserId());
            if (!userOpt.isPresent()) {
                continue;
            }

            SysUserEntity user = userOpt.get();
            SimpleLocalUserDto userDto = convertToSimpleLocalUserDto(user);
            result.add(userDto);
        }

        return result;
    }

    public SimpleLocalUserDto retrieveLocalUserByUsername(String username) {
        return null;
    }

    public SimpleLocalUserDto modifyLocalUserInfomation(String username, SimpleLocalUserDto userDto) {
        SysUserEntity user = userRepository.findNotDeletedUserByUsername(username);
        if (user == null) {
            log.debug("Such user does not exist with username {}", username);
            String msg = String.format("Failed to modify a none existed user with username {%s}.", username);
            throw new AuthServerException("3021", msg, username);
        }

        if (!username.equals(userDto.getUsername())) {
            throw new AuthServerException("3022", "Unexpected username to modify.");
        }

        user.setCellPhoneNo(userDto.getCellPhoneNo());
        user.setDepartment(userDto.getDepartment());
        user.setEmailAddr(userDto.getEmailAddr());
        user.setEnglishName(userDto.getEnglishName());
        user.setLocalName(userDto.getNativeName());
        user.setOfficeTelNo(userDto.getOfficeTelNo());
        user.setTitle(userDto.getTitle());
        user.setCellPhoneNo(userDto.getCellPhoneNo());
        user.setUpdatedBy(AuthenticationContextHolder.getCurrentUsername());
        user.setUpdatedTime(new Date());

        SysUserEntity modifiedUser = userRepository.saveAndFlush(user);
        return convertToSimpleLocalUserDto(modifiedUser);
    }

    public SimpleLocalUserDto registerLocalUser(SimpleLocalUserDto userDto) {
        validateSimpleLocalUserDto(userDto);
        SysUserEntity userEntity = userRepository.findNotDeletedUserByUsername(userDto.getUsername());
        if (userEntity != null) {
            log.info("such username {} to create has already existed.", userDto.getUsername());
            String msg = String.format("User {%s} already exists.", userDto.getUsername());
            throw new AuthServerException("3023", msg, userDto.getUsername());
        }

        userEntity = buildSysUserEntity(userDto);
        userRepository.saveAndFlush(userEntity);
        return convertToSimpleLocalUserDto(userEntity);
    }

    public List<SimpleLocalUserDto> retrieveAllActiveUsers() {
        List<SysUserEntity> userEntities = userRepository.findAllActiveUsers();
        List<SimpleLocalUserDto> result = new ArrayList<>();
        if (userEntities == null) {
            return result;
        }

        userEntities.forEach(user -> {
            SimpleLocalUserDto userDto = convertToSimpleLocalUserDto(user);

            List<UserRoleRsEntity> userRoles = userRoleRsRepository.findAllByUserId(user.getId());
            if (userRoles != null) {
                for (UserRoleRsEntity userRole : userRoles) {
                    Optional<SysRoleEntity> roleOpt = roleRepository.findById(userRole.getRoleId());
                    if (roleOpt.isPresent()) {
                        SysRoleEntity role = roleOpt.get();
                        SimpleLocalRoleDto roleDto = new SimpleLocalRoleDto();
                        roleDto.setId(role.getId());
                        roleDto.setDisplayName(role.getDisplayName());
                        roleDto.setName(role.getName());
                        roleDto.setEmail(role.getEmailAddress());

                        userDto.addRoles(roleDto);
                    }
                }
            }

            result.add(userDto);
        });

        return result;
    }

    @Transactional
    public void unregisterLocalUser(String userId) {
        Optional<SysUserEntity> userOpt = userRepository.findById(userId);
        if (!userOpt.isPresent()) {
            log.debug("Such user with ID {} does not exist.", userId);
            String msg = String.format("Such user with ID {%s} does not exist.", userId);
            throw new AuthServerException("3024", msg, userId);
        }

        SysUserEntity user = userOpt.get();
        if (user.isDeleted()) {
            log.debug("Such user with ID {} has already been deleted.", userId);
            String msg = String.format("Such user with ID {%s} does not exist.", userId);
            throw new AuthServerException("3024", msg, userId);
        }

        user.setActive(false);
        user.setDeleted(true);
        user.setUpdatedBy(AuthenticationContextHolder.getCurrentUsername());
        user.setUpdatedTime(new Date());
        userRepository.save(user);

        List<UserRoleRsEntity> userRoles = userRoleRsRepository.findAllByUserId(user.getId());

        if (userRoles != null) {
            for (UserRoleRsEntity userRole : userRoles) {
                userRole.setActive(false);
                userRole.setDeleted(true);
                userRole.setUpdatedBy(AuthenticationContextHolder.getCurrentUsername());
                userRole.setUpdatedTime(new Date());

                userRoleRsRepository.save(userRole);
            }
        }
    }

    private SimpleLocalUserDto convertToSimpleLocalUserDto(SysUserEntity user) {
        SimpleLocalUserDto dto = new SimpleLocalUserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setPassword(null);
        dto.setDepartment(user.getDepartment());
        dto.setEmailAddr(user.getEmailAddr());
        dto.setTitle(user.getTitle());
        dto.setEnglishName(user.getEnglishName());
        dto.setNativeName(user.getLocalName());
        dto.setCellPhoneNo(user.getCellPhoneNo());
        dto.setOfficeTelNo(user.getOfficeTelNo());
        dto.setActive(user.isActive());
        dto.setBlocked(user.isBlocked());

        return dto;
    }

    private SysUserEntity buildSysUserEntity(SimpleLocalUserDto dto) {
        SysUserEntity user = new SysUserEntity();
        user.setUsername(dto.getUsername());
        user.setPassword(encodePassword(dto.getPassword()));
        user.setCreatedBy(AuthenticationContextHolder.getCurrentUsername());
        user.setDepartment(dto.getDepartment());
        user.setEmailAddr(dto.getEmailAddr());
        user.setTitle(dto.getTitle());
        user.setEnglishName(dto.getEnglishName());
        user.setLocalName(dto.getNativeName());
        user.setCellPhoneNo(dto.getCellPhoneNo());
        user.setOfficeTelNo(dto.getOfficeTelNo());

        user.setAuthSource(dto.getAuthSource());
        user.setAuthContext(dto.getAuthContext());

        return user;
    }

    private String encodePassword(String rawPassword) {
        if (StringUtils.isBlank(rawPassword)) {
            return null;
        }

        return passwordEncoder.encode(rawPassword);
    }

    private void validateSimpleLocalUserDto(SimpleLocalUserDto userDto) {

        if (StringUtils.isBlank(userDto.getUsername())) {
            throw new AuthServerException("3025", "Username cannot be blank.");
        }

        String authSource = SysUserEntity.AUTH_SOURCE_LOCAL;
        if (!StringUtils.isBlank(userDto.getAuthSource())) {
            authSource = userDto.getAuthSource();
        }

        if (SysUserEntity.AUTH_SOURCE_LOCAL.equalsIgnoreCase(authSource)
                && StringUtils.isBlank(userDto.getPassword())) {
            throw new AuthServerException("3026", "Password cannot be blank.");
        }
    }
}
