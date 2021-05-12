/*
 * Copyright 2016-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cerner.hdxts.chefdk.webapp.storage;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Random;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import com.cerner.hdxts.chefdk.webapp.storage.SecretFileSystemStorageService;
import com.cerner.hdxts.chefdk.webapp.storage.StorageException;
import com.cerner.hdxts.chefdk.webapp.storage.StorageProperties;

/**
 * @author Dave Syer
 *
 */
public class SecretFileSystemStorageServiceTests {

    private final StorageProperties properties = new StorageProperties();
    private SecretFileSystemStorageService service;

    @Before
    public void init() {
        properties.setAuthFileLocation("target/files/" + Math.abs(new Random().nextLong()));
        service = new SecretFileSystemStorageService(properties);
        service.init();
    }

    @Test
    public void loadNonExistent() {
        assertThat(service.loadAuth("foo.txt")).doesNotExist();
    }

    @Test
    public void saveAndLoad() {
        service.storeAuthFile(new MockMultipartFile("foo", "foo.txt", MediaType.TEXT_PLAIN_VALUE,
                "Hello World".getBytes()));
        assertThat(service.loadAuth("foo.txt")).exists();
    }

    @Test(expected = StorageException.class)
    public void saveNotPermitted() {
        service.storeAuthFile(new MockMultipartFile("foo", "../foo.txt",
                MediaType.TEXT_PLAIN_VALUE, "Hello World".getBytes()));
    }

    @Test
    public void savePermitted() {
        service.storeAuthFile(new MockMultipartFile("foo", "bar/../foo.txt",
                MediaType.TEXT_PLAIN_VALUE, "Hello World".getBytes()));
    }

}
