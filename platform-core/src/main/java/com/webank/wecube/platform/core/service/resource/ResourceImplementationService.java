package com.webank.wecube.platform.core.service.resource;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.entity.plugin.ResourceItem;

@Service
public class ResourceImplementationService {

    @Autowired
    private MysqlDatabaseManagementService mysqlDatabaseManagementService;

    @Autowired
    private S3BucketManagementService s3BucketManagementService;

    @Autowired
    private DockerContainerManagementService dockerContainerManagementService;

    @Autowired
    private DockerImageManagementService dockerImageManagementService;

    private Map<ResourceItemType, ResourceItemService> itemServices = new HashMap<>();

    @PostConstruct
    public void registerService() {
        itemServices.put(ResourceItemType.S3_BUCKET, s3BucketManagementService);
        itemServices.put(ResourceItemType.MYSQL_DATABASE, mysqlDatabaseManagementService);
        itemServices.put(ResourceItemType.DOCKER_CONTAINER, dockerContainerManagementService);
        itemServices.put(ResourceItemType.DOCKER_IMAGE, dockerImageManagementService);
    }

    public void createItems(Iterable<ResourceItem> items) {
        for (ResourceItem item : items) {
            if (item.getResourceServer() == null) {
                throw new WecubeCoreException("3248",
                        String.format("Failed to create resource item [%s] as resource server is missing.", item), item.getId());
            }

            ResourceItemService service = itemServices.get(ResourceItemType.fromCode(item.getType()));
            if (service == null) {
                throw new WecubeCoreException("3249",
                        String.format("No service for the creating of resource type [%s].", item.getType()), item.getType());
            }
            service.createItem(item);
        }
    }

    public void updateItems(Iterable<ResourceItem> items) {
        for (ResourceItem item : items) {
            if (item.getResourceServer() == null) {
                throw new WecubeCoreException("3250",
                        String.format("Failed to update resource item [%s] as resource server is missing.", item));
            }

            ResourceItemService service = itemServices.get(ResourceItemType.fromCode(item.getType()));
            if (service == null) {
                throw new WecubeCoreException("3251",
                        String.format("No service for the updating of resource type [%s].", item.getType()), item.getType());
            }
            service.updateItem(item);
        }
    }

    public void deleteItems(Iterable<ResourceItem> items) {
        for (ResourceItem item : items) {
            if (item.getResourceServer() == null) {
                throw new WecubeCoreException("3252",String.format("Failed to update resource item [%s] as resource server is missing.", item), item.getId());
            }

            ResourceItemService service = itemServices.get(ResourceItemType.fromCode(item.getType()));
            if (service == null) {
                throw new WecubeCoreException("3253",String.format("No service for the deleting of resource type [%s].", item.getType()), item.getType());
            }
            service.deleteItem(item);
        }
    }
}
