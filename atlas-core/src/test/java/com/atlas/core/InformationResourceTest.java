package com.atlas.core;

import com.atlas.client.config.ClientConfig;
import com.atlas.core.resource.InformationResource;
import io.dropwizard.testing.junit.ResourceTestRule;
import org.junit.ClassRule;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class InformationResourceTest {

    @ClassRule
    public static final ResourceTestRule resources = ResourceTestRule.builder()
        .addResource(new InformationResource(clientConfig()))
        .build();

    private static ClientConfig clientConfig() {
        return new ClientConfig();
    }

    @Test
    public void testGetVersion() throws Exception {
        assertThat(getResource("/info/version", String.class)).isEqualTo("");
    }

    @Test
    public void testGetProperties() throws Exception {
        assertThat(getResource("/info/properties", ClientConfig.class)).isEqualTo(clientConfig());
    }

    private <T> T getResource(String path, Class<T> aClass) {
        return resources.client().target(path).request().get(aClass);
    }
}