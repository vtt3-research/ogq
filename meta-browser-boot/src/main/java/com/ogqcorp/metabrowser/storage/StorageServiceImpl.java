package com.ogqcorp.metabrowser.storage;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.stream.Stream;

@Service
public class StorageServiceImpl implements StorageService{

    private final Path rootLocation;


    @Autowired
    public StorageServiceImpl(StorageProperties properties) {

        this.rootLocation = Paths.get(properties.getLocation());
    }

    @Override
    public Path store(MultipartFile file) {
        Path newPath;
        try {
            String newFileName;
            if (file.isEmpty()) {
                throw new StorageException("Failed to store empty file " + file.getOriginalFilename());
            }
            String randString = UUID.randomUUID().toString();
            newFileName = randString+"."+FilenameUtils.getExtension(file.getOriginalFilename());

            newPath = this.rootLocation.resolve(newFileName);
            Files.copy(file.getInputStream(), newPath);
        } catch (IOException e) {
            throw new StorageException("Failed to store file " + file.getOriginalFilename(), e);
        }

        return newPath;
    }

    @Override
    public void copy(Path from, Path to) {

/*        from = this.rootLocation.resolve(from);
        to = this.rootLocation.resolve(to);*/
        try {
            if (!Files.exists(from)) {
                throw new StorageException("Failed to store empty file " + from.getFileName());
            }
            Files.copy(from, to);
        } catch (IOException e) {
            throw new StorageException("Failed to store file " + from.getFileName(), e);
        }

    }

    @Override
    public Stream<Path> loadAll() {
        try {
            return Files.walk(this.rootLocation, 1)
                    .filter(path -> !path.equals(this.rootLocation))
                    .map(path -> this.rootLocation.relativize(path));
        } catch (IOException e) {
            throw new StorageException("Failed to read stored files", e);
        }

    }

    @Override
    public Path load(String filename) {
        return rootLocation.resolve(filename);
    }

    @Override
    public Resource loadAsResource(String filename) {
        try {
            Path file = load(filename);
            Resource resource = new UrlResource(file.toUri());
            if(resource.exists() || resource.isReadable()) {
                return resource;
            }
            else {
                throw new StorageFileNotFoundException("Could not read file: " + filename);

            }
        } catch (MalformedURLException e) {
            throw new StorageFileNotFoundException("Could not read file: " + filename, e);
        }
    }

    @Override
    public Boolean delete(Path path) {

        Boolean result = false;
        try{
            result = Files.deleteIfExists(path);
        }catch (IOException e) {
            throw new StorageFileNotFoundException("Could not read file: " + path.getFileName());
        }

        return result;
    }

    @Override
    public void deleteAll() {
        FileSystemUtils.deleteRecursively(rootLocation.toFile());
    }

    @Override
    public void init() {
        try {
            if(!Files.exists(rootLocation))
                Files.createDirectory(rootLocation);
        } catch (IOException e) {
            throw new StorageException("Could not initialize storage", e);
        }
    }
}
