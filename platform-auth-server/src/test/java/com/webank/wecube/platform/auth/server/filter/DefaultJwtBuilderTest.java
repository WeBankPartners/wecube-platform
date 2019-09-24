package com.webank.wecube.platform.auth.server.filter;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webank.wecube.platform.auth.server.dto.ScopedAuthoritiesClaimDto;
import com.webank.wecube.platform.auth.server.dto.ScopedAuthoritiesDto;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

public class DefaultJwtBuilderTest {
    
    private static final String SIGNING_KEY = "platform-auth-server-@Jwt!&Secret^#";
    
    private static final Logger log = LoggerFactory.getLogger(DefaultJwtBuilderTest.class);

    @Test
    public void testBuildRefreshToken() {
//        fail("Not yet implemented");
    }

    @Test
    public void testBuildAccessToken() {
//        fail("Not yet implemented");
    }
    
    @Test
    public void testCreateJwt(){
        Date now = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(Calendar.MINUTE, 3600);
        Date expireTime = calendar.getTime();
        
        ScopedAuthoritiesClaimDto dto = buildScopedAuthoritiesClaimDto();
        
        String refreshToken = Jwts.builder()
                .setSubject("user" + "-" + "refreshToken")
                .setIssuedAt(now)
                .claim("type", "refreshToken")
                .claim(dto.getName(), dto)
                .setExpiration(expireTime)
                .signWith(SignatureAlgorithm.HS512, SIGNING_KEY)
                .compact();
        
        log.info("refreshToken:{}", refreshToken);
        
        Jws<Claims> jws = Jwts.parser()
        .setSigningKey(SIGNING_KEY)
        .parseClaimsJws(refreshToken);
        
        Claims claims = jws.getBody();
        
        String type  =claims.get("type", String.class);
        
        log.info("type:{}", type);
        
        Map parsedDto = claims.get(dto.getName(), Map.class);
        
        String subject = claims.getSubject();
        
        log.info("subject:{}", subject);
        
        log.info("{} {}", dto.getName(), parsedDto);
    }
    
    private ScopedAuthoritiesClaimDto buildScopedAuthoritiesClaimDto(){
        ScopedAuthoritiesClaimDto dto = new ScopedAuthoritiesClaimDto();
        dto.setName("scopedAuthorities");
        dto.addScopedAuthoritiesDto(buildScopedAuthoritiesDto());
        dto.addScopedAuthoritiesDto(buildScopedAuthoritiesDto());
        dto.addScopedAuthoritiesDto(buildScopedAuthoritiesDto());
        
        return dto;
    }
    
    private ScopedAuthoritiesDto buildScopedAuthoritiesDto(){
        ScopedAuthoritiesDto dto = new ScopedAuthoritiesDto();
        dto.setAuthorities(Arrays.asList("admin", "test"));
        dto.setAlg("SHA");
        dto.setScope(String.valueOf(System.currentTimeMillis()));
        
        return dto;
    }

}
