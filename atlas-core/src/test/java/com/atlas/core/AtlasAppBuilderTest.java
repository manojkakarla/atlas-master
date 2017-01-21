package com.atlas.core;

import com.atlas.client.JsonConverter;
import com.atlas.client.config.ClientConfig;
import com.atlas.client.config.JsonConfig;
import com.atlas.core.dw.AppConfig;
import com.atlas.core.dw.AtlasApp;
import com.atlas.core.testing.AtlasAppRule;
import com.atlas.infrastructure.SpringBundle;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ParseContext;
import com.jayway.jsonpath.internal.spi.json.JacksonJsonProvider;
import com.jayway.jsonpath.internal.spi.mapper.JacksonMappingProvider;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;

import static org.assertj.core.api.Assertions.assertThat;

//TODO investigate
@Ignore
public class AtlasAppBuilderTest {

    private static final String HEADER = "test";
    private ApplicationContext context;

    private static <T extends AppConfig> SpringBundle<T> buildConfig() {
        return new AtlasAppBuilder<T>()
                .withSpring(TestConfig.class)
                .withInfoResource()
                .buildConfig();
    }


    @ClassRule
    public static final AtlasAppRule<AppConfig> RULE = new AtlasAppRule(AtlasApp.class, buildConfig(), Resources.getResource("sample.yml").getPath());

    @Before
    public void setUp() throws Exception {
        context = ((AtlasApp) RULE.getApplication()).getContext();
    }

    @Test
    public void testBuild() throws Exception {
        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpUriRequest request = new HttpGet("http://localhost:9090/info/properties");

        HttpResponse httpResponse = httpClient.execute(request);
        InputStream stream = httpResponse.getEntity().getContent();
        StringWriter output = new StringWriter();
        IOUtils.copy(stream, output, StandardCharsets.UTF_8);
        ClientConfig clientConfig = readClientConfig(output);

        assertThat(clientConfig.getConnectTimeout()).isEqualTo(12000);
    }

    private static CountDownLatch latch = new CountDownLatch(2);

    private ClientConfig readClientConfig(StringWriter output) {
        ParseContext parseContext = parseContext(JsonConfig.configureMapper());
        return parseContext.parse(output.toString()).read("$.httpClient", ClientConfig.class);
    }

    private ParseContext parseContext(ObjectMapper objectMapper) {
        com.jayway.jsonpath.Configuration config = com.jayway.jsonpath.Configuration
                .builder()
                .mappingProvider(new JacksonMappingProvider(objectMapper))
                .jsonProvider(new JacksonJsonProvider())
                .build();
        return JsonPath.using(config);
    }

    @Configuration
    @Import(JsonConfig.class)
    public static class TestConfig {

        @Autowired
        private JsonConverter jsonConverter;
        @Autowired
        private AppConfig appConfig;
    }
}