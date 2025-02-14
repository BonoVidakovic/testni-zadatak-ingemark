package com.ingemark.movies.integration;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.ingemark.movies.integration.configuration.ContainersConfig;
import com.ingemark.movies.model.Movie;
import com.ingemark.movies.model.dto.MovieDto;
import com.ingemark.movies.repository.MovieRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
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
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static io.restassured.RestAssured.given;
import static io.restassured.config.JsonConfig.jsonConfig;
import static io.restassured.config.RestAssuredConfig.newConfig;
import static org.hamcrest.Matchers.comparesEqualTo;
import static org.hamcrest.Matchers.equalTo;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"test", "integration"})
@EnableWireMock
@Testcontainers
@Import(ContainersConfig.class)
public class GetMovieByIdIT {

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
    public void shouldFindMovieById() {
        var movie = new Movie();
        movie.setCode("1234567890");
        movie.setName("Movie 1");
        movie.setPrice(BigDecimal.valueOf(10.1));
        movie.setIsAvailable(true);

        movieRepository.save(movie);

        given().port(port)
                .when().accept(ContentType.JSON).get("/movies/" + movie.getId())
                .then()
                .assertThat()
                .statusCode(200)
                .body("id", equalTo(movie.getId().toString()))
                .body("code", equalTo(movie.getCode()))
                .body("name", equalTo(movie.getName()))
                .body("price_eur", comparesEqualTo(movie.getPrice()))
                .body("price_usd", comparesEqualTo(movie.getPrice().multiply(BigDecimal.valueOf(1.039000))));
    }

    @Test
    public void shouldReturnNotFound() {
        given().port(port)
                .when()
                .get("/movies/" + UUID.randomUUID())
                .then()
                .assertThat()
                .statusCode(404);
    }

    @Test
    public void shouldReturnBadRequest() {
        given().port(port)
                .when()
                .get("/movies/1234567890")
                .then()
                .assertThat()
                .statusCode(400);
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
