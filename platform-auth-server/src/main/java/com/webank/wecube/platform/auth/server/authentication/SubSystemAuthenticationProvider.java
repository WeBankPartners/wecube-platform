package com.webank.wecube.platform.auth.server.authentication;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import com.webank.wecube.platform.auth.server.common.util.StringUtilsEx;
import com.webank.wecube.platform.auth.server.encryption.EncryptionUtils;
import com.webank.wecube.platform.auth.server.model.SysSubSystemInfo;
import com.webank.wecube.platform.auth.server.service.SubSystemInfoDataService;

/**
 * 
 * @author gavin
 *
 */
@Component("subSystemAuthenticationProvider")
public class SubSystemAuthenticationProvider implements AuthenticationProvider {
    private static final Logger log = LoggerFactory.getLogger(SubSystemAuthenticationProvider.class);
    private static final String DELIMITER_SYSTEM_CODE_AND_NONCE = ":";

    @Autowired
    private SubSystemInfoDataService subSystemDataService;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        if (!(authentication instanceof SubSystemAuthenticationToken)) {
            log.error("only {} type supported", SubSystemAuthenticationToken.class.getSimpleName());
            throw new IllegalArgumentException("such authentication type doesnt supported");
        }

        SubSystemAuthenticationToken subSystemAuthToken = (SubSystemAuthenticationToken) authentication;

        String systemCode = (String) subSystemAuthToken.getPrincipal();

        SysSubSystemInfo subSystemInfo = retrieveSubSystemInfo(systemCode, subSystemAuthToken);

        verifySubSystemAuthenticationToken(subSystemAuthToken, subSystemInfo);

        return createSuccessAuthentication(subSystemInfo, subSystemAuthToken);
    }

    protected void verifySubSystemAuthenticationToken(SubSystemAuthenticationToken subSystemAuthToken,
            SysSubSystemInfo subSystemInfo) {
        String systemCode = (String) subSystemAuthToken.getPrincipal();
        String password = (String) subSystemAuthToken.getCredentials();
        String nonce = (String) subSystemAuthToken.getNonce();

        String subSystemPublicKey = subSystemInfo.getPubApiKey();
        
        if(StringUtils.isBlank(subSystemPublicKey)){
            log.warn("sub system public key is blank for system code:{}", systemCode);
            throw new BadCredentialsException("Bad credential and failed to decrypt password.");
        }

        String decryptedPassword = new String(
                EncryptionUtils.decryptByPublicKeyAsString(StringUtilsEx.decodeBase64(password), subSystemPublicKey),
                EncryptionUtils.UTF8);

        String[] decryptedPasswordParts = decryptedPassword.split(DELIMITER_SYSTEM_CODE_AND_NONCE);
        if ((decryptedPasswordParts.length < 2) && (!systemCode.equals(decryptedPasswordParts[0]))
                && (!nonce.equals(decryptedPasswordParts[1]))) {
            throw new BadCredentialsException("Bad credential");
        }
    }

    protected Authentication createSuccessAuthentication(SysSubSystemInfo retrievedSubSystemInfo,
            SubSystemAuthenticationToken authToken) {

        SubSystemAuthenticationToken returnAuthToken = new SubSystemAuthenticationToken(authToken.getPrincipal(),
                authToken.getCredentials(), authToken.getNonce(), retrievedSubSystemInfo.getAuthorities());

        return returnAuthToken;

    }

    protected SysSubSystemInfo retrieveSubSystemInfo(String systemCode, SubSystemAuthenticationToken authToken) {
        SysSubSystemInfo subSystemInfo = subSystemDataService.retrieveSysSubSystemInfoWithSystemCode(systemCode);

        if (subSystemInfo == null) {
            String errMsg = String.format("%s does not exist", systemCode);
            if (log.isInfoEnabled()) {
                log.info(errMsg);
            }
            throw new UsernameNotFoundException(errMsg);
        }

        return subSystemInfo;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return SubSystemAuthenticationToken.class.isAssignableFrom(authentication);
    }

}
