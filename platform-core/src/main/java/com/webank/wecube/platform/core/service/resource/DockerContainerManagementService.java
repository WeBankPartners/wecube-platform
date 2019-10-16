package com.webank.wecube.platform.core.service.resource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Ports;
import com.github.dockerjava.api.model.Volume;
import com.github.dockerjava.core.DockerClientBuilder;
import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.domain.ResourceItem;
import com.webank.wecube.platform.core.utils.JsonUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class DockerContainerManagementService implements ResourceItemService, ResourceItemOperationService {

    public DockerClient newDockerClient(String host, String port) {
        String url = String.format("tcp://%s:%s", host, port);
        return DockerClientBuilder.getInstance(url).build();
    }

    public List<Container> listRunningContainers(DockerClient client) {
        return client.listContainersCmd().exec();
    }

    @Override
    public ResourceItem createItem(ResourceItem item) {
        Map<String, String> additionalProperties = item.getAdditionalPropertiesMap();
        DockerClient dockerClient = newDockerClient(item.getResourceServer().getHost(), item.getResourceServer().getPort());

        String containerName = item.getName();
        String imageName = additionalProperties.get("imageName");
        List<String> portBindings = Arrays.asList(additionalProperties.get("portBindings").split(","));
        List<String> volumeBindings = Arrays.asList(additionalProperties.get("volumeBindings").split(","));

        List<Container> containers = dockerClient.listContainersCmd().withShowAll(true).withFilter("name", Arrays.asList(containerName)).exec();
        if (!containers.isEmpty()) {
            throw new WecubeCoreException(String.format("Failed to create the container with name [%s] : Already exists.", containerName));
        }

        Ports portMappings = new Ports();
        if (portBindings != null && !portBindings.isEmpty()) {
            portBindings.forEach(port -> {
                String[] portArray = port.split(":");
                ExposedPort containerPort = ExposedPort.tcp(Integer.valueOf(portArray[1]));
                portMappings.bind(containerPort, Ports.Binding.bindPort(Integer.valueOf(portArray[0])));
            });
        }

        List<Bind> volumeMappings = new ArrayList<>();
        List<Volume> containerVolumes = new ArrayList<>();
        if (volumeBindings != null && !volumeBindings.isEmpty()) {
            volumeBindings.forEach(volume -> {
                String[] volumeArray = volume.split(":");
                Volume containerVolume = new Volume(volumeArray[1]);
                containerVolumes.add(containerVolume);
                Bind bind = new Bind(volumeArray[0], containerVolume);
                volumeMappings.add(bind);
            });
        }

        String containerId = dockerClient.createContainerCmd(imageName)
                .withName(containerName)
                .withVolumes(containerVolumes)
                .withHostConfig(new HostConfig().withPortBindings(portMappings)
                        .withBinds(volumeMappings))
                .exec()
                .getId();
        additionalProperties.put("containerId", containerId);
        item.setAdditionalProperties(JsonUtils.toJsonString(additionalProperties));
        return item;
    }

    @Override
    public void deleteItem(ResourceItem item) {
        String containerName = item.getName();
        DockerClient dockerClient = newDockerClient(item.getResourceServer().getHost(), item.getResourceServer().getPort());

        List<Container> containers = dockerClient.listContainersCmd().withShowAll(true).withFilter("name", Arrays.asList(containerName)).exec();
        if (containers.isEmpty()) {
            log.warn("The container {} to be deleted is not existed.", containerName);
            return;
        }

        Container container = containers.get(0);
        if (!container.getState().equals("running")) {
            dockerClient.removeContainerCmd(containerName).exec();
        } else {
            throw new WecubeCoreException(String.format("Failed to delete container with name [%s] : Container still running, please stop first.", containerName));
        }
    }

    @Override
    public void startItem(ResourceItem item) {
        String containerName = item.getName();
        DockerClient dockerClient = newDockerClient(item.getResourceServer().getHost(), item.getResourceServer().getPort());
        List<Container> containers = dockerClient.listContainersCmd().withShowAll(true).withFilter("name", Arrays.asList(containerName)).exec();
        if (containers.isEmpty()) {
            throw new WecubeCoreException(String.format("Failed to start container with name [%s] : Container is not exists.", containerName));
        }

        Container container = containers.get(0);
        if (!container.getState().equals("running")) {
            dockerClient.startContainerCmd(containerName).exec();
        } else {
            log.warn("The container {} is already running.", containerName);
        }
    }

    @Override
    public void stopItem(ResourceItem item) {
        String containerName = item.getName();
        DockerClient dockerClient = newDockerClient(item.getResourceServer().getHost(), item.getResourceServer().getPort());
        List<Container> containers = dockerClient.listContainersCmd().withShowAll(true).withFilter("name", Arrays.asList(containerName)).exec();
        if (containers.isEmpty()) {
            throw new WecubeCoreException(String.format("Failed to start container with name [%s] : Container is not exists.", containerName));
        }

        Container container = containers.get(0);
        if (container.getState().equals("running")) {
            dockerClient.stopContainerCmd(containerName).exec();
        } else {
            log.warn("The container {} is already stopped.", containerName);
        }
    }

    @Override
    public ResourceItem retrieveItem(ResourceItem item) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ResourceItem updateItem(ResourceItem item) {
        switch (ResourceAvaliableStatus.fromCode(item.getStatus())) {
        case RUNNING:
            startItem(item);
            break;
        case STOPPED:
            stopItem(item);
            break;
        default:
            break;
        }
        return item;
    }

}
