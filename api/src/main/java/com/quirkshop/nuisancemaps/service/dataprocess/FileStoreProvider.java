package com.quirkshop.nuisancemaps.service.dataprocess;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.stereotype.Component;

/*
 * "wrapper" class so we can use dependency injection otherwise we can't mock
 * static File.getFileStore()
 */
@Component
public class FileStoreProvider {
    public FileStore getFileStore(Path path) throws IOException {
        return Files.getFileStore(path);
    }
}
