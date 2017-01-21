package com.atlas.client.healthcheck;

import com.atlas.client.HttpClient;
import com.codahale.metrics.health.HealthCheck;

import org.apache.http.HttpResponse;


public class ServiceHealthCheck extends HealthCheck {

    private final HttpClient client;
    private final String endpoint;

    public ServiceHealthCheck(HttpClient client, String endpoint) {
        this.endpoint = endpoint;
        this.client = client;
    }

    @Override
    protected Result check() throws Exception {
        HttpResponse response = client.getEntity(endpoint, null, null, HttpResponse.class);
        int status = response.getStatusLine().getStatusCode();
        return status == 200 ?
                Result.healthy() :
                Result.unhealthy(String.format("failed ping: status: %s, reason: %s", status, response.getEntity()));
    }
}
