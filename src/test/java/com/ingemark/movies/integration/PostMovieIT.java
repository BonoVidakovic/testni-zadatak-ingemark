package com.ingemark.movies.integration;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.ingemark.movies.integration.configuration.ContainersConfig;
import com.ingemark.movies.model.Movie;
import com.ingemark.movies.model.dto.MovieDto;
import com.ingemark.movies.repository.MovieRepository;
import io.restassured.RestAssured;
import io.restassured.path.json.config.JsonPathConfig;
import org.hamcrest.core.IsEqual;
import org.hamcrest.core.IsNot;
import org.hamcrest.core.IsNull;
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

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static io.restassured.RestAssured.given;
import static io.restassured.config.JsonConfig.jsonConfig;
import static io.restassured.config.RestAssuredConfig.newConfig;
import static org.hamcrest.Matchers.comparesEqualTo;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"test", "integration"})
@EnableWireMock
@Testcontainers
@Import(ContainersConfig.class)
public class PostMovieIT {

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
    public void shouldCreateMovie() {
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
                .statusCode(201)
                .body("code", IsEqual.equalTo("1234567890"))
                .body("name", IsEqual.equalTo("Movie 1"))
                .body("is_available", IsEqual.equalTo(true))
                .body("price_eur", comparesEqualTo(BigDecimal.valueOf(12.1)))
                .body("price_usd", comparesEqualTo(BigDecimal.valueOf(12.1).multiply(BigDecimal.valueOf(1.039000))))
                .body("id", IsNot.not(IsNull.nullValue()));
    }

    @Test
    public void shouldRejectMovie_whenCodeEmpty() {
        given().port(port)
                .body(MovieDto.builder()
                        .name("Movie 1")
                        .isAvailable(true)
                        .priceEur(new BigDecimal("12.1"))
                        .build())
                .contentType("application/json")
                .when().accept("application/json")
                .post("/movies")
                .then().assertThat()
                .statusCode(400);
    }

    @Test
    public void shouldRejectMovie_whenNameEmpty() {
        given().port(port)
                .body(MovieDto.builder()
                        .code("1234567890")
                        .isAvailable(true)
                        .priceEur(new BigDecimal("12.1"))
                        .build())
                .contentType("application/json")
                .when().accept("application/json")
                .post("/movies")
                .then().assertThat()
                .statusCode(400);
    }

    @Test
    public void shouldRejectMovie_whenPriceNull() {
        given().port(port)
                .body(MovieDto.builder()
                        .code("1234567890")
                        .name("Movie 1")
                        .isAvailable(true)
                        .build())
                .contentType("application/json")
                .when().accept("application/json")
                .post("/movies")
                .then().assertThat()
                .statusCode(400);
    }

    @Test
    public void shouldRejectMovie_whenCodeNotUnique() {
        var movie1 = new Movie();
        movie1.setCode("1234567890");
        movie1.setName("Movie 1");
        movie1.setPrice(BigDecimal.valueOf(10.1));
        movie1.setIsAvailable(true);

        movieRepository.save(movie1);

        given().port(port)
                .body(MovieDto.builder()
                        .name("Movie 1")
                        .code("1234567890")
                        .isAvailable(true)
                        .priceEur(new BigDecimal("12.1"))
                        .build())
                .contentType("application/json")
                .when().accept("application/json")
                .post("/movies")
                .then().assertThat()
                .statusCode(400);
    }

    @Test
    public void shouldRejectMovie_whenCodeNotExactly10Digits() {
        given().port(port)
                .body(MovieDto.builder()
                        .name("Movie 1")
                        .code("1234567")
                        .isAvailable(true)
                        .priceEur(new BigDecimal("12.1"))
                        .build())
                .contentType("application/json")
                .when().accept("application/json")
                .post("/movies")
                .then().assertThat()
                .statusCode(400);
    }

    @Test
    public void shouldRejectMovie_whenPriceNotPositive() {
        given().port(port)
                .body(MovieDto.builder()
                        .name("Movie 1")
                        .code("1234567890")
                        .isAvailable(true)
                        .priceEur(new BigDecimal("-0.1"))
                        .build())
                .contentType("application/json")
                .when().accept("application/json")
                .post("/movies")
                .then().assertThat()
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
