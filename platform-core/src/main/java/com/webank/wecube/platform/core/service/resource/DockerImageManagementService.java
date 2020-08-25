package com.webank.wecube.platform.core.service.resource;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.command.PullImageResultCallback;
import com.webank.wecube.platform.core.commons.ApplicationProperties.ResourceProperties;
import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.domain.ResourceItem;

@Service
public class DockerImageManagementService implements ResourceItemService {
    private static final Logger log = LoggerFactory.getLogger(DockerImageManagementService.class);

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
            throw new WecubeCoreException("3239",String.format("Failed to pull repository [%s] with tag [%s].", repository, tag), e);
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
    public void deleteItem(ResourceItem item) {
        Map<String, String> additionalProperties = item.getAdditionalPropertiesMap();

        String imageName = additionalProperties.get("repository") + ":" + additionalProperties.get("tag");
        DockerClient dockerClient = newDockerClient(item.getResourceServer().getHost(), item.getResourceServer().getPort());
        List<Image> images = dockerClient.listImagesCmd().withShowAll(true).withImageNameFilter(imageName).exec();
        if (images.isEmpty()) {
            log.warn("The image {} to be deleted is not existed.", imageName);
            return;
        }
        dockerClient.removeImageCmd(imageName).exec();
    }

}
