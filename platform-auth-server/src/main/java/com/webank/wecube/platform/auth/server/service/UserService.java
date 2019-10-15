package com.webank.wecube.platform.auth.server.service;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.webank.wecube.platform.auth.server.dto.CreateUserDto;
import com.webank.wecube.platform.auth.server.entity.SysUserEntity;
import com.webank.wecube.platform.auth.server.entity.SysUserEntity;
import com.webank.wecube.platform.auth.server.repository.UserRepository;

@Service("userService")
public class UserService {

	private static final Logger log = LoggerFactory.getLogger(UserService.class);

	@Autowired
	private UserRepository userRepository;

	@Autowired
	PasswordEncoder passwordEncoder;

	private final static Boolean ACTIVE = true;

	public SysUserEntity create(CreateUserDto createUserDto) throws Exception {

		SysUserEntity existedUser = userRepository.findOneByUsername(createUserDto.getUserName());

		log.info("existUser = {}", existedUser);
		if (!(null == existedUser))
			throw new Exception(String.format("User [%s] already existed", createUserDto.getUserName()));

		SysUserEntity user = new SysUserEntity(createUserDto.getUserName(),
				passwordEncoder.encode(createUserDto.getPassword()), ACTIVE);
		userRepository.saveAndFlush(user);

		return user;
	}

	public List<SysUserEntity> retrieve() {
		return userRepository.findByActive(ACTIVE);
	}

	public void delete(Long id) {
		userRepository.deleteById(id);
	}

	public SysUserEntity getUserByIdIfExisted(Long userId) throws Exception {
		Optional<SysUserEntity> UserEntityOptional = userRepository.findById(userId);
		if (!UserEntityOptional.isPresent())
			throw new Exception(String.format("User ID [%d] does not exist", userId));
		return UserEntityOptional.get();
	}
}
