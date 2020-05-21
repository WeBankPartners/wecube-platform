package com.webank.wecube.platform.auth.server.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.webank.wecube.platform.auth.server.common.AuthServerException;
import com.webank.wecube.platform.auth.server.dto.SimpleSubSystemDto;
import com.webank.wecube.platform.auth.server.dto.SubSystemTokenDto;
import com.webank.wecube.platform.auth.server.encryption.AsymmetricKeyPair;
import com.webank.wecube.platform.auth.server.encryption.EncryptionUtils;
import com.webank.wecube.platform.auth.server.entity.SysSubSystemEntity;
import com.webank.wecube.platform.auth.server.http.AuthenticationContextHolder;
import com.webank.wecube.platform.auth.server.model.JwtToken;
import com.webank.wecube.platform.auth.server.repository.SubSystemRepository;

@Service("subSystemManagementService")
public class SubSystemManagementService {

	private static final Logger log = LoggerFactory.getLogger(SubSystemManagementService.class);

	@Autowired
	private SubSystemRepository subSystemRepository;
	
	public SubSystemTokenDto registerSubSystemAccessToken(SubSystemTokenDto subSystemDto){
	    SubSystemTokenDto result = new SubSystemTokenDto();
	    result.setSystemCode(subSystemDto.getSystemCode());
	    
	    if(!validateSubSystemTokenFields(subSystemDto)){
	        return result;
	    }
	    
	    JwtToken accessToken = tryAuthenticateSubSystem(subSystemDto);
	    if(accessToken == null){
	        return result;
	    }
	    
	    result.setAccessToken(accessToken.getToken());
	    result.setCreateDate(String.valueOf(System.currentTimeMillis()));
	    result.setExpireDate(String.valueOf(accessToken.getExpiration()));
	    
	    return result;
	}
	
	private JwtToken tryAuthenticateSubSystem(SubSystemTokenDto subSystemDto){
	    
	    return null;
	}
	
	private boolean validateSubSystemTokenFields(SubSystemTokenDto dto){
	    if(dto == null){
	        return false;
	    }
	    
	    if(StringUtils.isBlank(dto.getSystemCode())){
	        return false;
	    }
	    
	    if(StringUtils.isBlank(dto.getNonce())){
	        return false;
	    }
	    
	    if(StringUtils.isBlank(dto.getCreateDate())){
	        return false;
	    }
	    
	    if(StringUtils.isBlank(dto.getExpireDate())){
	        return false;
	    }
	    
	    return true;
	}

	public SimpleSubSystemDto registerSubSystem(SimpleSubSystemDto subSystemDto){
	    
	    if(StringUtils.isBlank(subSystemDto.getSystemCode())){
	        throw new AuthServerException("Registering sub-system errors:system code cannot be blank.");
	    }

		SysSubSystemEntity subSystem = subSystemRepository
				.findOneBySystemCode(subSystemDto.getSystemCode());
		
		if(subSystem != null){
		    log.debug("such sub-system already exists,system code {}", subSystemDto.getSystemCode());
		    throw new AuthServerException(String.format("Sub-System with code {%s} already exists.", subSystemDto.getSystemCode()));
		}
		
		AsymmetricKeyPair keyPair = EncryptionUtils.initAsymmetricKeyPair();
		
		subSystem = new SysSubSystemEntity();
		subSystem.setCreatedBy(AuthenticationContextHolder.getCurrentUsername());
		subSystem.setDescription(subSystemDto.getDescription());
		subSystem.setApiKey(keyPair.getPrivateKey());
		subSystem.setPubApiKey(keyPair.getPublicKey());
		subSystem.setName(subSystemDto.getName());
		subSystem.setSystemCode(subSystemDto.getSystemCode());
		subSystem.setActive(true);
		subSystem.setBlocked(false);
		
		SysSubSystemEntity savedSubSystem = subSystemRepository.saveAndFlush(subSystem);

		return convertToSimpleSubSystemDto(savedSubSystem);
	}
	
	private SimpleSubSystemDto convertToSimpleSubSystemDto(SysSubSystemEntity subSystem){
	    SimpleSubSystemDto dto = new SimpleSubSystemDto();
	    dto.setId(subSystem.getId());
	    dto.setActive(subSystem.isActive());
	    dto.setBlocked(subSystem.isBlocked());
	    dto.setDescription(subSystem.getDescription());
	    dto.setName(subSystem.getName());
	    dto.setSystemCode(subSystem.getSystemCode());
	    
	    return dto;
	}

	public List<SimpleSubSystemDto> retrieveAllSubSystems() {
	    List<SysSubSystemEntity> subSystems = subSystemRepository.findAll();
	    
	    List<SimpleSubSystemDto> result = new ArrayList<>();
	    if(subSystems == null || subSystems.isEmpty()){
	        return result;
	    }
	    
	    for(SysSubSystemEntity subSystem: subSystems){
	        SimpleSubSystemDto d = convertToSimpleSubSystemDto(subSystem);
	        result.add(d);
	    }
		return result;
	}
}
