package com.atlas.client.config;

import com.atlas.client.AsyncClient;
import com.atlas.client.JsonConverter;
import com.atlas.client.error.ApiExceptionMapper;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(JsonConfig.class)
public class HttpConfig {

    private final ClientConfig clientConfig;

    private final JsonConverter jsonConverter;

    @Autowired
    public HttpConfig(JsonConverter jsonConverter, ClientConfig clientConfig) {
        this.jsonConverter = jsonConverter;
        this.clientConfig = clientConfig;
    }

    @Bean(destroyMethod = "close", initMethod = "start")
    public AsyncClient asyncClient() {
        RequestConfig requestConfig = RequestConfig.custom()
            .setSocketTimeout(clientConfig.getConnectTimeout())
            .setConnectTimeout(clientConfig.getSocketTimeout())
            .build();

        CloseableHttpAsyncClient httpClient = HttpAsyncClients.custom()
            .setDefaultRequestConfig(requestConfig)
            .setMaxConnPerRoute(clientConfig.getMaxConnPerRoute())
            .setMaxConnTotal(clientConfig.getMaxConnTotal())
            .build();
        return new AsyncClient(httpClient, jsonConverter);
    }

    @Bean
    public ApiExceptionMapper apiExceptionMapper() {
        return new ApiExceptionMapper();
    }
}
