package com.atlas.core.dw;

import com.atlas.infrastructure.SpringBundle;
import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Set;

@Slf4j
public class AtlasApp<T extends Configuration> extends Application<T> {

    private final String name;
    private final SpringBundle<T> bundle;
    @Getter
    protected ConfigurableApplicationContext context;

    public AtlasApp(String name, SpringBundle<T> bundle) {
        this.name = name;
        this.bundle = bundle;
    }

    public AtlasApp() {
        this(AtlasApp.class.getName(), null);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void initialize(Bootstrap<T> bootstrap) {
        if (bundle != null) {
            bootstrap.addBundle(bundle);
        }
    }

    @Override
    public void run(T configuration, Environment environment) throws Exception {
        Set<Object> singletons = environment.jersey().getResourceConfig().getSingletons();
        if (singletons.stream().allMatch(this::match)) {
            log.info("No resource found, disabling Jersey");
            environment.jersey().disable();
        }
        if (bundle != null) {
            context = bundle.getContext();
            doAdditionalBindings();
        }
    }

    protected void doAdditionalBindings() {
    }

    private boolean match(Object s) {
        return !s.getClass().getName().startsWith("com.atlas");
    }
}
