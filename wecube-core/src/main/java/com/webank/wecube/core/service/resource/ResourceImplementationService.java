package com.webank.wecube.core.service.resource;

import java.util.Arrays;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.webank.wecube.core.commons.WecubeCoreException;
import com.webank.wecube.core.domain.ResourceItem;

@Service
public class ResourceImplementationService {

    @Autowired
    private MysqlManagementService mysqlManagementService;

    @Autowired
    private S3ManagementService s3ManagementService;

    @Autowired
    private DockerManagementService dockerManagementService;

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
            case DOCKER_CONTAINER:
                createDockerContainer(item);
                break;
            case DOCKER_IMAGE:
                createDockerImage(item);
                break;
            default:
                throw new WecubeCoreException(String.format("Doesn't support creation of resource type [%s].", item.getType()));
            }
        }
    }

    private void createDockerImage(ResourceItem item) {
        Map<String, String> additionalProperties = item.getAdditionalPropertiesMap();
        dockerManagementService.newDockerClient(item.getResourceServer().getHost(), item.getResourceServer().getPort())
                .pullImage(additionalProperties.get("repository"), additionalProperties.get("tag"));
    }

    private void createDockerContainer(ResourceItem item) {
        Map<String, String> additionalProperties = item.getAdditionalPropertiesMap();
        dockerManagementService.newDockerClient(item.getResourceServer().getHost(), item.getResourceServer().getPort())
                .createContainerWithBindings(item.getName(), additionalProperties.get("imageName"),
                        Arrays.asList(additionalProperties.get("portBindings").split(",")),
                        Arrays.asList(additionalProperties.get("volumeBindings").split(",")));
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
            case DOCKER_CONTAINER:
                updateDockerContainer(item);
                break;
            default:
                throw new WecubeCoreException(String.format("Doesn't support update of resource type [%s].", item.getType()));
            }
        }
    }

    private void updateDockerContainer(ResourceItem item) {
        switch (ResourceAvaliableStatus.fromCode(item.getStatus())) {
        case RUNNING:
            dockerManagementService.startContainer(item.getName());
            break;
        case STOPPED:
            dockerManagementService.stopContainer(item.getName());
            break;
        default:
            break;
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
            case DOCKER_CONTAINER:
                dockerManagementService.deleteContainer(item.getName());
                break;
            case DOCKER_IMAGE:
                deleteDockerImage(item);
                break;
            default:
                throw new WecubeCoreException(String.format("Doesn't support deletion of resource type [%s].", item.getType()));
            }
        }
    }

    private void deleteDockerImage(ResourceItem item) {
        Map<String, String> additionalProperties = item.getAdditionalPropertiesMap();
        dockerManagementService.newDockerClient(item.getResourceServer().getHost(), item.getResourceServer().getPort())
                .deleteImage(additionalProperties.get("repository"), additionalProperties.get("tag"));
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
