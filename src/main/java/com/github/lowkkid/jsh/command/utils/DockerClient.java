package com.github.lowkkid.jsh.command.utils;

import java.io.IOException;
import java.util.List;

public interface DockerClient {

    record ContainerInfo(String id, String name, String image, String status) {
    }

    List<ContainerInfo> fetchContainers() throws IOException, InterruptedException;

    Process showLogs(String containerName) throws IOException;

    int stopContainer(String containerName) throws IOException, InterruptedException;
}
