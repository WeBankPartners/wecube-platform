package com.webank.wecube.core.service.resource;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.api.model.Ports;
import com.github.dockerjava.api.model.Volume;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.command.BuildImageResultCallback;
import com.github.dockerjava.core.command.PullImageResultCallback;
import com.github.dockerjava.core.command.PushImageResultCallback;
import com.webank.wecube.core.commons.ApplicationProperties.ResourceProperties;
import com.webank.wecube.core.commons.WecubeCoreException;

@Service
public class DockerManagementService {

    @Autowired
    private ResourceProperties resourceProperties;

    private DockerClient dockerClient;

    public DockerManagementService newDockerClient(String host, String port) {
        String url = String.format("tcp://%s:%s", host, port);
        dockerClient = DockerClientBuilder.getInstance(url).build();
        return this;
    }

    public List<Container> listRunningContainers(DockerClient client) {
        return client.listContainersCmd().exec();
    }

    public String createContainer(String containerName, String imageName) {
        List<Container> containers = dockerClient.listContainersCmd().withShowAll(true).withFilter("name", Arrays.asList(containerName)).exec();
        if (!containers.isEmpty()) {
            throw new WecubeCoreException(String.format("Failed to create the container with name [%s] : Already exists.", containerName));
        }

        return dockerClient.createContainerCmd(imageName)
                .withName(containerName)
                .exec()
                .getId();
    }

    public String createContainerWithBindings(String containerName, String imageName, List<String> portBindings, List<String> volumeBindings) {
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

        return dockerClient.createContainerCmd(imageName)
                .withName(containerName)
                .withVolumes(containerVolumes)
                .withHostConfig(new HostConfig().withPortBindings(portMappings)
                        .withBinds(volumeMappings))
                .exec()
                .getId();
    }

    public void deleteContainer(String containerName) {
        List<Container> containers = dockerClient.listContainersCmd().withShowAll(true).withFilter("name", Arrays.asList(containerName)).exec();
        if (containers.isEmpty()) {
            throw new WecubeCoreException(String.format("Failed to delete container with name [%s] : Container is not exists.", containerName));
        }

        Container container = containers.get(0);
        if (!container.getState().equals("running")) {
            dockerClient.removeContainerCmd(containerName).exec();
        } else {
            throw new WecubeCoreException(String.format("Failed to delete container with name [%s] : Container still running, please stop first.", containerName));
        }
    }

    public void startContainer(String containerName) {
        List<Container> containers = dockerClient.listContainersCmd().withShowAll(true).withFilter("name", Arrays.asList(containerName)).exec();
        if (containers.isEmpty()) {
            throw new WecubeCoreException(String.format("Failed to start container with name [%s] : Container is not exists.", containerName));
        }

        Container container = containers.get(0);
        if (!container.getState().equals("running")) {
            dockerClient.startContainerCmd(containerName).exec();
        }
    }

    public void stopContainer(String containerName) {
        List<Container> containers = dockerClient.listContainersCmd().withShowAll(true).withFilter("name", Arrays.asList(containerName)).exec();
        if (containers.isEmpty()) {
            throw new WecubeCoreException(String.format("Failed to start container with name [%s] : Container is not exists.", containerName));
        }

        Container container = containers.get(0);
        if (container.getState().equals("running")) {
            dockerClient.stopContainerCmd(containerName).exec();
        }
    }

    public void createImageFromDockerFileWithTag(String dockerFileContent, String imageTag) {
        Set<String> imageTags = new HashSet<>();
        imageTags.add(imageTag);

        File dockerFile = null;
        try {
            dockerFile = File.createTempFile("temp_docker_file", ".txt");
            dockerFile.deleteOnExit();
            BufferedWriter out = new BufferedWriter(new FileWriter(dockerFile));
            out.write(dockerFileContent);
            out.close();
        } catch (IOException e) {
            throw new WecubeCoreException(String.format("Failed to create temp docker file of content [%s]", dockerFileContent));
        }

        dockerClient.buildImageCmd()
                .withDockerfile(dockerFile)
                .withPull(true)
                .withTags(imageTags)
                .exec(new BuildImageResultCallback())
                .awaitImageId();
    }

    public void pullImage(String repository, String tag) {
        try {
            dockerClient.pullImageCmd(repository)
                    .withTag(tag)
                    .exec(new PullImageResultCallback())
                    .awaitCompletion(resourceProperties.getDockerPullImageTimeout(), TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new WecubeCoreException(String.format("Failed to pull repository [%s] with tag [%s].", repository, tag));
        }
    }

    public void pushImage(String repository, String tag) {
        try {
            dockerClient.pushImageCmd(repository)
                    .withTag(tag)
                    .exec(new PushImageResultCallback())
                    .awaitCompletion(0, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new WecubeCoreException(String.format("Failed to push repository [%s] with tag [%s].", repository, tag));
        }
    }

    public void deleteImage(String repository, String tag) {
        String imageName = repository + ":" + tag;
        List<Image> images = dockerClient.listImagesCmd().withShowAll(true).withImageNameFilter(imageName).exec();
        if (images.isEmpty()) {
            throw new WecubeCoreException(String.format("Failed to delete image with name [%s] : Image is not exists.", imageName));
        }
        dockerClient.removeImageCmd(imageName).exec();
    }
}
