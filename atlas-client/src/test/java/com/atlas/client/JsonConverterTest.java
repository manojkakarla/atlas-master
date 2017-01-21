package com.atlas.client;

import io.dropwizard.jackson.Jackson;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class JsonConverterTest {

    private static final String SAMPLE_JSON = "[{\"key\":\"k1\",\"value\":\"v1\"},{\"key\":\"k2\",\"value\":\"v2\"}]";
    private static final TestSample SAMPLE1 = new TestSample("k1", "v1");
    private static final TestSample SAMPLE2 = new TestSample("k2", "v2");
    private JsonConverter testObj = new JsonConverter(Jackson.newObjectMapper());

    @Test
    public void testParseFromJson() throws Exception {
        List<TestSample> list = testObj.parseFromJson(SAMPLE_JSON, List.class, TestSample.class);
        assertThat(list.size()).isEqualTo(2);
        assertThat(list).contains(SAMPLE1, SAMPLE2);
    }

    @Test
    public void testParseEmptyJson() throws Exception {
        String result = testObj.parseFromJson("", String.class);
        assertThat(result).isNull();
    }

    @Test
    public void testConvertToJson() throws Exception {
        String json = testObj.convertToJson(Arrays.asList(SAMPLE1, SAMPLE2));
        assertThat(json).isEqualTo(SAMPLE_JSON);
    }

   @Test
    public void testConvertNullToJson() throws Exception {
        String json = testObj.convertToJson(null);
        assertThat(json).isNull();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class TestSample {

        private String key;
        private String value;
    }
}