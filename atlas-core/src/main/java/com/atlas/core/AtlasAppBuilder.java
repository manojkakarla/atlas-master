package com.atlas.core;

import com.atlas.core.config.InfoConfig;
import com.atlas.core.dw.AtlasApp;
import com.atlas.infrastructure.SpringBundle;
import io.dropwizard.Configuration;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.function.Function;

@Slf4j
public class AtlasAppBuilder<T extends Configuration> {

    private Function<T, Map<String, Object>> beansFunction;
    @Getter
    private Map<String, Object> beans = new HashMap<>();
    @Getter
    private List<Class<?>> springConfigs = new ArrayList<>();
    private String name;

    public AtlasAppBuilder<T> name(String name) {
        this.name = name;
        return this;
    }

    public AtlasAppBuilder withSpring(Class<?>... springConfigs) {
        return withSpring(null, springConfigs);
    }

    public AtlasAppBuilder<T> withSpring(Function<T, Map<String, Object>> beansFunction, Class<?>... springConfigs) {
        this.springConfigs.addAll(Arrays.asList(springConfigs));
        this.beansFunction = beansFunction;
        return this;
    }

    public AtlasAppBuilder<T> withInfoResource() {
        springConfigs.add(InfoConfig.class);
        return this;
    }

    public <A extends AtlasApp<T>> A build(Class<A> appClass) {
        SpringBundle<T> springBundle = new SpringBundle<>(beansFunction, springConfigs.toArray(new Class[springConfigs.size()]));
        springBundle.setBeans(beans);
        try {
            return appClass.getConstructor(String.class, SpringBundle.class).newInstance(name, springBundle);
        } catch (Exception e) {
            log.error("Failed to create app instance with error: " + e.getMessage());
            return buildDefaultApp(name, springBundle);
        }
    }

    protected <A extends AtlasApp<T>> A buildDefaultApp(String name, SpringBundle<T> springBundle) {
        return (A) new AtlasApp(name, springBundle);
    }

    public AtlasApp<T> build() {
        return build(AtlasApp.class);
    }

    public SpringBundle<T> buildConfig() {
        SpringBundle<T> springBundle = new SpringBundle<>(beansFunction, springConfigs.toArray(new Class[springConfigs.size()]));
        springBundle.setBeans(beans);
        return springBundle;
    }


}
