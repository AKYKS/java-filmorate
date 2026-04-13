package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
public class FilmController {
    private final FilmService filmService;

    @GetMapping
    public Collection<Film> findAll() {
        return filmService.findAll();
    }

    @GetMapping("/popular")
    public Collection<Film> listOfpopularFilms(@RequestParam(defaultValue = "10") long count) {
        return filmService.listOfPopularFilms(count);
    }

    @GetMapping("/{id}")
    public Film findCurrentFilm(@PathVariable long id) {
        return filmService.findCurrentFilm(id);
    }

    @PostMapping
    public Film create(@RequestBody Film film) {
        return filmService.create(film);
    }

    @PutMapping("/{id}/like/{userId}")
    public Film addLikeFilm(@PathVariable long id, @PathVariable long userId) {
        return filmService.addLikeFilm(id, userId);
    }

    @PutMapping
    public Film update(@RequestBody Film newFilm) {
        return filmService.update(newFilm);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public Film deleteLikeFilm(@PathVariable long id, @PathVariable long userId) {
        return filmService.deleteLikeFilm(id, userId);
    }

    public void clear() {
        filmService.clear();
    }
}
