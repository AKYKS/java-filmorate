package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.expection.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.expection.NotFoundException;
import ru.yandex.practicum.filmorate.expection.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.time.LocalDate;
import java.util.*;

@Service
public class FilmService {

    private final FilmStorage filmStorage;

    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage) {
        this.filmStorage = filmStorage;
    }

    public Film getFilmById(Long id) {
        try {
            return filmStorage.getFilmById(id);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Такого фильма нет в списке!");
        }
    }

    public Film userLikesFilm(Long id, Long userId) {
        return filmStorage.userLikesFilm(id, userId);
    }

    public Film deleteLikesFilm(Long id, Long userId) {
        return filmStorage.deleteLikesFilm(id, userId);
    }

    public Collection<Film> listFirstCountFilm(int count) {
        Collection<Film> films;
        films = sortingToDown().stream()
                .limit(count)
                .toList();
        return films;
    }

    public List<Film> sortingToDown() {
        ArrayList<Film> listFilms = new ArrayList<>(filmStorage.getAllFilms());
        listFilms.sort((Film film1, Film film2) ->
                Integer.compare(film2.getLikes().size(), film1.getLikes().size())
        );
        return listFilms;
    }

    public Collection<Film> getAllFilms() {
        return filmStorage.getAllFilms();
    }

    public Film createFilm(Film film) {
        checkFilmData(film);
        return filmStorage.createFilm(film);
    }

    public Film updateFilm(Film film) {
        return filmStorage.updateFilm(film);
    }

    private void checkFilmData(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            throw new ConditionsNotMetException("название не может быть пустым");
        }
        if (film.getDescription().length() > 200) {
            throw new ValidationException("Максимальная длина описания — 200 символов");
        }
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            throw new ValidationException("Дата релиза — не раньше 28 декабря 1895 года");
        }
        if (film.getDuration() <= 0) {
            throw new ValidationException("Продолжительность фильма должна быть положительным числом.");
        }
    }
}
