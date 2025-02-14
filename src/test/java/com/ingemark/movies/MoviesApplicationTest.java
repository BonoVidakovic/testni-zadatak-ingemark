package com.ingemark.movies;

import com.ingemark.movies.integration.configuration.ContainersConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.wiremock.spring.EnableWireMock;

@SpringBootTest
@ActiveProfiles({"test", "integration"})
@Testcontainers
@Import(ContainersConfig.class)
@EnableWireMock
class MoviesApplicationTest {

	@Test
	void contextLoads() {
	}

}
