package com.webank.wecube.platform.auth.server.filter;

import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webank.wecube.platform.auth.server.common.util.StringUtilsEx;
import com.webank.wecube.platform.auth.server.dto.ScopedAuthoritiesClaimDto;
import com.webank.wecube.platform.auth.server.dto.ScopedAuthoritiesDto;
import com.webank.wecube.platform.auth.server.encryption.AsymmetricKeyPair;
import com.webank.wecube.platform.auth.server.encryption.EncryptionUtils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

public class DefaultJwtBuilderTest {

    private static final String SIGNING_KEY = "platform-auth-serve";

    private Charset ISO_8859_1 = Charset.forName("ISO-8859-1");

    private static final Logger log = LoggerFactory.getLogger(DefaultJwtBuilderTest.class);

    @Test
    public void testBuildRefreshToken() {
        // fail("Not yet implemented");
    }

    @Test
    public void testBuildAccessToken() {
        // fail("Not yet implemented");
    }

    @Test
    public void testJwtUsageWithRSAAndDES() {
        byte[] signingKeyData = SecureRandom.getSeed(32);
        String signingKeyText = StringUtilsEx.encodeBase64String(signingKeyData);

        log.info("signingKeyText:{}", signingKeyText);

        AsymmetricKeyPair keyPair = EncryptionUtils.initAsymmetricKeyPair();

        String token = buildRefreshToken(signingKeyData);

        log.info("refresh token:{}", token);

        String desToken = StringUtilsEx
                .encodeBase64String(EncryptionUtils.encrypt(token.getBytes(ISO_8859_1), signingKeyData));
        
        log.info("desToken:{}", desToken);

        String privateKeyString = EncryptionUtils.encryptByPrivateKeyAsString(signingKeyData, keyPair.getPrivateKey());
        log.info("privateKeyString:{}", privateKeyString);

        String finalToken = String.format("%s.%s", privateKeyString, desToken);

        log.info("finalToken:{}", finalToken);

        parseFinalToken(finalToken, keyPair.getPublicKey());

    }

    private void parseFinalToken(final String finalToken, String publicKey) {
        String sSigningKey = finalToken.substring(0, finalToken.indexOf("."));
        log.info("sSigningKey:{}", sSigningKey);
        String desToken = finalToken.substring(finalToken.indexOf(".") + 1);
        log.info("desToken:{}", desToken);
        
        byte[] signingKey = EncryptionUtils.decryptByPublicKey(StringUtilsEx.decodeBase64(sSigningKey),
                StringUtilsEx.decodeBase64(publicKey));
        
        String sToken = new String(EncryptionUtils.decrypt(StringUtilsEx.decodeBase64(desToken), signingKey),ISO_8859_1);

        log.info("sToken:{}", sToken);
        

        Jws<Claims> jws = Jwts.parser().setSigningKey(signingKey).parseClaimsJws(sToken);

        Claims claims = jws.getBody();

        String tokenType = claims.get("type", String.class);

        log.info("tokenType:{}", tokenType);

        Assert.assertEquals("refreshToken", tokenType);
    }

    private String buildRefreshToken(byte[] signingKeyData) {
        Date now = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(Calendar.MINUTE, 3600);
        Date expireTime = calendar.getTime();

        String refreshToken = Jwts.builder().setSubject("umadmin").setIssuedAt(now).claim("type", "refreshToken")
                .claim("authority", "[USER,ADMIN]").setExpiration(expireTime)
                .signWith(SignatureAlgorithm.HS512, signingKeyData).compact();

        return refreshToken;
    }

    @Test
    public void testCreateJwt() {
        Date now = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(Calendar.MINUTE, 3600);
        Date expireTime = calendar.getTime();

        ScopedAuthoritiesClaimDto dto = buildScopedAuthoritiesClaimDto();

        String refreshToken = Jwts.builder().setSubject("user" + "-" + "refreshToken").setIssuedAt(now)
                .claim("type", "refreshToken").claim(dto.getName(), dto).setExpiration(expireTime)
                .signWith(SignatureAlgorithm.HS512, SIGNING_KEY).compact();

        log.info("refreshToken:{}", refreshToken);

        Jws<Claims> jws = Jwts.parser().setSigningKey(SIGNING_KEY).parseClaimsJws(refreshToken);

        Claims claims = jws.getBody();

        String type = claims.get("type", String.class);

        log.info("type:{}", type);

        Map parsedDto = claims.get(dto.getName(), Map.class);

        String subject = claims.getSubject();

        log.info("subject:{}", subject);

        log.info("{} {}", dto.getName(), parsedDto);
    }

    private ScopedAuthoritiesClaimDto buildScopedAuthoritiesClaimDto() {
        ScopedAuthoritiesClaimDto dto = new ScopedAuthoritiesClaimDto();
        dto.setName("scopedAuthorities");
        dto.addScopedAuthoritiesDto(buildScopedAuthoritiesDto());
        dto.addScopedAuthoritiesDto(buildScopedAuthoritiesDto());
        dto.addScopedAuthoritiesDto(buildScopedAuthoritiesDto());

        return dto;
    }

    private ScopedAuthoritiesDto buildScopedAuthoritiesDto() {
        ScopedAuthoritiesDto dto = new ScopedAuthoritiesDto();
        dto.setAuthorities(Arrays.asList("admin", "test"));
        dto.setAlg("SHA");
        dto.setScope(String.valueOf(System.currentTimeMillis()));

        return dto;
    }

}
