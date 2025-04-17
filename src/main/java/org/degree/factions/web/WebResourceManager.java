package org.degree.factions.web;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;

public class WebResourceManager {

    public void copyResources(String resourcePath, Path targetDir) throws IOException {
        if (!resourcePath.startsWith("/")) {
            resourcePath = "/" + resourcePath;
        }

        URL resourceURL = getClass().getResource(resourcePath);
        if (resourceURL == null) {
            throw new IOException("Resource not found: " + resourcePath);
        }

        try (FileSystem fs = FileSystems.newFileSystem(resourceURL.toURI(), Collections.emptyMap(), getClass().getClassLoader())) {
            Path resource = fs.getPath(resourcePath);
            if (Files.exists(resource)) {
                Files.walkFileTree(resource, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                        Path relativePath = resource.relativize(dir);
                        Path targetPath = targetDir.resolve(relativePath.toString());
                        if (Files.notExists(targetPath)) {
                            Files.createDirectories(targetPath);
                        }
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        Path relativePath = resource.relativize(file);
                        Path targetPath = targetDir.resolve(relativePath.toString());
                        Files.copy(file, targetPath, StandardCopyOption.REPLACE_EXISTING);
                        return FileVisitResult.CONTINUE;
                    }
                });
            } else {
                throw new IOException("Resource bot found: " + resourcePath);
            }
        } catch (URISyntaxException e) {
            throw new IOException("Wrong URI resource: " + resourcePath, e);
        }
    }
}
