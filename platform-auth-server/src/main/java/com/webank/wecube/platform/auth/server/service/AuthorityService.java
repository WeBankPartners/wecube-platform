package com.webank.wecube.platform.auth.server.service;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.webank.wecube.platform.auth.server.dto.CreateAuthorityDto;
import com.webank.wecube.platform.auth.server.entity.SysApiEntity;
import com.webank.wecube.platform.auth.server.entity.SysAuthorityEntity;
import com.webank.wecube.platform.auth.server.repository.AuthorityRepository;

@Service("authorityService")
public class AuthorityService {

	private static final Logger log = LoggerFactory.getLogger(AuthorityService.class);

	@Autowired
	private AuthorityRepository authorityRepository;

	public SysAuthorityEntity create(CreateAuthorityDto createAuthorityDto) throws Exception {

		SysAuthorityEntity existedAuthorityEntity = authorityRepository.findOneByCode(createAuthorityDto.getCode());

		log.info("existedAuthorityEntity = {}", existedAuthorityEntity);
		if (!(null == existedAuthorityEntity))
			throw new Exception(String.format("Authority code [%s] already existed", createAuthorityDto.getCode()));

		SysAuthorityEntity authority = new SysAuthorityEntity(createAuthorityDto.getCode(),
				createAuthorityDto.getDisplayName(), createAuthorityDto.getSystemId(),
				createAuthorityDto.getSystemCode(), createAuthorityDto.getSystemName());
		authorityRepository.saveAndFlush(authority);

		return authority;
	}

	public List<SysAuthorityEntity> retrieve() {
		return authorityRepository.findAll();
	}

	public void delete(Long id) {
		authorityRepository.deleteById(id);
	}

	public SysAuthorityEntity getAuthorityByIdIfExisted(Long authorityId) throws Exception {
		Optional<SysAuthorityEntity> authorityEntityOptional = authorityRepository.findById(authorityId);
		if (!authorityEntityOptional.isPresent())
			throw new Exception(String.format("Api ID [%d] does not exist", authorityId));
		return authorityEntityOptional.get();
	}
}
