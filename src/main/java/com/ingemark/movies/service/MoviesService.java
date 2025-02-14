package com.ingemark.movies.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.ingemark.movies.config.ComaBigDecimalDeserializer;
import com.ingemark.movies.model.dto.MovieDto;
import com.ingemark.movies.model.exception.ServerError;
import com.ingemark.movies.repository.MovieRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.*;

@Service
public class MoviesService {

    private final String hnbUrl;
    private final MovieRepository movieRepository;
    private final RestClient restClient;

    public MoviesService(@Value("${hnb.url}") String hnbUrl, MovieRepository movieRepository, RestClient restClient) {
        this.hnbUrl = hnbUrl;
        this.movieRepository = movieRepository;
        this.restClient = restClient;
    }

    public Optional<MovieDto> getMovie(UUID movieId) {
        return movieRepository.findById(movieId)
                .map(it -> MovieDto.from(it, getExchangeRate().multiply(it.getPrice())));
    }

    public List<MovieDto> getMovies() {
        var exchangeRate = getExchangeRate();
        return movieRepository.findAll()
                .stream()
                .map(it -> MovieDto.from(it, exchangeRate.multiply(it.getPrice())))
                .toList();
    }

    public MovieDto createMovie(MovieDto movieDto) {
        var movie = movieRepository.save(movieDto.toMovie());
        return MovieDto.from(movie, getExchangeRate().multiply(movie.getPrice()));
    }

    private BigDecimal getExchangeRate() {
        var typeReference = new ParameterizedTypeReference<List<ExchangeRate>>() {
        };

        try {
            return Objects.requireNonNull(restClient.get()
                            .uri(hnbUrl + "/tecajn-eur/v3?valuta=USD")
                            .retrieve()
                            .body(typeReference))
                    .getFirst()
                    .srednjiTecaj();
        } catch (Exception e) {
            throw new ServerError("Unable to reach HNB api", e);
        }
    }

    private record ExchangeRate(
            @JsonProperty("srednji_tecaj") @JsonDeserialize(using = ComaBigDecimalDeserializer.class) BigDecimal srednjiTecaj
    ) {
    }
}
