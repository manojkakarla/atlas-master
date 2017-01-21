package com.atlas.infrastructure;

import com.codahale.metrics.health.HealthCheck;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

import java.util.Map;
import java.util.function.Function;

import javax.ws.rs.Path;
import javax.ws.rs.ext.Provider;

import io.dropwizard.Configuration;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.lifecycle.Managed;
import io.dropwizard.servlets.tasks.Task;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SpringBundle<T extends Configuration> implements ConfiguredBundle<T> {

    private final Function<T, Map<String, Object>> beansFunction;
    private final Class<?>[] springConfigs;
    @Getter
    private ConfigurableApplicationContext context;
    @Getter
    @Setter
    private Map<String, Object> beans;

    public SpringBundle(Class<?>... springConfigs) {
        this(null, springConfigs);
    }

    public SpringBundle(Function<T, Map<String, Object>> beansFunction, Class<?>... springConfigs) {
        this.beansFunction = beansFunction;
        this.springConfigs = springConfigs;
    }

    @Override
    public void initialize(Bootstrap<?> bootstrap) {

    }

    @Override
    public void run(T configuration, Environment environment) throws Exception {
        AnnotationConfigApplicationContext parent = new AnnotationConfigApplicationContext();
        parent.setDisplayName("DropWizard");
        parent.refresh();
        ConfigurableListableBeanFactory beanFactory = parent.getBeanFactory();
        beanFactory.registerSingleton("dwConfig", configuration);
        beanFactory.registerSingleton("dwEnv", environment);
        beanFactory.registerSingleton("mapper", environment.getObjectMapper());

        if (beansFunction != null) {
            log.info("Registering beans from application");
            Map<String, Object> beans = beansFunction.apply(configuration);
            if (beans != null) {
                beans.forEach(beanFactory::registerSingleton);
                log.info("Added beans: {}", beans.keySet());
            }
        }

        parent.registerShutdownHook();
        parent.start();

        ConfigurableApplicationContext ctx = buildContext(parent);
        context = ctx;

        Map<String, HealthCheck> healthChecks = ctx.getBeansOfType(HealthCheck.class);
        healthChecks.entrySet().forEach(entry -> environment.healthChecks().register(entry.getKey(), entry.getValue()));
        log.info("Registered HealthChecks: {}", healthChecks.size());

        Map<String, Object> resources = ctx.getBeansWithAnnotation(Path.class);
        resources.entrySet().forEach(entry -> environment.jersey().register(entry.getValue()));
        log.info("Registered Resources: {}", resources.size());

        Map<String, Object> providers = ctx.getBeansWithAnnotation(Provider.class);
        providers.entrySet().forEach(entry -> environment.jersey().register(entry.getValue()));
        log.info("Registered Providers: {}", providers.size());

//        Map<String, InjectableProvider> injProviders = ctx.getBeansOfType(InjectableProvider.class);
//        injProviders.entrySet().forEach(entry -> environment.jersey().register(entry.getValue()));
//        log.info("Registered InjectableProviders: {}", injProviders.size());

        if (resources.isEmpty()) {
            environment.jersey().disable();
            log.info("Disabled jersey as no resources are found");
        }
        Map<String, Managed> managed = ctx.getBeansOfType(Managed.class);
        managed.entrySet().forEach(entry -> environment.lifecycle().manage(entry.getValue()));
        log.info("Registered Managed: {}", managed.size());

        Map<String, Task> tasks = ctx.getBeansOfType(Task.class);
        tasks.entrySet().forEach(entry -> environment.admin().addTask(entry.getValue()));
        log.info("Registered Tasks: {}", tasks.size());

        if (ctx instanceof WebApplicationContext) {
            environment.admin().addServletListeners(new ContextLoaderListener((WebApplicationContext) ctx));
        }
    }


    private ConfigurableApplicationContext buildContext(ApplicationContext parent) {
        AnnotationConfigWebApplicationContext ctx = new AnnotationConfigWebApplicationContext();
        ctx.setParent(parent);
        ctx.register(springConfigs);
        ctx.refresh();
        if (beans != null) {
            beans.forEach(ctx.getBeanFactory()::registerSingleton);
            log.info("Added beans: {}", beans.keySet());
        }
        ctx.registerShutdownHook();
        ctx.start();

        return ctx;
    }
}
