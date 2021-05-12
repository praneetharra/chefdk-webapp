package com.cerner.hdxts.chefdk.webapp.storage;

import java.nio.file.Path;
import java.util.stream.Stream;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface StorageService {

    void init();

    void storeAuthFile(MultipartFile file);

    void storeSecretFile(MultipartFile file);

    void storeJSONFile(MultipartFile file);

    Stream<Path> loadAll();

    Path loadAuth(String filename);

    Path loadSecrets(String filename);

    Path loadJSON(String filename);

    Resource loadAuthAsResource(String filename);

    Resource loadSecretAsResource(String filename);

    Resource loadJSONAsResource(String filename);

    void deleteAll();

}
