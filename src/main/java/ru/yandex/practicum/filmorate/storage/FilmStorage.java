package ru.yandex.practicum.filmorate.storage;

import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.filmorate.expection.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.expection.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public interface FilmStorage {
    final Map<Long, Film> films = new HashMap<>();

    public Collection<Film> findAll();

    public Film create(@RequestBody Film film);

    default long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    public Film update(@RequestBody Film newFilm);

    default void checkFilmData(Film film) {
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

    default void clear() {
        films.clear();
    }
}
