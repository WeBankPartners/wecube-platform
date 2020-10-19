package com.webank.wecube.platform.core.service.workflow;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.webank.wecube.platform.core.utils.EncryptionUtils;

@Service
public class EncryptionService {
    
    private static final String AES_SEED = "platform-aes-seed-2020";
    private static final String AES_SALT = "platform-aes-salt-2020";
    
    public String encodeToAesBase64(String raw) {
        if(StringUtils.isBlank(raw)) {
            return raw;
        }
        
        return EncryptionUtils.encryptWithAes(raw, AES_SEED, AES_SALT);
    }
    
    public String decodeFromAesBase64(String aesBase64) {
        if(StringUtils.isBlank(aesBase64)) {
            return aesBase64;
        }
        
        return EncryptionUtils.decryptWithAes(aesBase64, AES_SEED, AES_SALT);
    }

}
