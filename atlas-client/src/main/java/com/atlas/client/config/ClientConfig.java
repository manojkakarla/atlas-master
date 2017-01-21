package com.atlas.client.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@AllArgsConstructor
@EqualsAndHashCode
public class ClientConfig {
    private int socketTimeout;
    private int connectTimeout;
    private int maxConnPerRoute;
    private int maxConnTotal;

    public ClientConfig() {
        socketTimeout = 5000;
        connectTimeout = 5000;
        maxConnPerRoute = 1024;
        maxConnTotal = 1024;
    }
}
