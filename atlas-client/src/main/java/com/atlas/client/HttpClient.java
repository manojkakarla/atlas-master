package com.atlas.client;

import java.util.Map;

public interface HttpClient {

    <T> T getEntity(String endPoint, Map<String, String> queryParams, Map<String, String> headers, Class<T> tClass, Class<?>... types);

    <T, S> T putEntity(String endPoint, Map<String, String> queryParams, Map<String, String> headers, S input, Class<T> tClass, Class<?>... types);

    <T, S> T postEntity(String endPoint, Map<String, String> queryParams, Map<String, String> headers, S input, Class<T> tClass, Class<?>... types);

}
