package com.atlas.core.dw;

import com.atlas.client.config.ClientConfig;
import io.dropwizard.Configuration;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = false)
public class AppConfig extends Configuration {

    @Valid
    private Map<String, ?> app = new HashMap<>();
    @Valid
    private ClientConfig httpClient = new ClientConfig();

    public <T> T get(String property) {
        return getProperty(property);
    }

    public int getInt(String property) {
        return getProperty(property);
    }

    public long getLong(String property) {
        return getProperty(property);
    }

    public boolean getBoolean(String property) {
        return getProperty(property);
    }

    private <T> T getProperty(String property) {
        return (T) app.get(property);
    }

}
