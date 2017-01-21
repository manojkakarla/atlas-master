package com.atlas.client;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.*;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.ServerErrorException;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Slf4j
public class AsyncClient implements HttpClient {

    static final String CONTENT_TYPE_HEADER = "Content-type";
    static final String ACCEPT_HEADER = "Accept";

    private final CloseableHttpAsyncClient client;

    private final JsonConverter jsonConverter;

    public AsyncClient(CloseableHttpAsyncClient client, JsonConverter jsonConverter) {
        this.client = client;
        this.jsonConverter = jsonConverter;
    }

    public void start() {
        client.start();
        log.info("Http client started.");
    }

    public void close() throws IOException {
        log.info("Shutting down http client !!");
        client.close();
    }

    @Override
    public <T> T getEntity(String endPoint,
                           Map<String, String> queryParams,
                           Map<String, String> headers, Class<T> tClass, Class<?>... types) {

        HttpGet request = new HttpGet(buildUri(endPoint, queryParams));
        setHeaders(request, headers, null, tClass);

        return executeRequest(tClass, request, types);
    }

    @Override
    public <T, S> T putEntity(String endPoint,
                              Map<String, String> queryParams,
                              Map<String, String> headers, S input, Class<T> tClass, Class<?>... types) {

        HttpPut request = new HttpPut(buildUri(endPoint, queryParams));
        setHeaders(request, headers, input, tClass);
        loadEntity(request, input);

        return executeRequest(tClass, request, types);
    }

    @Override
    public <T, S> T postEntity(String endPoint,
                               Map<String, String> queryParams,
                               Map<String, String> headers, S input, Class<T> tClass, Class<?>... types) {

        HttpPost request = new HttpPost(buildUri(endPoint, queryParams));
        setHeaders(request, headers, input, tClass);
        loadEntity(request, input);

        return executeRequest(tClass, request, types);
    }

    private <T, S> void setHeaders(HttpRequest request, Map<String, String> headers, S input, Class<T> outputClass) {

        // if the request is not null and the Content-type header is not set, set it to application/json
        if (input != null && (headers == null || !headers.containsKey(CONTENT_TYPE_HEADER))) {
            request.addHeader(CONTENT_TYPE_HEADER, MediaType.APPLICATION_JSON);
        }

        // if the response type is not String and the Accept header is not set, set it to application/json
        if ((outputClass == null || !outputClass.isAssignableFrom(String.class)) && (headers == null || !headers.containsKey(ACCEPT_HEADER))) {
            request.addHeader(ACCEPT_HEADER, MediaType.APPLICATION_JSON);
        }

        if (headers != null && !headers.isEmpty()) {
            for (Map.Entry<String, String> header : headers.entrySet()) {
                request.addHeader(header.getKey(), header.getValue());
            }
        }

    }

    private <S> void loadEntity(HttpEntityEnclosingRequestBase request, S input) {
        if (input != null) {
            request.setEntity(new StringEntity(jsonConverter.convertToJson(input), Charsets.UTF_8));
        }
    }

    private <T> T executeRequest(Class<T> tClass, HttpUriRequest request, Class<?>... types) {
        try {
            log.info(request.getMethod() + " " + request.getURI());
            HttpResponse response = client.execute(request, null).get();
            if (tClass.isAssignableFrom(HttpResponse.class)) {
                return (T) response;
            }
            return extractResponse(tClass, response, types);
        } catch (InterruptedException | ExecutionException e) {
            log.error(e.getMessage(), e);
            throw new ServerErrorException("Failed to execute request:", 500, e);
        }
    }


    private URI buildUri(String endPoint, Map<String, String> queryParams) {
        try {
            URIBuilder uriBuilder = new URIBuilder(endPoint);
            if (queryParams != null && !queryParams.isEmpty()) {
                queryParams.entrySet().forEach(param -> uriBuilder.addParameter(param.getKey(), param.getValue()));
            }
            return uriBuilder.build();
        } catch (URISyntaxException e) {
            throw new BadRequestException(String.format("Invalid endpoint: [%s] and/or paths: %s", endPoint, queryParams));
        }
    }

    private <T> T extractResponse(Class<T> tClass, HttpResponse response, Class<?>... types) {
        InputStream responseBody = validateResponse(response);

        if (tClass == Void.class) {
            return null;
        }
        if (tClass == null || tClass.isAssignableFrom(InputStream.class)) {
            return (T) responseBody;
        }

        try {
            if (!tClass.isAssignableFrom(String.class)) {
                return jsonConverter.parseFromJson(responseBody, tClass, types);
            } else {
                return (T) IOUtils.toString(responseBody);
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new ServerErrorException("Unable to read response stream", 500, e);
        }
    }

    private InputStream validateResponse(HttpResponse response) throws ServerErrorException {
        try {
            if (response.getStatusLine().getStatusCode() >= 300) {
                throw appropriateException(response);
            }
            return response.getEntity().getContent();

        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new ServerErrorException(e.getMessage(), 500);
        }
    }


    private RuntimeException appropriateException(HttpResponse resp) {
        int status = resp.getStatusLine().getStatusCode();

        String message;
        try {
            InputStream is = resp.getEntity().getContent();
            StringWriter writer = new StringWriter();
            IOUtils.copy(is, writer, Charsets.UTF_8);
            message = writer.toString();
        } catch (IOException e) {
            message = e.getMessage();
        }

        String msg = status + " response: " + message;
        if (log.isDebugEnabled()) {
            log.debug(msg);
        }

        if (status == 400) {
            throw new BadRequestException(message);
        } else if (status == 401) {
            throw new NotAuthorizedException(message);
        } else if (status == 404) {
            throw new NotFoundException(message);
        } else {
            throw new ServerErrorException(String.format("Unexpected status code: %d. Message: %s", status, message), 500);
        }
    }
}
