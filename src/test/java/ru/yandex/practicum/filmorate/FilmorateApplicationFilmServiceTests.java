package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.storage.dao.UserDbStorage;
import ru.yandex.practicum.filmorate.expection.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.expection.DatabaseException;
import ru.yandex.practicum.filmorate.expection.NotFoundException;
import ru.yandex.practicum.filmorate.expection.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({FilmService.class, UserDbStorage.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class FilmorateApplicationFilmServiceTests {

    @Autowired
    private UserDbStorage userDbStorage;
    @Autowired
    private FilmService filmService;

    private static final LocalDate VALID_RELEASE_DATE = LocalDate.of(1990, 1, 1);

    private Long createTestUser(String email, String login, String name) {
        User user = new User();
        user.setEmail(email);
        user.setLogin(login);
        user.setName(name);
        user.setBirthday(LocalDate.of(1990, 1, 1));

        return userDbStorage.createUser(user).getId();
    }

    // Вспомогательный метод для создания тестового фильма
    private Long createTestFilm() {
        Film film = Film.builder()
                .name("Test Film")
                .description("Test description")
                .releaseDate(VALID_RELEASE_DATE)
                .duration(120)
                .build();
        return filmService.createFilm(film).getId();
    }

    @Test
    public void testGetFilmById_Success() {
        Long filmId = createTestFilm();

        Film result = filmService.getFilmById(filmId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(filmId);
        assertThat(result.getName()).isEqualTo("Test Film");
    }

    @Test
    public void testGetFilmById_NotFound() {
        Long nonExistentId = 999L;

        assertThatThrownBy(() -> filmService.getFilmById(nonExistentId))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Такого фильма не существует!");
    }

    @Test
    public void testUserLikesFilm_Success() {
        Long userId = createTestUser("test@example.com", "Test User", "TestUser");
        Long filmId = createTestFilm();

        Film result = filmService.userLikesFilm(filmId, userId);

        assertThat(result).isNotNull();
        assertThat(result.getLikes()).contains(userId);
    }

    @Test
    public void testUserLikesFilm_UserNotFound() {
        Long filmId = createTestFilm();
        Long nonExistentUserId = 999L;

        assertThatThrownBy(() -> filmService.userLikesFilm(filmId, nonExistentUserId))
                .isInstanceOf(DatabaseException.class)
                .hasMessageContaining("Такого юзера нет в списке!");
    }

    @Test
    public void testDeleteLikesFilm_Success() {
        Long userId = createTestUser("test@example.com", "Test User", "TestUser");
        Long filmId = createTestFilm();
        filmService.userLikesFilm(filmId, userId);

        Film result = filmService.deleteLikesFilm(filmId, userId);

        assertThat(result.getLikes()).doesNotContain(userId);
    }

    @Test
    public void testDeleteLikesFilm_UserNotFound() {
        Long filmId = createTestFilm();
        Long nonExistentUserId = 999L;

        assertThatThrownBy(() -> filmService.deleteLikesFilm(filmId, nonExistentUserId))
                .isInstanceOf(DatabaseException.class)
                .hasMessageContaining("Такого юзера нет в списке!");
    }

    @Test
    public void testListFirstCountFilm_Success() {
        Long user1Id = createTestUser("test@example.com", "Test User", "TestUser");
        Long user2Id = createTestUser("test2@example.com", "Test User2", "TestUser2");

        Long film1Id = createTestFilm(); // 2 лайка
        filmService.userLikesFilm(film1Id, user1Id);
        filmService.userLikesFilm(film1Id, user2Id);

        Long film2Id = createTestFilm(); // 1 лайк
        filmService.userLikesFilm(film2Id, user1Id);

        createTestFilm(); // без лайков

        Collection<Film> topFilms = filmService.listFirstCountFilm(2);

        List<Film> filmList = new ArrayList<>(topFilms);
        assertThat(filmList).hasSize(2);
        assertThat(filmList.get(0).getLikes().size()).isEqualTo(2); // первый — с 2 лайками
        assertThat(filmList.get(1).getLikes().size()).isEqualTo(1); // второй — с 1 лайком
    }

    @Test
    public void testCreateFilm_Success() {
        Film film = Film.builder()
                .name("New Film")
                .description("New description")
                .releaseDate(VALID_RELEASE_DATE)
                .duration(90)
                .build();

        Film createdFilm = filmService.createFilm(film);

        assertThat(createdFilm).isNotNull();
        assertThat(createdFilm.getId()).isNotNull();
        assertThat(createdFilm.getName()).isEqualTo("New Film");
    }

    @Test
    public void testCreateFilm_InvalidName() {
        Film invalidFilm = Film.builder()
                .name("")
                .description("Description")
                .releaseDate(VALID_RELEASE_DATE)
                .duration(90)
                .build();

        assertThatThrownBy(() -> filmService.createFilm(invalidFilm))
                .isInstanceOf(ConditionsNotMetException.class)
                .hasMessageContaining("название не может быть пустым");
    }

    @Test
    public void testCreateFilm_LongDescription() {
        String longDescription = "a".repeat(201);
        Film invalidFilm = Film.builder()
                .name("Film")
                .description(longDescription)
                .releaseDate(VALID_RELEASE_DATE)
                .duration(90)
                .build();

        assertThatThrownBy(() -> filmService.createFilm(invalidFilm))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Максимальная длина описания — 200 символов");
    }

    @Test
    public void testCreateFilm_EarlyReleaseDate() {
        LocalDate earlyDate = LocalDate.of(1890, 1, 1);
        Film invalidFilm = Film.builder()
                .name("Film")
                .description("Description")
                .releaseDate(earlyDate)
                .duration(90)
                .build();

        assertThatThrownBy(() -> filmService.createFilm(invalidFilm))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Дата релиза — не раньше 28 декабря 1895 года");
    }

    @Test
    public void testGetAllFilms_Success() {
        Long film1Id = createTestFilm();
        Long film2Id = createTestFilm();

        Collection<Film> allFilms = filmService.getAllFilms();

        assertThat(allFilms).hasSize(2);
        assertThat(allFilms)
                .extracting(Film::getId)
                .contains(film1Id, film2Id);
    }

    @Test
    public void testSortingToDown_EmptyList() {
        List<Film> sortedFilms = filmService.sortingToDown();

        assertThat(sortedFilms).isEmpty();
    }

    @Test
    public void testSortingToDown_WithLikes() {
        Long user1Id = createTestUser("test@example.com", "Test User", "TestUser");
        Long user2Id = createTestUser("test2@example.com", "Test User2", "TestUser2");

        Long film1Id = createTestFilm(); // 2 лайка
        filmService.userLikesFilm(film1Id, user1Id);
        filmService.userLikesFilm(film1Id, user2Id);

        Long film2Id = createTestFilm(); // 1 лайк
        filmService.userLikesFilm(film2Id, user1Id);

        createTestFilm(); // без лайков

        List<Film> sortedFilms = filmService.sortingToDown();

        assertThat(sortedFilms).hasSize(3);
        assertThat(sortedFilms.get(0).getLikes().size()).isEqualTo(2); // первый — с 2 лайками
        assertThat(sortedFilms.get(1).getLikes().size()).isEqualTo(1); // второй — с 1 лайком
        assertThat(sortedFilms.get(2).getLikes()).isEmpty(); // третий — без лайков
    }

    @Test
    public void testCreateFilm_ZeroDuration() {
        Film invalidFilm = Film.builder()
                .name("Film")
                .description("Description")
                .releaseDate(VALID_RELEASE_DATE)
                .duration(0)
                .build();

        assertThatThrownBy(() -> filmService.createFilm(invalidFilm))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Продолжительность фильма должна быть положительным числом");
    }

    @Test
    public void testUpdateFilm_NotFound() {
        Film nonExistentFilm = Film.builder()
                .id(999L)
                .name("Updated Name")
                .description("Updated description")
                .releaseDate(VALID_RELEASE_DATE)
                .duration(120)
                .build();

        assertThatThrownBy(() -> filmService.updateFilm(nonExistentFilm))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Такого фильма нет в списке!");
    }
}