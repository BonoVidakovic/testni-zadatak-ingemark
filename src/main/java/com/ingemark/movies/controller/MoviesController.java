package com.ingemark.movies.controller;

import com.ingemark.movies.model.dto.MovieDto;
import com.ingemark.movies.model.exception.ServerError;
import com.ingemark.movies.service.MoviesService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/movies")
@RequiredArgsConstructor
public class MoviesController {

    private final MoviesService moviesService;

    @GetMapping(value = "/{movieId}")
    public ResponseEntity<MovieDto> getMovie(@PathVariable UUID movieId) {
        var movie = moviesService.getMovie(movieId);

        return movie.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<MovieDto>> getAllMovies() {
        return ResponseEntity.ok(moviesService.getMovies());
    }

    @PostMapping
    public ResponseEntity<MovieDto> createMovie(@Valid @RequestBody MovieDto movieDto) {
        try {
            return ResponseEntity.status(201).body(moviesService.createMovie(movieDto));
        } catch (ServerError e) {
            throw e;
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
