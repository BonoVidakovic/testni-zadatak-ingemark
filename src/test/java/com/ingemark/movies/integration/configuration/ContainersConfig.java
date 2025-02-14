package com.ingemark.movies.integration.configuration;

import org.springframework.boot.devtools.restart.RestartScope;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.testcontainers.containers.PostgreSQLContainer;

@TestConfiguration
@Profile("integration")
public class ContainersConfig {

    @Bean
    @ServiceConnection
    @RestartScope
    public PostgreSQLContainer<?> postgres() {
        return new PostgreSQLContainer<>("postgres:17.2");
    }
}
