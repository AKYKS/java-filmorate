package ru.yandex.practicum.filmorate.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.expection.DuplicatedDataException;
import ru.yandex.practicum.filmorate.expection.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.InMemoryFilmStorage;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FilmService {
    private static final Logger log = LoggerFactory.getLogger(FilmService.class);
    private final InMemoryFilmStorage inMemoryFilmStorage;
    private final UserService userService;

    @Autowired
    public FilmService(InMemoryFilmStorage inMemoryFilmStorage, UserService userService) {
        this.inMemoryFilmStorage = inMemoryFilmStorage;
        this.userService = userService;
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

    public void isPresentFilm(long id) {
        if (inMemoryFilmStorage.findCurrentFilm(id) == null) {
            log.warn("Фильм с таким id {} не найден", id);
            throw new NotFoundException("Фильм с таки id не найден");
        }
    }

    public boolean isCurUserLikedFilm(long id, long userId) {
        if (inMemoryFilmStorage.findCurrentFilm(id).getUserLikes().contains(userId)) {
            return true;
        }
        return false;
    }
}
