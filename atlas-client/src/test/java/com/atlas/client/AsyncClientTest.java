package com.atlas.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.message.BasicHttpResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotAuthorizedException;
import java.util.List;

import static com.atlas.client.AsyncClient.ACCEPT_HEADER;
import static com.atlas.client.AsyncClient.CONTENT_TYPE_HEADER;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AsyncClientTest {

    @Mock
    private CloseableHttpAsyncClient client;
    @Mock
    private java.util.concurrent.Future<HttpResponse> mockFuture;

    private JsonConverter jsonConverter = new JsonConverter(new ObjectMapper());
    private AsyncClient testObj;
    private BasicHttpResponse response = new BasicHttpResponse(new ProtocolVersion("http", 1, 1), 200, "OK");

    @Before
    public void setUp() throws Exception {
        response.setEntity(new StringEntity(""));
        when(client.execute(any(HttpUriRequest.class), eq(null))).thenReturn(mockFuture);
        when(mockFuture.get()).thenReturn(response);
        testObj = new AsyncClient(client, jsonConverter);
    }

    @Test
    public void getStringAddsNoHeaders() throws Exception {
        String endPoint = "http://host:port/path";
        testObj.getEntity(endPoint, null, null, String.class);

        ArgumentCaptor<HttpGet> captor = ArgumentCaptor.forClass(HttpGet.class);
        verify(client).execute(captor.capture(), eq(null));
        HttpGet request = captor.getValue();
        assertThat(request.getURI().toString()).isEqualTo(endPoint);
        assertThat(request.getAllHeaders().length).isZero();
    }

    @Test
    public void getObjectAddsHeaders() throws Exception {
        response.setEntity(new StringEntity("[\"one\", \"two\"]"));
        List<String> entity = testObj.getEntity("http://host:port/path", null, null, List.class);

        ArgumentCaptor<HttpGet> captor = ArgumentCaptor.forClass(HttpGet.class);
        verify(client).execute(captor.capture(), eq(null));
        HttpGet request = captor.getValue();
        assertThat(request.getFirstHeader(ACCEPT_HEADER).getValue()).isEqualTo(APPLICATION_JSON);
        assertThat(entity).contains("one", "two");
    }

    @Test(expected = BadRequestException.class)
    public void invalidEndpointThrowsBadRequest() throws Exception {
        testObj.getEntity("http:\\host:port//path", null, null, String.class);

    }

    @Test(expected = BadRequestException.class)
    public void invalidRequestEndpointThrowsBadRequest() throws Exception {
        response.setStatusCode(400);
        testObj.getEntity("http://host:port/path", null, null, String.class);
    }

    @Test(expected = NotAuthorizedException.class)
    public void unauthorisedRequestThrowsValidException() throws Exception {
        response.setStatusCode(401);
        testObj.getEntity("http://host:port/path", null, null, String.class);
    }

    @Test
    public void testPutEntity() throws Exception {

    }

    @Test
    public void testPostEntity() throws Exception {
        testObj.postEntity("http://host:port", null, null, ImmutableMap.of("k1", "v1"), String.class);
        ArgumentCaptor<HttpPost> captor = ArgumentCaptor.forClass(HttpPost.class);
        verify(client).execute(captor.capture(), eq(null));
        HttpPost request = captor.getValue();
        assertThat(request.getFirstHeader(CONTENT_TYPE_HEADER).getValue()).isEqualTo(APPLICATION_JSON);
    }
}