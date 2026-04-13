package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.expection.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.expection.DuplicatedDataException;
import ru.yandex.practicum.filmorate.expection.NotFoundException;
import ru.yandex.practicum.filmorate.expection.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.InMemoryFilmStorage;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FilmService {
    private static final Logger log = LoggerFactory.getLogger(FilmService.class);
    private final InMemoryFilmStorage inMemoryFilmStorage;
    private final UserService userService;

    public Collection<Film> findAll() {
        return inMemoryFilmStorage.findAll();
    }

    public Film findCurrentFilm(long id) {
        return inMemoryFilmStorage.findCurrentFilm(id);
    }

    public Film create(Film film) {
        checkFilmData(film);
        return inMemoryFilmStorage.create(film);
    }

    public Film update(Film newFilm) {
        // проверяем необходимые условия
        if (newFilm.getId() == null) {
            throw new ConditionsNotMetException("Id должен быть указан");
        }
        if (findCurrentFilm(newFilm.getId()) != null) {
            return inMemoryFilmStorage.update(newFilm);
        }

        throw new NotFoundException("Фильм с id = " + newFilm.getId() + " не найден");
    }

    public Film addLikeFilm(long id, long userId) {
        userService.isPresentUser(userId);
        isPresentFilm(id);

        Film curFilm = inMemoryFilmStorage.findCurrentFilm(id);
        Set<Long> curFilmLikes = curFilm.getUserLikes();

        if (curFilmLikes != null) {
            if (isCurUserLikedFilm(id, userId)) {
                throw new DuplicatedDataException("Вы уже лайкали этот фильм!");
            }
        } else {
            curFilmLikes = new HashSet<>();
        }

        curFilmLikes.add(userId);
        curFilm.setUserLikes(curFilmLikes);

        return curFilm;
    }

    public Film deleteLikeFilm(long id, long userId) {
        userService.isPresentUser(userId);
        isPresentFilm(id);

        Film curFilm = inMemoryFilmStorage.findCurrentFilm(id);
        Set<Long> curFilmLikes = curFilm.getUserLikes();

        if (curFilmLikes != null) {
            if (!isCurUserLikedFilm(id, userId)) {
                throw new NotFoundException("Вы и так не лайкали этот фильм");
            }
        } else {
            throw new NotFoundException("Вы и так не лайкали этот фильм");
        }

        curFilmLikes.remove(userId);
        curFilm.setUserLikes(curFilmLikes);

        return curFilm;
    }

    public Collection<Film> listOfPopularFilms(long count) {
        return inMemoryFilmStorage.findAll().stream()
                .filter(film -> film.getUserLikes() != null)
                .sorted((f1, f2) -> Integer.compare(
                        f2.getUserLikes().size(),
                        f1.getUserLikes().size()
                ))
                .limit(count)
                .collect(Collectors.toList());
    }

    public void clear() {
        inMemoryFilmStorage.clear();
    }

    private void isPresentFilm(long id) {
        if (inMemoryFilmStorage.findCurrentFilm(id) == null) {
            log.warn("Фильм с таким id {} не найден", id);
            throw new NotFoundException("Фильм с таким id не найден");
        }
    }

    private boolean isCurUserLikedFilm(long id, long userId) {
        if (inMemoryFilmStorage.findCurrentFilm(id).getUserLikes().contains(userId)) {
            return true;
        }
        return false;
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
