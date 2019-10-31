package com.webank.wecube.platform.auth.server.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@ConfigurationProperties(prefix = "platform.auth.server")
public class AuthServerProperties {

    @NestedConfigurationProperty
    private JwtTokenProperties jwtToken = new JwtTokenProperties();

    public JwtTokenProperties getJwtToken() {
        return jwtToken;
    }

    public void setJwtToken(JwtTokenProperties jwtToken) {
        this.jwtToken = jwtToken;
    }

    public static class JwtTokenProperties {
        private int userRefreshToken = 30;
        private int userAccessToken = 1440;
        private int subSystemRefreshToken = 5;
        private int subSystemAccessToken = 60;

        public int getUserRefreshToken() {
            return userRefreshToken;
        }

        public void setUserRefreshToken(int userRefreshToken) {
            this.userRefreshToken = userRefreshToken;
        }

        public int getUserAccessToken() {
            return userAccessToken;
        }

        public void setUserAccessToken(int userAccessToken) {
            this.userAccessToken = userAccessToken;
        }

        public int getSubSystemRefreshToken() {
            return subSystemRefreshToken;
        }

        public void setSubSystemRefreshToken(int subSystemRefreshToken) {
            this.subSystemRefreshToken = subSystemRefreshToken;
        }

        public int getSubSystemAccessToken() {
            return subSystemAccessToken;
        }

        public void setSubSystemAccessToken(int subSystemAccessToken) {
            this.subSystemAccessToken = subSystemAccessToken;
        }

        @Override
        public String toString() {
            return "userRefreshToken=" + userRefreshToken + ", userAccessToken=" + userAccessToken
                    + ", subSystemRefreshToken=" + subSystemRefreshToken + ", subSystemAccessToken="
                    + subSystemAccessToken + "";
        }
        
        

    }

}
