package com.atlas.core.testing;

import com.atlas.infrastructure.SpringBundle;
import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.junit.DropwizardAppRule;

public class AtlasAppRule<C extends Configuration> extends DropwizardAppRule<C> {

    private final Class<? extends Application<C>> appClass;
    private final SpringBundle<C> bundle;

    public AtlasAppRule(Class<? extends Application<C>> applicationClass, String configPath, ConfigOverride... configOverrides) {
        this(applicationClass, null, configPath, configOverrides);
    }

    public AtlasAppRule(Class<? extends Application<C>> applicationClass, SpringBundle<C> springBundle, String configPath, ConfigOverride... configOverrides) {
        super(applicationClass, configPath, configOverrides);
        appClass = applicationClass;
        bundle = springBundle;
    }

    @Override
    public Application<C> newApplication() {
        try {
            return bundle != null ? appClass.getConstructor(SpringBundle.class).newInstance(bundle) : appClass.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
