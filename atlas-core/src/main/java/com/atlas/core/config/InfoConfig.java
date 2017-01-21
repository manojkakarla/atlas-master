package com.atlas.core.config;

import com.atlas.core.resource.InformationResource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InfoConfig {

    @Bean
    public InformationResource informationResource(io.dropwizard.Configuration env) {
        return new InformationResource(env);
    }

}
