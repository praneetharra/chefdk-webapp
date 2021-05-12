package com.cerner.hdxts.chefdk.webapp.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("storage")
public class StorageProperties {

    /**
     * Folder location for storing files
     */
    private String authFileLocation = "/etc/chef";

    private String secretFileLocation = "/etc/secrets";

    private String JSONLocation = "/etc/json";

    public String getAuthFileLocation() {
        return authFileLocation;
    }

    public void setAuthFileLocation(final String location) {
        this.authFileLocation = location;
    }

    public String getSecretFileLocation() {
        return secretFileLocation;
    }

    public void setSecretFileLocation(final String secretFileLocation) {
        this.secretFileLocation = secretFileLocation;
    }

    public String getJSONLocation() {
        return JSONLocation;
    }

    public void setJSONLocation(final String JSONLocation) {
        this.JSONLocation = JSONLocation;
    }

}
