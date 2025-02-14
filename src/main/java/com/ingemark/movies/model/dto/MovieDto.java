package com.ingemark.movies.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ingemark.movies.model.Movie;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class MovieDto {

    public static MovieDto from(Movie movie, BigDecimal priceUsd) {
        return MovieDto.builder()
                .id(movie.getId())
                .code(movie.getCode())
                .name(movie.getName())
                .priceEur(movie.getPrice())
                .priceUsd(priceUsd)
                .isAvailable(movie.getIsAvailable())
                .build();
    }

    public Movie toMovie() {
        var movie = new Movie();
        movie.setCode(code);
        movie.setName(name);
        movie.setPrice(priceEur);
        movie.setIsAvailable(isAvailable);
        return movie;
    }

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private UUID id;

    @NotBlank
    @Size(min = 10, max = 10)
    private String code;

    @NotBlank
    private String name;

    @JsonProperty("price_eur")
    @NotNull
    @Min(0)
    private BigDecimal priceEur;

    @JsonProperty("price_usd")
    private BigDecimal priceUsd;

    @JsonProperty("is_available")
    private Boolean isAvailable;
}
