package com.webank.wecube.core.service.resource;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.webank.wecube.core.commons.WecubeCoreException;
import com.webank.wecube.core.domain.ResourceItem;

import lombok.extern.slf4j.Slf4j;

@Service
public class ResourceImplementationService {

    @Autowired
    private MysqlManagementService mysqlManagementService;

    @Autowired
    private S3ManagementService s3ManagementService;

    public void createItems(Iterable<ResourceItem> items) {
        for (ResourceItem item : items) {
            if (item.getResourceServer() == null) {
                throw new WecubeCoreException(String.format("Failed to create resource item [%s] as resource server is missing.", item));
            }

            switch (ResourceItemType.fromCode(item.getType())) {
            case S3_BUCKET:
                createS3Bucket(item);
                break;
            case MYSQL_DATABASE:
                createMySQLDatabaseWithAccount(item);
                break;
            default:
                throw new WecubeCoreException(String.format("Doesn't support creation of resource type [%s].", item.getType()));
            }
        }
    }

    private void createMySQLDatabaseWithAccount(ResourceItem item) {
        try {
            mysqlManagementService.newMysqlClient(
                    item.getResourceServer().getHost(),
                    item.getResourceServer().getPort(),
                    item.getResourceServer().getLoginUsername(),
                    item.getResourceServer().getLoginPassword());
            mysqlManagementService.connect();
            mysqlManagementService.createSchema(item.getName());
            Map<String, String> additionalProperties = item.getAdditionalPropertiesMap();
            if (additionalProperties.get("username") != null && additionalProperties.get("password") != null) {
                mysqlManagementService.createAccount(additionalProperties.get("username"), additionalProperties.get("password"), item.getName());
            }
            mysqlManagementService.disconnect();
        } catch (Exception e) {
            throw new WecubeCoreException(String.format("Failed to create mysql database [%s] from server [%s].", item.getName(), item.getResourceServer()), e);
        }
    }

    public void createS3Bucket(ResourceItem item) {
        s3ManagementService.newS3Client(
                item.getResourceServer().getHost(),
                item.getResourceServer().getPort(),
                item.getResourceServer().getLoginUsername(),
                item.getResourceServer().getLoginPassword())
                .createBucket(item.getName());
    }

    public void updateItems(Iterable<ResourceItem> items) {
        for (ResourceItem item : items) {
            if (item.getResourceServer() == null) {
                throw new WecubeCoreException(String.format("Failed to update resource item [%s] as resource server is missing.", item));
            }
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
            if (item.getResourceServer() == null) {
                throw new WecubeCoreException(String.format("Failed to delete resource item [%s] as resource server is missing.", item));
            }
            switch (ResourceItemType.fromCode(item.getType())) {
            case S3_BUCKET:
                deleteS3Bucket(item);
                break;
            case MYSQL_DATABASE:
                deleteMysqlDatabaseWithAccount(item);
                break;
            default:
                throw new WecubeCoreException(String.format("Doesn't support deletion of resource type [%s].", item.getType()));
            }
        }
    }

    private void deleteMysqlDatabaseWithAccount(ResourceItem item) {
        try {
            mysqlManagementService.newMysqlClient(
                    item.getResourceServer().getHost(),
                    item.getResourceServer().getPort(),
                    item.getResourceServer().getLoginUsername(),
                    item.getResourceServer().getLoginPassword());
            mysqlManagementService.connect();
            if (mysqlManagementService.hasTables(item.getName())) {
                throw new WecubeCoreException(String.format("Can not delete database [%s] : Database is not empty.", item.getName()));
            }
            Map<String, String> additionalProperties = item.getAdditionalPropertiesMap();
            if (additionalProperties.get("username") != null) {
                mysqlManagementService.deleteAccount(additionalProperties.get("username"));
            }
            mysqlManagementService.deleteSchema(item.getName());
            mysqlManagementService.disconnect();
        } catch (Exception e) {
            throw new WecubeCoreException(String.format("Failed to delete mysql database [%s] from server [%s].", item.getName(), item.getResourceServer()), e);
        }
    }

    private void deleteS3Bucket(ResourceItem item) {
        s3ManagementService.newS3Client(
                item.getResourceServer().getHost(),
                item.getResourceServer().getPort(),
                item.getResourceServer().getLoginUsername(),
                item.getResourceServer().getLoginPassword())
                .deleteBucket(item.getName());
    }
}
