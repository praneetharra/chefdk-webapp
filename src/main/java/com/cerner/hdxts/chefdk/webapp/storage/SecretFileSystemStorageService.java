package com.cerner.hdxts.chefdk.webapp.storage;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class SecretFileSystemStorageService implements StorageService {

    private final Path authFileLocation;
    private final Path secretFileLocation;
    private final Path JSONLocation;

    @Autowired
    public SecretFileSystemStorageService(final StorageProperties properties) {
        this.authFileLocation = Paths.get(properties.getAuthFileLocation());
        this.secretFileLocation = Paths.get(properties.getSecretFileLocation());
        this.JSONLocation = Paths.get(properties.getJSONLocation());
    }

    @Override
    public void storeAuthFile(final MultipartFile file) {
        final String filename = StringUtils.cleanPath(file.getOriginalFilename());
        try {
            if (file.isEmpty()) {
                throw new StorageException("Failed to store empty file " + filename);
            }
            if (filename.contains("..")) {
                // This is a security check
                throw new StorageException(
                        "Cannot store file with relative path outside current directory "
                                + filename);
            }
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, this.authFileLocation.resolve(filename),
                        StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (final IOException e) {
            throw new StorageException("Failed to store file " + filename, e);
        }
    }

    @Override
    public void storeSecretFile(final MultipartFile file) {
        final String filename = StringUtils.cleanPath(file.getOriginalFilename());
        try {
            if (file.isEmpty()) {
                throw new StorageException("Failed to store empty file " + filename);
            }
            if (filename.contains("..")) {
                // This is a security check
                throw new StorageException(
                        "Cannot store file with relative path outside current directory "
                                + filename);
            }
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, this.secretFileLocation.resolve(filename),
                        StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (final IOException e) {
            throw new StorageException("Failed to store file " + filename, e);
        }
    }

    @Override
    public void storeJSONFile(final MultipartFile file) {
        final String filename = StringUtils.cleanPath(file.getOriginalFilename());
        try {
            if (file.isEmpty()) {
                throw new StorageException("Failed to store empty file " + filename);
            }
            if (filename.contains("..")) {
                // This is a security check
                throw new StorageException(
                        "Cannot store file with relative path outside current directory "
                                + filename);
            }
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, this.JSONLocation.resolve(filename),
                        StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (final IOException e) {
            throw new StorageException("Failed to store file " + filename, e);
        }
    }

    @Override
    public Stream<Path> loadAll() {
        try {
            return Files.walk(this.secretFileLocation, 1)
                    .filter(path -> !path.equals(this.secretFileLocation))
                    .map(this.secretFileLocation::relativize);
        } catch (final IOException e) {
            throw new StorageException("Failed to read stored files", e);
        }

    }

    @Override
    public Path loadAuth(final String filename) {
        return authFileLocation.resolve(filename);
    }

    @Override
    public Path loadSecrets(final String filename) {
        return secretFileLocation.resolve(filename);
    }

    @Override
    public Path loadJSON(final String filename) {
        return JSONLocation.resolve(filename);
    }

    @Override
    public Resource loadAuthAsResource(final String filename) {
        try {
            final Path file = loadAuth(filename);
            final Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new StorageFileNotFoundException(
                        "Could not read file: " + filename);

            }
        } catch (final MalformedURLException e) {
            throw new StorageFileNotFoundException("Could not read file: " + filename, e);
        }
    }

    @Override
    public Resource loadSecretAsResource(final String filename) {
        try {
            final Path file = loadSecrets(filename);
            final Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new StorageFileNotFoundException(
                        "Could not read file: " + filename);

            }
        } catch (final MalformedURLException e) {
            throw new StorageFileNotFoundException("Could not read file: " + filename, e);
        }
    }

    @Override
    public Resource loadJSONAsResource(final String filename) {
        try {
            final Path file = loadJSON(filename);
            final Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new StorageFileNotFoundException(
                        "Could not read file: " + filename);

            }
        } catch (final MalformedURLException e) {
            throw new StorageFileNotFoundException("Could not read file: " + filename, e);
        }
    }

    @Override
    public void deleteAll() {
        FileSystemUtils.deleteRecursively(authFileLocation.toFile());
    }

    @Override
    public void init() {
        try {
            Files.createDirectories(authFileLocation);
            Files.createDirectories(secretFileLocation);
            Files.createDirectories(JSONLocation);
        } catch (final IOException e) {
            throw new StorageException("Could not initialize storage", e);
        }
    }
}
