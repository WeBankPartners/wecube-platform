package com.webank.wecube.platform.auth.server.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.webank.wecube.platform.auth.server.dto.CreateSubsystemDto;
import com.webank.wecube.platform.auth.server.entity.SysSubSystemEntity;
import com.webank.wecube.platform.auth.server.repository.SubSystemRepository;

@Service("subsystemService")
public class SubsystemService {

	private static final Logger log = LoggerFactory.getLogger(SubsystemService.class);
	private final static Boolean ACTIVE = true;

	@Autowired
	private SubSystemRepository subSystemRepository;

	public SysSubSystemEntity create(CreateSubsystemDto createSubsystemDto) throws Exception {

		SysSubSystemEntity existedSubSystemEntity = subSystemRepository
				.findOneBySystemCode(createSubsystemDto.getSystemCode());

		log.info("existedAuthorityEntity = {}", existedSubSystemEntity);
		if (!(null == existedSubSystemEntity))
			throw new Exception(
					String.format("Sub system code [%s] already existed", createSubsystemDto.getSystemCode()));

		SysSubSystemEntity subSystem = new SysSubSystemEntity(createSubsystemDto.getName(),
				createSubsystemDto.getSystemCode(), createSubsystemDto.getApiKey(), createSubsystemDto.getPubApiKey(),
				ACTIVE);
		subSystemRepository.saveAndFlush(subSystem);

		return subSystem;
	}

	public List<SysSubSystemEntity> retrieve() {
		return subSystemRepository.findAll();
	}

	public void delete(Long id) {
		subSystemRepository.deleteById(id);
	}
}
