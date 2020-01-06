package com.webank.wecube.platform.core;

import com.webank.wecube.platform.auth.client.context.JwtSsoClientContext;
import com.webank.wecube.platform.auth.client.model.JwtSsoAccessToken;
import com.webank.wecube.platform.auth.client.model.JwtSsoRefreshToken;
import com.webank.wecube.platform.core.commons.ApplicationProperties;
import com.webank.wecube.platform.core.commons.ApplicationProperties.*;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;

@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@EnableConfigurationProperties({
        ApplicationProperties.class,
        PluginProperties.class,
        S3Properties.class
})
public abstract class BaseSpringBootTest {
    @Autowired
    private JwtSsoClientContext jwtSsoClientContext;

    @Before
    public void initJwtSsoClientContext() {
        this.jwtSsoClientContext.setRefreshToken(getRefreshToken());
        this.jwtSsoClientContext.setAcccessToken(getAccessToken());
    }


    public JwtSsoRefreshToken getRefreshToken() {
        return new JwtSsoRefreshToken() {
            @Override
            public Date getExpireTime() {
                return new Date(System.currentTimeMillis() + 5 * 60 * 1000);
            }

            @Override
            public boolean isExpired() {
                return false;
            }

            @Override
            public String getToken() {
                return "Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxMTY4IiwiaWF0IjoxNTc4MzA1NzAyLCJ0eXBlIjoicmVmcmVzaFRva2VuIiwiY2xpZW50VHlwZSI6IlVTRVIiLCJleHAiOjE1NzgzMDc1MDJ9.dnCGb91Z9YDiUX6YBlpaZ7yakPsXNPVxSNAuT0LeM_2qpPkcztqdswBEe-01nnCNJlS_jMm1GPrHJrdaYRQSyQ";
            }

            @Override
            public String getTokenType() {
                return "refreshToken";
            }
        };
    }

    public JwtSsoAccessToken getAccessToken() {
        return new JwtSsoAccessToken() {
            @Override
            public Date getExpireTime() {
                return new Date(System.currentTimeMillis() + 5 * 60 * 1000);
            }

            @Override
            public boolean isExpired() {
                return false;
            }

            @Override
            public String getToken() {
                return "Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxMTY4IiwiaWF0IjoxNTc4MzA1NzAyLCJ0eXBlIjoiYWNjZXNzVG9rZW4iLCJjbGllbnRUeXBlIjoiVVNFUiIsImV4cCI6MTU3ODMwNjAwMiwiYXV0aG9yaXR5IjoiWzExNjhyb2xlXSJ9.sf1Rg7fdoYu3UOQQ0vk2v0vpEOVBMYx7ucIgxepieMQRB-j2VwrxnUyI7aiPQUYFgxn3DXt4BP97nHVSFneZBA";
            }

            @Override
            public String getTokenType() {
                return "accessToken";
            }
        };
    }
}
