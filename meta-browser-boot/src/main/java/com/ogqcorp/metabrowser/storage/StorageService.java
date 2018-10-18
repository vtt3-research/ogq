package com.ogqcorp.metabrowser.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.stream.Stream;

public interface StorageService {
    void init();

    Path store(MultipartFile file);

    void copy(Path from, Path to);

    Stream<Path> loadAll();

    Path load(String filename);

    Resource loadAsResource(String filename);

    Boolean delete(Path path);

    void deleteAll();
}
