package ru.yandex.practicum.filmorate.storage;

import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.filmorate.model.Film;

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

    default void clear() {
        films.clear();
    }
}
