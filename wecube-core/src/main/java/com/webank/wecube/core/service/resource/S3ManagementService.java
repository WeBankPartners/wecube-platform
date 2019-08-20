package com.webank.wecube.core.service.resource;

import org.springframework.stereotype.Service;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.webank.wecube.core.commons.WecubeCoreException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class S3ManagementService {
    private AmazonS3 amazonS3;

    public S3ManagementService newS3Client(String host, String port, String username, String password) {
        String endPoint = String.format("http://%s:%s", host, port);
        ClientConfiguration clientConfiguration = new ClientConfiguration();
        amazonS3 = AmazonS3ClientBuilder.standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endPoint, Regions.US_EAST_1.name()))
                .withPathStyleAccessEnabled(true)
                .withClientConfiguration(clientConfiguration)
                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(username, password)))
                .build();
        return this;
    }

    public void createBucket(String name) {
        if (amazonS3 != null) {
            if (amazonS3.doesBucketExist(name)) {
                throw new WecubeCoreException(String.format("Can not create bucket [%s] : Bucket exists.", name));
            }
            amazonS3.createBucket(name);
        }
    }

    public void deleteBucket(String name) {
        if (amazonS3 != null) {
            if (amazonS3.doesBucketExist(name)) {
                if (!amazonS3.listObjects(name).getObjectSummaries().isEmpty()) {
                    throw new WecubeCoreException(String.format("Can not delete bucket [%s] : Bucket have [%s] amount of objects", name, amazonS3.listObjects(name).getObjectSummaries().size()));
                }
                amazonS3.deleteBucket(name);
            } else {
                log.warn("To be delete bucket {%s} does not exists.", name);
            }
        }
    }
}
