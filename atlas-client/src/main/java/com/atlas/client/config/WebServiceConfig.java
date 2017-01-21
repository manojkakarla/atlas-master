package com.atlas.client.config;

import com.google.common.base.Throwables;

import org.apache.http.client.utils.URIBuilder;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import lombok.Data;

@Data
public class WebServiceConfig {

    private String host;
    private int port;
    private String endpoint;
    private Map<String, String> endpoints = new HashMap<>();
    private int healthcheckPort;
    private String healthcheckPath;

    public String buildUri() {
        return buildUriForPath(endpoint);
    }

    public String buildUri(String key) {
        String path = endpoints.get(key);
        if(path == null) {
            throw new IllegalArgumentException("cannot find path for key: " + key);
        }
        return buildUriForPath(path);
    }

    private String buildUriForPath(String path) {
        return construct(port, path);
    }

    public String buildHealthcheckUri() {
        return construct(healthcheckPort, healthcheckPath);
    }

    private String construct(int port, String path) {
        try {
            return new URIBuilder(host).setPort(port).setPath(path).build().toString();
        } catch (URISyntaxException e) {
            throw Throwables.propagate(e);
        }
    }
}
