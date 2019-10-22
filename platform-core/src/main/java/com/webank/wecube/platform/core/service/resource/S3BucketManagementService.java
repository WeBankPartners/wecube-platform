package com.webank.wecube.platform.core.service.resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.webank.wecube.platform.core.commons.ApplicationProperties.ResourceProperties;
import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.domain.ResourceItem;
import com.webank.wecube.platform.core.utils.EncryptionUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class S3BucketManagementService implements ResourceItemService {

    @Autowired
    private ResourceProperties resourceProperties;

    public AmazonS3 newS3Client(String host, String port, String username, String password) {
        String endPoint = String.format("http://%s:%s", host, port);
        ClientConfiguration clientConfiguration = new ClientConfiguration();
        return AmazonS3ClientBuilder.standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endPoint, Regions.US_EAST_1.name()))
                .withPathStyleAccessEnabled(true)
                .withClientConfiguration(clientConfiguration)
                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(username, password)))
                .build();
    }

    @Override
    public ResourceItem createItem(ResourceItem item) {
        String password = EncryptionUtils.decryptWithAes(item.getResourceServer().getLoginPassword(), resourceProperties.getPasswordEncryptionSeed(), item.getResourceServer().getName());
        AmazonS3 amazonS3 = newS3Client(
                item.getResourceServer().getHost(),
                item.getResourceServer().getPort(),
                item.getResourceServer().getLoginUsername(),
                password);

        if (amazonS3 != null) {
            if (amazonS3.doesBucketExist(item.getName())) {
                throw new WecubeCoreException(String.format("Can not create bucket [%s] : Bucket exists.", item.getName()));
            }
            amazonS3.createBucket(item.getName());
        }
        return item;
    }

    @Override
    public void deleteItem(ResourceItem item) {
        String password = EncryptionUtils.decryptWithAes(item.getResourceServer().getLoginPassword(), resourceProperties.getPasswordEncryptionSeed(), item.getResourceServer().getName());
        AmazonS3 amazonS3 = newS3Client(
                item.getResourceServer().getHost(),
                item.getResourceServer().getPort(),
                item.getResourceServer().getLoginUsername(),
                password);

        if (amazonS3 != null) {
            if (amazonS3.doesBucketExist(item.getName())) {
                if (!amazonS3.listObjects(item.getName()).getObjectSummaries().isEmpty()) {
                    throw new WecubeCoreException(String.format("Can not delete bucket [%s] : Bucket have [%s] amount of objects", item.getName(), amazonS3.listObjects(item.getName()).getObjectSummaries().size()));
                }
                amazonS3.deleteBucket(item.getName());
            } else {
                log.warn("To be delete bucket {%s} does not exists.", item.getName());
            }
        }
    }

    @Override
    public ResourceItem retrieveItem(ResourceItem item) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ResourceItem updateItem(ResourceItem item) {
        throw new UnsupportedOperationException();
    }
}
