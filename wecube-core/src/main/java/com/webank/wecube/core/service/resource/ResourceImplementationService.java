package com.webank.wecube.core.service.resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.webank.wecube.core.commons.WecubeCoreException;
import com.webank.wecube.core.domain.ResourceItem;

@Service
public class ResourceImplementationService {

    private static final Logger logger = LoggerFactory.getLogger(ResourceImplementationService.class);

    @Autowired
    private MysqlManagementService mysqlManagementService;

    public void createItems(Iterable<ResourceItem> items) {
        for (ResourceItem item : items) {
            switch (ResourceItemType.fromCode(item.getType())) {
            case S3_BUCKET:
                createS3Bucket(item);
                break;
            case MYSQL_DATABASE:
                createMySQLDatabase(item);
                break;
            default:
                throw new WecubeCoreException(String.format("Doesn't support creation of resource type [%s].", item.getType()));
            }
        }
    }

    private void createMySQLDatabase(ResourceItem item) {
        if (item.getResourceServer() != null) {
            try {
                mysqlManagementService.createMysqlClient(item.getResourceServer().getHost(), item.getResourceServer().getPort(), item.getResourceServer().getLoginUsername(), item.getResourceServer().getLoginPassword());
                mysqlManagementService.connect();
                mysqlManagementService.createSchema(item.getName());
                mysqlManagementService.disconnect();
            } catch (Exception e) {
                throw new WecubeCoreException(String.format("Failed to create mysql database [%s] from server [%s].", item.getName(), item.getResourceServer()), e);
            }
        }
    }

    public void createS3Bucket(ResourceItem item) {
        AmazonS3 s3Client = createS3Client(item);
        if (s3Client.doesBucketExist(item.getName())) {
            throw new WecubeCoreException(String.format("Can not create bucket [%s] : Bucket exists.", item.getName()));
        }
        s3Client.createBucket(item.getName());
    }

    private AmazonS3 createS3Client(ResourceItem item) {
        if (item.getResourceServer() == null) {
            throw new WecubeCoreException("S3 server info is not found.");
        }

        String endPoint = String.format("http://%s:%s", item.getResourceServer().getHost(), item.getResourceServer().getPort());
        ClientConfiguration clientConfiguration = new ClientConfiguration();
        return AmazonS3ClientBuilder.standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endPoint, Regions.US_EAST_1.name()))
                .withPathStyleAccessEnabled(true)
                .withClientConfiguration(clientConfiguration)
                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(item.getResourceServer().getLoginUsername(), item.getResourceServer().getLoginPassword())))
                .build();
    }

    public void updateItems(Iterable<ResourceItem> items) {
        for (ResourceItem item : items) {
            switch (ResourceItemType.fromCode(item.getType())) {
            case S3_BUCKET:
                break;
            case MYSQL_DATABASE:
                break;
            default:
                throw new WecubeCoreException(String.format("Doesn't support update of resource type [%s].", item.getType()));
            }
        }
    }

    public void deleteItems(Iterable<ResourceItem> items) {
        for (ResourceItem item : items) {
            switch (ResourceItemType.fromCode(item.getType())) {
            case S3_BUCKET:
                deleteS3Bucket(item);
                break;
            case MYSQL_DATABASE:
                deleteMysqlDatabase(item);
                break;
            default:
                throw new WecubeCoreException(String.format("Doesn't support deletion of resource type [%s].", item.getType()));
            }
        }
    }

    private void deleteMysqlDatabase(ResourceItem item) {
        if (item.getResourceServer() != null) {
            try {
                mysqlManagementService.createMysqlClient(item.getResourceServer().getHost(), item.getResourceServer().getPort(), item.getResourceServer().getLoginUsername(), item.getResourceServer().getLoginPassword());
                mysqlManagementService.connect();
                if (mysqlManagementService.hasTables(item.getName())) {
                    throw new WecubeCoreException(String.format("Can not delete database [%s] : Database is not empty.", item.getName()));
                }
                mysqlManagementService.deleteSchema(item.getName());
                mysqlManagementService.disconnect();
            } catch (Exception e) {
                throw new WecubeCoreException(String.format("Failed to delete mysql database [%s] from server [%s].", item.getName(), item.getResourceServer()), e);
            }
        }
    }

    private void deleteS3Bucket(ResourceItem item) {
        AmazonS3 s3Client = createS3Client(item);
        if (s3Client.doesBucketExist(item.getName())) {
            if (!s3Client.listObjects(item.getName()).getObjectSummaries().isEmpty()) {
                throw new WecubeCoreException(String.format("Can not delete bucket [%s] : Bucket have [%s] amount of objects", item.getName(), s3Client.listObjects(item.getName()).getObjectSummaries().size()));
            }
            s3Client.deleteBucket(item.getName());
        } else {
            logger.warn(String.format("To be delete bucket [%s] does not exists.", item.getName()));
        }
    }
}
