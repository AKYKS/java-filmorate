package ru.yandex.practicum.filmorate.controller;

import jakarta.websocket.server.PathParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.InMemoryFilmStorage;

import java.util.Collection;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private final InMemoryFilmStorage inMemoryFilmStorage;
    private final FilmService filmService;

    @Autowired
    public FilmController(InMemoryFilmStorage inMemoryFilmStorage, FilmService filmService) {
        this.inMemoryFilmStorage = inMemoryFilmStorage;
        this.filmService = filmService;
    }

    @GetMapping
    public Collection<Film> findAll() {
        return inMemoryFilmStorage.findAll();
    }

    @GetMapping("/popular")
    public Collection<Film> listOfpopularFilms(@RequestParam(defaultValue = "10") long count) {
        return filmService.listOfPopularFilms(count);
    }

    @GetMapping("/{id}")
    public Film findCurrentFilm(@PathVariable long id) {
        return inMemoryFilmStorage.findCurrentFilm(id);
    }

    @PostMapping
    public Film create(@RequestBody Film film) {
        return inMemoryFilmStorage.create(film);
    }

    @PutMapping("/{id}/like/{userId}")
    public Film addFriend(@PathVariable long id, @PathVariable long userId) {
        return filmService.addLikeFilm(id, userId);
    }

    @PutMapping
    public Film update(@RequestBody Film newFilm) {
        return inMemoryFilmStorage.update(newFilm);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public Film deleteFriend(@PathVariable long id, @PathVariable long userId) {
        return filmService.deleteLikeFilm(id, userId);
    }

    public void clear() {
        inMemoryFilmStorage.clear();
    }
}
