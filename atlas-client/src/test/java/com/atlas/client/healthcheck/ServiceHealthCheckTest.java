package com.atlas.client.healthcheck;

import com.codahale.metrics.health.HealthCheck;
import com.atlas.client.HttpClient;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.message.BasicStatusLine;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ServiceHealthCheckTest {

    private static final String ENDPOINT = "endpoint";
    private static final BasicStatusLine SUCCESS = new BasicStatusLine(new ProtocolVersion("http", 1, 1), 200, "OK");
    @Mock
    private HttpClient httpClient;
    @Mock
    private HttpResponse response;
    private ServiceHealthCheck testObj;

    @Before
    public void setUp() throws Exception {
        when(httpClient.getEntity(ENDPOINT, null, null, HttpResponse.class)).thenReturn(response);
        testObj = new ServiceHealthCheck(httpClient, ENDPOINT);
    }

    @Test
    public void testCheck() throws Exception {
        when(response.getStatusLine()).thenReturn(SUCCESS);

        HealthCheck.Result result = testObj.check();
        assertThat(result.isHealthy()).isTrue();
    }

    @Test
    public void testCheckUnhealthy() throws Exception {
        BasicStatusLine error = new BasicStatusLine(new ProtocolVersion("http", 1, 1), 500, "Test Error");
        when(response.getStatusLine()).thenReturn(error);

        HealthCheck.Result result = testObj.check();
        assertThat(result.isHealthy()).isFalse();
    }
}