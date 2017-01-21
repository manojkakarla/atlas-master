package com.atlas.client.config;

import com.google.common.collect.ImmutableMap;

import org.junit.Test;

import java.net.URISyntaxException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class WebServiceConfigTest {
    private WebServiceConfig testObj = new WebServiceConfig();
    {
        testObj.setHost("http://localhost");
        testObj.setPort(8080);
        testObj.setEndpoint("/test_ep");
        testObj.setEndpoints(ImmutableMap.of("k1", "/ep1", "k2", "/ep2", "k3", "err"));
        testObj.setHealthcheckPort(8081);
        testObj.setHealthcheckPath("/ping");
    }

    @Test
    public void testBuildUri() throws Exception {
        String uri = testObj.buildUri();
        assertThat(uri, is("http://localhost:8080/test_ep"));
    }

    @Test
    public void testBuildUriFailure() throws Exception {
        WebServiceConfig config = new WebServiceConfig();
        config.setHost("http:\\localhost");
        config.setEndpoint("endpoint");

        assertThatThrownBy(config::buildUri)
            .isInstanceOf(RuntimeException.class)
            .hasCauseInstanceOf(URISyntaxException.class);
    }

    @Test
    public void testBuildUriForPath() throws Exception {
        String uri = testObj.buildUri("k2");
        assertThat(uri, is("http://localhost:8080/ep2"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuildUriForNonExistingPath() throws Exception {
        testObj.buildUri("k5");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuildUriForNull() throws Exception {
        testObj.buildUri(null);
    }

    @Test
    public void testBuildHealthcheckUri() throws Exception {
        String uri = testObj.buildHealthcheckUri();
        assertThat(uri, is("http://localhost:8081/ping"));
    }
}