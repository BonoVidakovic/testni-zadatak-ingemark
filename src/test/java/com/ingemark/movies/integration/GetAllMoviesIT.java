package com.ingemark.movies.integration;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.ingemark.movies.integration.configuration.ContainersConfig;
import com.ingemark.movies.model.Movie;
import com.ingemark.movies.model.dto.MovieDto;
import com.ingemark.movies.repository.MovieRepository;
import io.restassured.RestAssured;
import io.restassured.path.json.config.JsonPathConfig;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.InjectWireMock;

import java.math.BigDecimal;
import java.util.Arrays;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static io.restassured.RestAssured.given;
import static io.restassured.config.JsonConfig.jsonConfig;
import static io.restassured.config.RestAssuredConfig.newConfig;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"test", "integration"})
@EnableWireMock
@Testcontainers
@Import(ContainersConfig.class)
public class GetAllMoviesIT {

    @LocalServerPort
    private Integer port;

    @Autowired
    private MovieRepository movieRepository;

    @InjectWireMock
    private WireMockServer wireMockServer;

    @BeforeAll
    public static void setup() {
        RestAssured.config = newConfig().jsonConfig(jsonConfig().numberReturnType(JsonPathConfig.NumberReturnType.BIG_DECIMAL));
    }

    @BeforeEach
    public void setupTest() {
        wireMockServer.resetMappings();
        stubFor(get("/tecajn-eur/v3?valuta=USD").willReturn(ok(
                """
                        [{"broj_tecajnice":"32","datum_primjene":"2025-02-14","drzava":"SAD","drzava_iso":"USA","kupovni_tecaj":"1,040600","prodajni_tecaj":"1,037400","sifra_valute":"840","srednji_tecaj":"1,039000","valuta":"USD"}]
                        """)
                .withHeader("Content-Type", "application/json")));
        movieRepository.deleteAll();
    }

    @Test
    public void shouldReturnAllMovies() {
        var movie1 = new Movie();
        movie1.setCode("1234567890");
        movie1.setName("Movie 1");
        movie1.setPrice(BigDecimal.valueOf(10.1));
        movie1.setIsAvailable(true);

        var movie2 = new Movie();
        movie2.setCode("1234567891");
        movie2.setName("Movie 2");
        movie2.setPrice(BigDecimal.valueOf(20.1));
        movie2.setIsAvailable(true);

        var movie3 = new Movie();
        movie3.setCode("1234567892");
        movie3.setName("Movie 3");
        movie3.setPrice(BigDecimal.valueOf(30.1));
        movie3.setIsAvailable(false);

        movieRepository.save(movie1);
        movieRepository.save(movie2);
        movieRepository.save(movie3);

        var movies = given().port(port)
                .when().accept("application/json")
                .get("/movies")
                .getBody()
                .as(MovieDto[].class);

        var allMovies = movieRepository.findAll();
        Arrays.stream(movies)
                .forEach(movieDto -> {
                    assert allMovies.stream().anyMatch(movie -> movieDto.getId().equals(movie.getId()) &&
                            movieDto.getCode().equals(movie.getCode()) &&
                            movieDto.getIsAvailable().equals(movie.getIsAvailable()) &&
                            movieDto.getName().equals(movie.getName()) &&
                            movieDto.getPriceEur().equals(movie.getPrice()) &&
                            movieDto.getPriceUsd().compareTo(movie.getPrice().multiply(BigDecimal.valueOf(1.039000))) == 0
                    );
                });
    }

    @Test
    public void shouldReturn500_whenHNBisDown() {
        wireMockServer.resetMappings();

        given().port(port)
                .body(MovieDto.builder()
                        .code("1234567890")
                        .name("Movie 1")
                        .isAvailable(true)
                        .priceEur(new BigDecimal("12.1"))
                        .build())
                .contentType("application/json")
                .when().accept("application/json")
                .post("/movies")
                .then().assertThat()
                .statusCode(500);
    }

}
