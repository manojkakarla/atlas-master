# Atlas Master

Parent module for Atlas components

This contains 2 modules
* Bundles
* Client

## Atlas Bundles
This contain custom bundles. Currently it has 
* **Spring Bundle**: Initialises spring context and registers beans. Has provision to add custom beans.

### Usage
In Dropwizard application `bootstrap` method,
 ```java
 @Override
    public void initialize(Bootstrap<AppConfig> bootstrap) {
        bootstrap.addBundle(new SpringBundle<>(SpringConfig.class));
    }
```
To add custom beans, pass a function that takes `Configuration` class as argument and return `Map<String, Object>` to the bundle.
 ```java
 @Override
    public void initialize(Bootstrap<AppConfig> bootstrap) {
        Function<AppConfig, Map<String, Object>> beansFunction = appConfig -> ImmutableMap.of(
                "database", appConfig.getDatabase(),
                "httpClient", appConfig.getHttpClient());
        bootstrap.addBundle(new SpringBundle<>(beansFunction, SpringConfig.class));
    }
```

## Atlas Client
This has http client that can be used to call APIs.
> This requires Spring config and passing of httpClient bean.

* Import `HttpConfig` class into your spring config.
* Provides framework to translate HttpResponse into desired object.
* Converts Http errors to Service exceptions.
