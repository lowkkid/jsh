package com.github.lowkkid.jsh.command.utils;

import com.github.lowkkid.jsh.executor.ProcessBuilderFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class DefaultDockerClient implements DockerClient {

    @Override
    public List<ContainerInfo> fetchContainers() throws IOException, InterruptedException {
        ProcessBuilder pb = ProcessBuilderFactory.create(
                "docker", "ps", "--format", "{{.ID}}\t{{.Names}}\t{{.Image}}\t{{.Status}}"
        );
        pb.redirectErrorStream(false);
        Process process = pb.start();

        List<ContainerInfo> containers = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\t", 4);
                if (parts.length == 4) {
                    containers.add(new ContainerInfo(parts[0], parts[1], parts[2], parts[3]));
                }
            }
        }

        String errorOutput = new String(
                process.getErrorStream().readAllBytes(), StandardCharsets.UTF_8
        ).trim();
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new IOException(errorOutput.isEmpty() ? "docker command failed" : errorOutput);
        }
        return containers;
    }

    @Override
    public Process showLogs(String containerName) throws IOException {
        ProcessBuilder pb = ProcessBuilderFactory.create("docker", "logs", "-f", containerName);
        pb.inheritIO();
        return pb.start();
    }

    @Override
    public int stopContainer(String containerName) throws IOException, InterruptedException {
        ProcessBuilder pb = ProcessBuilderFactory.create("docker", "stop", containerName);
        pb.redirectErrorStream(true);
        Process process = pb.start();
        process.getInputStream().readAllBytes();
        return process.waitFor();
    }
}
