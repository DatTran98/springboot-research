package com.individual.restapi.config;

import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class OkHttpClientConfig {
    @Bean
    public OkHttpClient okHttpClientFactoryConfig() {
        return new OkHttpClient.Builder().retryOnConnectionFailure(false)
                .connectionPool(new ConnectionPool(200, 2L, TimeUnit.MINUTES)).connectTimeout(30L, TimeUnit.SECONDS)
                .build();
    }
}
