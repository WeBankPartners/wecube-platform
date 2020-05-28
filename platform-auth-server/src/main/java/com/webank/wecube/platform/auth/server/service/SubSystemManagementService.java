package com.webank.wecube.platform.auth.server.service;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.webank.wecube.platform.auth.server.authentication.SubSystemAuthenticationToken;
import com.webank.wecube.platform.auth.server.common.AuthServerException;
import com.webank.wecube.platform.auth.server.config.AuthServerProperties;
import com.webank.wecube.platform.auth.server.dto.SimpleSubSystemDto;
import com.webank.wecube.platform.auth.server.dto.SubSystemTokenDto;
import com.webank.wecube.platform.auth.server.encryption.AsymmetricKeyPair;
import com.webank.wecube.platform.auth.server.encryption.EncryptionUtils;
import com.webank.wecube.platform.auth.server.entity.SysSubSystemEntity;
import com.webank.wecube.platform.auth.server.http.AuthenticationContextHolder;
import com.webank.wecube.platform.auth.server.http.filter.DefaultJwtBuilder;
import com.webank.wecube.platform.auth.server.model.JwtToken;
import com.webank.wecube.platform.auth.server.model.SysSubSystemInfo;
import com.webank.wecube.platform.auth.server.repository.SubSystemRepository;

@Service("subSystemManagementService")
public class SubSystemManagementService {

	private static final Logger log = LoggerFactory.getLogger(SubSystemManagementService.class);

	private static final String DATE_PATTERN = "yyyyMMdd";

	private static final String MSG_BAD_CREDENTIAL = "Bad credential.";

	@Autowired
	private SubSystemInfoDataService subSystemInfoDataService;

	@Autowired
	private SubSystemRepository subSystemRepository;

	@Autowired
	private AuthServerProperties authServerProperties;

	private DefaultJwtBuilder jwtBuilder;

	@PostConstruct
	public void afterPropertiesSet() {
		jwtBuilder = new DefaultJwtBuilder(authServerProperties.getJwtToken());
	}

	public SubSystemTokenDto registerSubSystemAccessToken(SubSystemTokenDto subSystemDto) {
		SubSystemTokenDto result = new SubSystemTokenDto();
		result.setSystemCode(subSystemDto.getSystemCode());

		if (!validateSubSystemTokenFields(subSystemDto)) {
			return result;
		}

		JwtToken accessToken = tryAuthenticateSubSystem(subSystemDto);
		if (accessToken == null) {
			return result;
		}

		result.setAccessToken(accessToken.getToken());
		result.setCreateDate(String.valueOf(System.currentTimeMillis()));
		result.setExpireDate(String.valueOf(accessToken.getExpiration()));

		return result;
	}

	private JwtToken tryAuthenticateSubSystem(SubSystemTokenDto subSystem) {
		String systemCode = subSystem.getSystemCode();
		SysSubSystemInfo subSystemInfo = subSystemInfoDataService.retrieveSysSubSystemInfoWithSystemCode(systemCode);
		if (subSystemInfo == null) {
			throw new BadCredentialsException(String.format("Sub system %s does not exist.", systemCode));
		}

		if (subSystemInfo.getBlocked() == true) {
			throw new BadCredentialsException(String.format("Sub system %s is blocked.", systemCode));
		}

		if (subSystemInfo.getActive() == false) {
			throw new BadCredentialsException(String.format("Sub system %s is inactive.", systemCode));
		}

		return doAuthenticateSubSystem(subSystem, subSystemInfo);
	}

	private Authentication tryAuthenticate(SubSystemTokenDto subSystem, SysSubSystemInfo subSystemInfo) {
		Date currTime = new Date();
		String currTimeStr = formatDate(currTime);
		if (!currTimeStr.equals(subSystem.getCreateDate())) {
			throw new BadCredentialsException(MSG_BAD_CREDENTIAL);
		}

		if (subSystem.getNonce().length() != 10) {
			throw new BadCredentialsException(MSG_BAD_CREDENTIAL);
		}

		int len = Integer.parseInt(subSystem.getNonce().substring(8));
		if (len != subSystem.getSystemCode().length()) {
			throw new BadCredentialsException(MSG_BAD_CREDENTIAL);
		}

		return createSuccessAuthentication(subSystemInfo, subSystem);
	}

	protected Authentication createSuccessAuthentication(SysSubSystemInfo retrievedSubSystemInfo,
			SubSystemTokenDto subSystem) {

		SubSystemAuthenticationToken returnAuthToken = new SubSystemAuthenticationToken(subSystem.getSystemCode(),
				subSystem.getSystemCode(), subSystem.getNonce(), retrievedSubSystemInfo.getAuthorities());

		return returnAuthToken;

	}

	private JwtToken doAuthenticateSubSystem(SubSystemTokenDto subSystem, SysSubSystemInfo subSystemInfo) {
		Date expireTime = tryCalculateExpireTime(subSystem);
		Authentication auth = tryAuthenticate(subSystem, subSystemInfo);
		return jwtBuilder.buildAccessToken(auth, expireTime);
	}

	private Date tryCalculateExpireTime(SubSystemTokenDto subSystem) {
		Date expireDate = parseDate(subSystem.getExpireDate());
		return expireDate;
	}

	private Date parseDate(String dateTimeStr) {
		DateFormat df = new SimpleDateFormat(DATE_PATTERN);
		try {
			return df.parse(dateTimeStr);
		} catch (ParseException e) {
			throw new RuntimeException("Bad date format.");
		}
	}

	private String formatDate(Date dateTime) {
		DateFormat df = new SimpleDateFormat(DATE_PATTERN);
		return df.format(dateTime);
	}

	private boolean validateSubSystemTokenFields(SubSystemTokenDto dto) {
		if (dto == null) {
			return false;
		}

		if (StringUtils.isBlank(dto.getSystemCode())) {
			return false;
		}

		if (StringUtils.isBlank(dto.getNonce())) {
			return false;
		}

		if (StringUtils.isBlank(dto.getCreateDate())) {
			return false;
		}

		if (StringUtils.isBlank(dto.getExpireDate())) {
			return false;
		}

		return true;
	}

	public SimpleSubSystemDto registerSubSystem(SimpleSubSystemDto subSystemDto) {

		if (StringUtils.isBlank(subSystemDto.getSystemCode())) {
			throw new AuthServerException("Registering sub-system errors:system code cannot be blank.");
		}

		SysSubSystemEntity subSystem = subSystemRepository.findOneBySystemCode(subSystemDto.getSystemCode());

		if (subSystem != null) {
			log.debug("such sub-system already exists,system code {}", subSystemDto.getSystemCode());
			throw new AuthServerException(
					String.format("Sub-System with code {%s} already exists.", subSystemDto.getSystemCode()));
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

	private SimpleSubSystemDto convertToSimpleSubSystemDto(SysSubSystemEntity subSystem) {
		SimpleSubSystemDto dto = new SimpleSubSystemDto();
		dto.setId(subSystem.getId());
		dto.setActive(subSystem.isActive());
		dto.setBlocked(subSystem.isBlocked());
		dto.setDescription(subSystem.getDescription());
		dto.setName(subSystem.getName());
		dto.setSystemCode(subSystem.getSystemCode());

		return dto;
	}
	
	public SimpleSubSystemDto retrieveSubSystemApikey(String systemCode){
	    SimpleSubSystemDto result = new SimpleSubSystemDto();
	    if(StringUtils.isBlank(systemCode)){
	        return result;
	    }
	    
	    SysSubSystemEntity subSystem = subSystemRepository.findOneBySystemCode(systemCode);
	    if(subSystem == null){
	        return result;
	    }
	    
	    result.setId(subSystem.getId());
	    result.setActive(subSystem.isActive());
	    result.setBlocked(subSystem.isBlocked());
        result.setDescription(subSystem.getDescription());
        result.setName(subSystem.getName());
        result.setSystemCode(subSystem.getSystemCode());
        result.setApikey(subSystem.getApiKey());
        
        return result;
	}

	public List<SimpleSubSystemDto> retrieveAllSubSystems() {
		List<SysSubSystemEntity> subSystems = subSystemRepository.findAll();

		List<SimpleSubSystemDto> result = new ArrayList<>();
		if (subSystems == null || subSystems.isEmpty()) {
			return result;
		}

		for (SysSubSystemEntity subSystem : subSystems) {
			SimpleSubSystemDto d = convertToSimpleSubSystemDto(subSystem);
			result.add(d);
		}
		return result;
	}
}
