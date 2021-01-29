package com.webank.wecube.platform.auth.server.encryption;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;

import com.webank.wecube.platform.auth.server.common.ApplicationConstants;
import com.webank.wecube.platform.auth.server.common.util.StringUtilsEx;
import com.webank.wecube.platform.auth.server.dto.CredentialDto;

public class EncryptionUtilsTest {

    private Logger log = LoggerFactory.getLogger(getClass());

    private String privateKey = "MIIBUwIBADANBgkqhkiG9w0BAQEFAASCAT0wggE5AgEAAkEAiV9cxCoaixsNg2ItLcmxwIT3dTuRlY4EIlM1ytwnbxO1912i4gMddCWcwLMJdpdMLsCBp9nJRQ/4pJeIppLm7QIDAQABAkAVs3PjJUeWLArhc3PxpMgowpiY83UXLB0pEv4PcuHj4Pr1Op/mBIL8sRrfEcOr1V5HRxNACTSdiPwqFuflnU99AiEA87csRj/hs1+KX293rlouLtVA1qhwqgAlEHRv4+vfoJcCIQCQS/knZ5dc2LC2pidh/rywiMSrGAcQHi4ZGLY4b7KhGwIgN5KtJderP5upkdl5EOi/Xy6BenEuP5WI3heu6+n9NEECIHtyw+HWWkRwjh5039SSntNY7wiBMem0KDQIVDzMMsJpAiA+2F3+x+QuV09Dz826IRV639XKhp0J9aDsxRsypS6PRA==";

    private String publicKey = "MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAIlfXMQqGosbDYNiLS3JscCE93U7kZWOBCJTNcrcJ28TtfddouIDHXQlnMCzCXaXTC7AgafZyUUP+KSXiKaS5u0CAwEAAQ==";

    @Test
    public void testInitAsymmetricKeyPair() {
        AsymmetricKeyPair keyPair = EncryptionUtils.initAsymmetricKeyPair();
        Assert.assertNotNull(keyPair);

        log.info("privateKey:{}", keyPair.getPrivateKey());
        log.info("publicKey:{}", keyPair.getPublicKey());
    }

    @Test
    public void testEncryptByPrivateKeyAndDecrptByPublicKey() {
        String systemCode = "HTTP-MOCK";
        String nonce = "123";
        String password = String.format("%s:%s", systemCode, nonce);

        String cipherPassword = EncryptionUtils.encryptByPrivateKeyAsString(password.getBytes(EncryptionUtils.UTF8),
                privateKey);

        Assert.assertNotNull(cipherPassword);

        log.info("password:{}", cipherPassword);

        String plainPassword = new String(
                EncryptionUtils.decryptByPublicKeyAsString(StringUtilsEx.decodeBase64(cipherPassword), publicKey),
                EncryptionUtils.UTF8);

        Assert.assertNotNull(plainPassword);

        Assert.assertEquals(password, plainPassword);

    }

    @Ignore
    @Test
    public void testLoginWithSubSystemCode() {
        String loginEndPoint = "http://{auth-server-host}:{auth-server-port}/auth/v1/api/login";
        String subSystemCode = "SUB_SYSTEM_CODE_A";
        String nonce = "123";// random number here
        String plainPassword = String.format("%s:%s", subSystemCode, nonce);
        String cipherPassword = EncryptionUtils
                .encryptByPrivateKeyAsString(plainPassword.getBytes(EncryptionUtils.UTF8), privateKey);

        CredentialDto credential = new CredentialDto();
        credential.setClientType(ApplicationConstants.ClientType.SUB_SYSTEM);
        credential.setNonce(nonce);
        credential.setPassword(cipherPassword);
        credential.setUsername(subSystemCode);

        RestTemplate restClient = new RestTemplate();
        String responseBody = restClient.postForObject(loginEndPoint, credential, String.class);
        log.info("RESULT:/n{}", responseBody);
    }

    @Test
    public void testEncryptByPrivateKeyAsString() {
        String txt = String.format("%s:%s", "SYS_MONITOR", "123");
        String cipherTxt = EncryptionUtils.encryptStringWithBase64Key(txt, privateKey);
//        System.out.println(cipherTxt);
    }

    @Test
    public void testEncryptByPublicKey() {
    }

    @Test
    public void testEncryptByPublicKeyAsString() {
    }

    @Test
    public void testDecryptByPrivateKey() {
    }

    @Test
    public void testDecryptByPrivateKeyAsString() {
    }

    @Test
    public void testDecryptByPublicKey() {

        try {
            String cipherTxt = "YrTDHUQLK2A0+kquQiooySx5X1S0nFnfmDCog7Kbn+nrhX5f2m0poLDU950NUqIPwSEIOAw4U4BtyYgKgr+2ng==";
            String plainTxt = EncryptionUtils.decryptStringWithBase64Key(cipherTxt, publicKey);
//            System.out.println(plainTxt);
        } catch (Exception e) {
//            e.printStackTrace();
        }
    }

    @Test
    public void testDecryptByPublicKeyAsString() {
    }

    @Test
    public void testEncrypt() {
    }

    @Test
    public void testEncryptWithBase64Key() {
    }

    @Test
    public void testEncryptStringWithBase64Key() {
    }

    @Test
    public void testDecrypt() {
    }

    @Test
    public void testDecryptWithBase64Key() {
    }

    @Test
    public void testDecryptStringWithBase64Key() {
    }

}
