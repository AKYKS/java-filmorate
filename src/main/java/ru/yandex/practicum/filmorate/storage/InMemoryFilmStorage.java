package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.expection.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.expection.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;

@Component
public class InMemoryFilmStorage implements FilmStorage {

    @Override
    public Collection<Film> findAll() {
        return films.values();
    }

    public Film findCurrentFilm(long id) {
        return films.get(id);
    }

    @Override
    public Film create(Film film) {

        // проверяем выполнение необходимых условий
        checkFilmData(film);
        // формируем дополнительные данные
        film.setId(getNextId());
        // сохраняем новую публикацию в памяти приложения
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Film update(Film newFilm) {
        // проверяем необходимые условия
        if (newFilm.getId() == null) {
            throw new ConditionsNotMetException("Id должен быть указан");
        }
        if (films.containsKey(newFilm.getId())) {
            Film oldFilm = films.get(newFilm.getId());
            if (newFilm.getName() != null) {
                oldFilm.setName(newFilm.getName());
            }
            if (newFilm.getDescription() != null) {
                oldFilm.setDescription(newFilm.getDescription());
            }
            if (newFilm.getDuration() != oldFilm.getDuration()) {
                oldFilm.setDuration(newFilm.getDuration());
            }
            if (newFilm.getReleaseDate() != null) {
                oldFilm.setReleaseDate(newFilm.getReleaseDate());
            }
            // если публикация найдена и все условия соблюдены, обновляем её содержимое
            return oldFilm;
        }
        throw new NotFoundException("Фильм с id = " + newFilm.getId() + " не найден");
    }
}
