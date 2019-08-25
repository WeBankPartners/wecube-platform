package com.webank.wecube.core.service.resource;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.command.PullImageResultCallback;
import com.webank.wecube.core.commons.ApplicationProperties.ResourceProperties;
import com.webank.wecube.core.commons.WecubeCoreException;
import com.webank.wecube.core.domain.ResourceItem;

@Service
public class DockerImageManagementService implements ResourceItemService {

    @Autowired
    private ResourceProperties resourceProperties;

    public DockerClient newDockerClient(String host, String port) {
        String url = String.format("tcp://%s:%s", host, port);
        return DockerClientBuilder.getInstance(url).build();
    }

    @Override
    public ResourceItem createItem(ResourceItem item) {
        Map<String, String> additionalProperties = item.getAdditionalPropertiesMap();
        String repository = additionalProperties.get("repository");
        String tag = additionalProperties.get("tag");
        try {
            DockerClient dockerClient = newDockerClient(item.getResourceServer().getHost(), item.getResourceServer().getPort());
            dockerClient.pullImageCmd(repository)
                    .withTag(tag)
                    .exec(new PullImageResultCallback())
                    .awaitCompletion(resourceProperties.getDockerPullImageTimeout(), TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new WecubeCoreException(String.format("Failed to pull repository [%s] with tag [%s].", repository, tag), e);
        }
        return item;
    }

    @Override
    public ResourceItem retrieveItem(ResourceItem item) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ResourceItem updateItem(ResourceItem item) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int deleteItem(ResourceItem item) {
        Map<String, String> additionalProperties = item.getAdditionalPropertiesMap();

        String imageName = additionalProperties.get("repository") + ":" + additionalProperties.get("tag");
        DockerClient dockerClient = newDockerClient(item.getResourceServer().getHost(), item.getResourceServer().getPort());
        List<Image> images = dockerClient.listImagesCmd().withShowAll(true).withImageNameFilter(imageName).exec();
        if (images.isEmpty()) {
            throw new WecubeCoreException(String.format("Failed to delete image with name [%s] : Image is not exists.", imageName));
        }
        dockerClient.removeImageCmd(imageName).exec();
        return 1;
    }

}
