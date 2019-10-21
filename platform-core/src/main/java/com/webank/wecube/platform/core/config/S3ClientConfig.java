package com.webank.wecube.platform.core.config;


import com.webank.wecube.platform.core.commons.ApplicationProperties.S3Properties;
import com.webank.wecube.platform.core.support.RealS3Client;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//@Configuration
public class S3ClientConfig {

//    @Autowired
//    private S3Properties s3Properties;
//
//    @Bean
//    public RealS3Client configS3Client() {
//        return new RealS3Client(s3Properties.getEndpoint(), s3Properties.getAccessKey(), s3Properties.getSecretKey());
//    }
}
