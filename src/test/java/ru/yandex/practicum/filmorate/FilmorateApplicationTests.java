package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.expection.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.expection.DuplicatedDataException;
import ru.yandex.practicum.filmorate.expection.NotFoundException;
import ru.yandex.practicum.filmorate.expection.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class FilmorateApplicationTests {

    @Autowired
    private FilmController filmController;
    @Autowired
    private UserController userController;

    @BeforeEach
    void setUp() {
        filmController.clear();
        userController.clear();
    }

    @Test
    void contextLoads() {
    }

    @Test
    void createUser_ValidData_ShouldReturnCreatedUser() throws Exception {
        User user = new User();
        user.setEmail("valid@example.com");
        user.setLogin("validlogin");
        user.setName("Valid Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User createdUser = userController.create(user);

        assertNotNull(createdUser.getId());
        assertEquals("valid@example.com", createdUser.getEmail());
        assertEquals("validlogin", createdUser.getLogin());
    }

    @Test
    void createUser_EmptyEmail_ShouldThrowConditionsNotMetException() {
        User user = new User();
        user.setEmail("");
        user.setLogin("login");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        ConditionsNotMetException exception = assertThrows(
                ConditionsNotMetException.class,
                () -> userController.create(user)
        );

        assertTrue(exception.getMessage().contains("Электронная почта не может быть пустой"));
    }

    @Test
    void createUser_EmailWithoutAtSymbol_ShouldThrowConditionsNotMetException() {
        User user = new User();
        user.setEmail("invalidemail.com");
        user.setLogin("login");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        ConditionsNotMetException exception = assertThrows(
                ConditionsNotMetException.class,
                () -> userController.create(user)
        );

        assertTrue(exception.getMessage().contains("должна содержать символ"));
    }

    @Test
    void createUser_DuplicateEmail_ShouldThrowDuplicatedDataException() {
        // Сначала создаём пользователя с определённым email
        User existingUser = new User();
        existingUser.setEmail("duplicate@example.com");
        existingUser.setLogin("existinglogin");
        existingUser.setBirthday(LocalDate.of(1990, 1, 1));
        userController.create(existingUser);

        // Пытаемся создать второго пользователя с тем же email
        User newUser = new User();
        newUser.setEmail("duplicate@example.com");
        newUser.setLogin("newlogin");
        newUser.setBirthday(LocalDate.of(1995, 5, 15));

        DuplicatedDataException exception = assertThrows(
                DuplicatedDataException.class,
                () -> userController.create(newUser)
        );

        assertTrue(exception.getMessage().contains("Этот имейл уже используется"));
    }

    @Test
    void createUser_EmptyLogin_ShouldThrowConditionsNotMetException() {
        User user = new User();
        user.setEmail("email@example.com");
        user.setLogin("");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        ConditionsNotMetException exception = assertThrows(
                ConditionsNotMetException.class,
                () -> userController.create(user)
        );

        assertTrue(exception.getMessage().contains("Логин не может быть пустым"));
    }


    @Test
    void createUser_LoginWithSpace_ShouldThrowConditionsNotMetException() {
        User user = new User();
        user.setEmail("email@example.com");
        user.setLogin("invalid login");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        ConditionsNotMetException exception = assertThrows(
                ConditionsNotMetException.class,
                () -> userController.create(user)
        );

        assertTrue(exception.getMessage().contains("содержать пробелы"));
    }

    @Test
    void createUser_NameIsNull_ShouldSetNameAsLogin() {
        User user = new User();
        user.setEmail("email@example.com");
        user.setLogin("mylogin");
        user.setName(null);
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User createdUser = userController.create(user);

        assertEquals("mylogin", createdUser.getName());
    }

    @Test
    void createUser_BirthdayInFuture_ShouldThrowValidationException() {
        User user = new User();
        user.setEmail("email@example.com");
        user.setLogin("login");
        user.setBirthday(LocalDate.now().plusDays(1));

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> userController.create(user)
        );

        assertTrue(exception.getMessage().contains("Дата рождения не может быть в будущем"));
    }

    @Test
    void createUser_BirthdayToday_ShouldBeValid() {
        User user = new User();
        user.setEmail("email@example.com");
        user.setLogin("login");
        user.setBirthday(LocalDate.now());

        assertDoesNotThrow(() -> userController.create(user));
    }

    @Test
    void createUser_EarliestPossibleDate_ShouldBeValid() {
        User user = new User();
        user.setEmail("email@example.com");
        user.setLogin("login");
        user.setBirthday(LocalDate.MIN);

        assertDoesNotThrow(() -> userController.create(user));
    }

    @Test
    void updateUser_ValidData_ShouldUpdateUser() {
        // Создаём пользователя для обновления
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setEmail("old@example.com");
        existingUser.setLogin("oldlogin");
        existingUser.setName("Old Name");
        existingUser.setBirthday(LocalDate.of(1990, 1, 1));
        userController.create(existingUser);

        // Обновляем данные
        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setEmail("updated@example.com");
        updatedUser.setLogin("updatedlogin");

        User result = userController.update(updatedUser);

        assertEquals("updated@example.com", result.getEmail());
        assertEquals("updatedlogin", result.getLogin());
        assertEquals("Old Name", result.getName()); // имя не изменилось
    }

    @Test
    void updateUser_MissingId_ShouldThrowConditionsNotMetException() {
        User user = new User();
        user.setEmail("email@example.com");
        user.setLogin("login");

        ConditionsNotMetException exception = assertThrows(
                ConditionsNotMetException.class,
                () -> userController.update(user)
        );

        assertTrue(exception.getMessage().contains("Id должен быть указан"));
    }

    @Test
    void updateUser_NonExistentUser_ShouldThrowNotFoundException() {
        User user = new User();
        user.setId(999L); // ID, которого нет в системе
        user.setEmail("email@example.com");
        user.setLogin("login");

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> userController.update(user)
        );

        assertTrue(exception.getMessage().contains("Пользователь с id = 999 не найден"));
    }

    @Test
    void updateUser_DuplicateEmail_ShouldThrowDuplicatedDataException() {
        // Создаём первого пользователя
        User firstUser = new User();
        firstUser.setEmail("first@example.com");
        firstUser.setLogin("firstlogin");
        firstUser.setBirthday(LocalDate.of(1990, 1, 1));
        userController.create(firstUser);

        // Создаём второго пользователя
        User secondUser = new User();
        secondUser.setEmail("second@example.com");
        secondUser.setLogin("secondlogin");
        secondUser.setBirthday(LocalDate.of(1995, 5, 15));
        User createdSecondUser = userController.create(secondUser);

        // Пытаемся обновить второго пользователя с email первого
        createdSecondUser.setEmail("first@example.com");

        DuplicatedDataException exception = assertThrows(
                DuplicatedDataException.class,
                () -> userController.update(createdSecondUser)
        );

        assertTrue(exception.getMessage().contains("Этот имейл уже используется"));
    }

    @Test
    void findAll_EmptyList_ShouldReturnEmptyCollection() {
        Collection<User> users = userController.findAll();

        assertNotNull(users);
        assertTrue(users.isEmpty());
    }

    @Test
    void findAll_NonEmptyList_ShouldReturnAllUsers() {
        // Создаём нескольких пользователей
        User user1 = new User();
        user1.setEmail("user1@example.com");
        user1.setLogin("user1");
        user1.setBirthday(LocalDate.of(1990, 1, 1));
        userController.create(user1);

        User user2 = new User();
        user2.setEmail("user2@example.com");
        user2.setLogin("user2");
        user2.setBirthday(LocalDate.of(1995, 5, 15));
        userController.create(user2);

        Collection<User> users = userController.findAll();

        assertNotNull(users);
        assertEquals(2, users.size());
        assertTrue(users.stream().anyMatch(u -> u.getEmail().equals("user1@example.com")));
        assertTrue(users.stream().anyMatch(u -> u.getEmail().equals("user2@example.com")));
    }

    //FILM TEST

    @Test
    void shouldThrowExceptionWhenNameIsBlank() {
        Film film = new Film();
        film.setName("");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120);

        assertThrows(ConditionsNotMetException.class, () -> filmController.create(film));
    }

    @Test
    void shouldThrowExceptionWhenNameIsSpaces() {
        Film film = new Film();
        film.setName("   ");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120);

        assertThrows(ConditionsNotMetException.class, () -> filmController.create(film));
    }

    @Test
    void shouldCreateFilmWithMinimalName() {
        Film film = new Film();
        film.setName("A");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120);

        Film createdFilm = filmController.create(film);
        assertNotNull(createdFilm.getId());
        assertEquals("A", createdFilm.getName());
    }

    @Test
    void shouldCreateFilmWithMaxDescriptionLength() {
        String maxDescription = "A".repeat(200);
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription(maxDescription);
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120);

        Film createdFilm = filmController.create(film);
        assertEquals(maxDescription, createdFilm.getDescription());
    }

    @Test
    void shouldThrowExceptionWhenDescriptionTooLong() {
        String longDescription = "A".repeat(201);
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription(longDescription);
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120);

        assertThrows(ValidationException.class, () -> filmController.create(film));
    }

    @Test
    void shouldCreateFilmWithEmptyDescription() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120);

        Film createdFilm = filmController.create(film);
        assertEquals("", createdFilm.getDescription());
    }

    @Test
    void shouldCreateFilmWithMinReleaseDate() {
        LocalDate minDate = LocalDate.of(1895, 12, 28);
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Description");
        film.setReleaseDate(minDate);
        film.setDuration(120);

        Film createdFilm = filmController.create(film);
        assertEquals(minDate, createdFilm.getReleaseDate());
    }

    @Test
    void shouldThrowExceptionWhenReleaseDateBeforeMin() {
        LocalDate beforeMinDate = LocalDate.of(1895, 12, 27);
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Description");
        film.setReleaseDate(beforeMinDate);
        film.setDuration(120);

        assertThrows(ValidationException.class, () -> filmController.create(film));
    }

    @Test
    void shouldCreateFilmWithFutureReleaseDate() {
        LocalDate futureDate = LocalDate.now().plusYears(1);
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Description");
        film.setReleaseDate(futureDate);
        film.setDuration(120);

        Film createdFilm = filmController.create(film);
        assertEquals(futureDate, createdFilm.getReleaseDate());
    }

    @Test
    void shouldThrowExceptionWhenDurationZero() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(0);

        assertThrows(ValidationException.class, () -> filmController.create(film));
    }

    @Test
    void shouldThrowExceptionWhenDurationNegative() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(-1);

        assertThrows(ValidationException.class, () -> filmController.create(film));
    }

    @Test
    void shouldCreateFilmWithPositiveDuration() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(1);

        Film createdFilm = filmController.create(film);
        assertEquals(1, createdFilm.getDuration());
    }

    @Test
    void shouldThrowExceptionWhenIdIsNull() {
        Film newFilm = new Film();
        newFilm.setId(null);
        newFilm.setName("Updated Name");

        assertThrows(ConditionsNotMetException.class, () -> filmController.update(newFilm));
    }

    @Test
    void shouldThrowExceptionWhenFilmNotFound() {
        Film newFilm = new Film();
        newFilm.setId(999L);
        newFilm.setName("Updated Name");

        assertThrows(NotFoundException.class, () -> filmController.update(newFilm));
    }

    @Test
    void shouldUpdateExistingFilm() {
        // Сначала создаём фильм
        Film originalFilm = new Film();
        originalFilm.setName("Original Name");
        originalFilm.setDescription("Original Description");
        originalFilm.setReleaseDate(LocalDate.of(2020, 1, 1));
        originalFilm.setDuration(120);
        Film createdFilm = filmController.create(originalFilm);

        // Теперь обновляем
        Film updateFilm = new Film();
        updateFilm.setId(createdFilm.getId());
        updateFilm.setName("Updated Name");
        updateFilm.setDescription("Updated Description");

        Film updatedFilm = filmController.update(updateFilm);
        assertEquals("Updated Name", updatedFilm.getName());
        assertEquals("Updated Description", updatedFilm.getDescription());
    }

    //test for final11
    //friends

    @Test
    void getFriendList_EmptyFriends_ShouldReturnEmptyCollection() {
        // Создаём пользователя без друзей
        User user = new User();
        user.setEmail("user@example.com");
        user.setLogin("userlogin");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        User createdUser = userController.create(user);

        Collection<User> friends = userController.getFriendList(createdUser.getId());

        assertNotNull(friends);
        assertTrue(friends.isEmpty());
    }

    @Test
    void getFriendList_WithFriends_ShouldReturnAllFriends() {
        // Создаём основного пользователя
        User mainUser = new User();
        mainUser.setEmail("main@example.com");
        mainUser.setLogin("mainlogin");
        mainUser.setBirthday(LocalDate.of(1990, 1, 1));
        User createdMainUser = userController.create(mainUser);

        // Создаём друзей
        User friend1 = new User();
        friend1.setEmail("friend1@example.com");
        friend1.setLogin("friend1login");
        friend1.setBirthday(LocalDate.of(1995, 5, 15));
        User createdFriend1 = userController.create(friend1);

        User friend2 = new User();
        friend2.setEmail("friend2@example.com");
        friend2.setLogin("friend2login");
        friend2.setBirthday(LocalDate.of(2000, 3, 20));
        User createdFriend2 = userController.create(friend2);

        // Добавляем друзей
        userController.addFriend(createdMainUser.getId(), createdFriend1.getId());
        userController.addFriend(createdMainUser.getId(), createdFriend2.getId());

        Collection<User> friends = userController.getFriendList(createdMainUser.getId());

        assertNotNull(friends);
        assertEquals(2, friends.size());
        assertTrue(friends.stream().anyMatch(u -> u.getId().equals(createdFriend1.getId())));
        assertTrue(friends.stream().anyMatch(u -> u.getId().equals(createdFriend2.getId())));
    }


    @Test
    void getMutualFriendList_WithMutualFriends_ShouldReturnMutualFriends() {
        // Создаём пользователей и их общих друзей
        User user1 = new User();
        user1.setEmail("user1@example.com");
        user1.setLogin("user1login");
        user1.setBirthday(LocalDate.of(1990, 1, 1));
        User createdUser1 = userController.create(user1);

        User user2 = new User();
        user2.setEmail("user2@example.com");
        user2.setLogin("user2login");
        user2.setBirthday(LocalDate.of(1995, 5, 15));
        User createdUser2 = userController.create(user2);

        User mutualFriend = new User();
        mutualFriend.setEmail("mutual@example.com");
        mutualFriend.setLogin("mutuallogin");
        mutualFriend.setBirthday(LocalDate.of(2000, 3, 20));
        User createdMutualFriend = userController.create(mutualFriend);

        // Устанавливаем дружбу
        userController.addFriend(createdUser1.getId(), createdMutualFriend.getId());
        userController.addFriend(createdUser2.getId(), createdMutualFriend.getId());

        Collection<User> mutualFriends = userController.getMutualFriendList(
                createdUser1.getId(),
                createdUser2.getId()
        );

        assertNotNull(mutualFriends);
        assertEquals(1, mutualFriends.size());
        assertEquals(createdMutualFriend.getId(), mutualFriends.iterator().next().getId());
    }

    @Test
    void addFriend_ValidData_ShouldAddFriend() {
        // Создаём двух пользователей
        User user = new User();
        user.setEmail("user@example.com");
        user.setLogin("userlogin");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        User createdUser = userController.create(user);

        User friend = new User();
        friend.setEmail("friend@example.com");
        friend.setLogin("friendlogin");
        friend.setBirthday(LocalDate.of(1995, 5, 15));
        User createdFriend = userController.create(friend);

        Collection<User> result = userController.addFriend(
                createdUser.getId(),
                createdFriend.getId()
        );

        assertNotNull(result);
        assertEquals(2, result.size()); // оба пользователя возвращаются
    }

    @Test
    void addFriend_DuplicateFriend_ShouldThrowDuplicatedDataException() {
        // Создаём двух пользователей и добавляем друга
        User user = new User();
        user.setEmail("user@example.com");
        user.setLogin("userlogin");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        User createdUser = userController.create(user);

        User friend = new User();
        friend.setEmail("friend@example.com");
        friend.setLogin("friendlogin");
        friend.setBirthday(LocalDate.of(1995, 5, 15));
        User createdFriend = userController.create(friend);

        userController.addFriend(createdUser.getId(), createdFriend.getId());

        // Пытаемся добавить того же друга повторно
        DuplicatedDataException exception = assertThrows(
                DuplicatedDataException.class,
                () -> userController.addFriend(createdUser.getId(), createdFriend.getId())
        );

        assertTrue(exception.getMessage().contains("Пользователь с этим id уже у вас в друзьях"));
    }

    @Test
    void deleteFriend_ValidData_ShouldDeleteFriend() {
        // Создаём двух пользователей и добавляем друга
        User user = new User();
        user.setEmail("user@example.com");
        user.setLogin("userlogin");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        User createdUser = userController.create(user);

        User friend = new User();
        friend.setEmail("friend@example.com");
        friend.setLogin("friendlogin");
        friend.setBirthday(LocalDate.of(1995, 5, 15));
        User createdFriend = userController.create(friend);

        userController.addFriend(createdUser.getId(), createdFriend.getId());

        // Удаляем друга
        Collection<User> result = userController.deleteFriend(
                createdUser.getId(),
                createdFriend.getId()
        );

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void addFriend_NonExistentUser_ShouldThrowNotFoundException() {
        // Создаём одного пользователя
        User user = new User();
        user.setEmail("user@example.com");
        user.setLogin("userlogin");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        User createdUser = userController.create(user);

        // Пытаемся добавить несуществующего друга
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> userController.addFriend(createdUser.getId(), 999L)
        );

        assertTrue(exception.getMessage().contains("Пользователь с таким id не найден"));
    }

    @Test
    void getFriendList_NonExistentUser_ShouldThrowNotFoundException() {
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> userController.getFriendList(999L)
        );

        assertTrue(exception.getMessage().contains("Пользователь с таким id не найден"));
    }

    @Test
    void getMutualFriendList_NonExistentUser_ShouldThrowNotFoundException() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setLogin("userlogin");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        User createdUser = userController.create(user);

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> userController.getMutualFriendList(createdUser.getId(), 999L)
        );

        assertTrue(exception.getMessage().contains("Пользователь с таким id не найден"));
    }

    //likes

    @Test
    void addLikeFilm_ValidData_ShouldAddLike() {
        // Создаём фильм и пользователя
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120);
        Film createdFilm = filmController.create(film);

        User user = new User();
        user.setEmail("user@example.com");
        user.setLogin("userlogin");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        User createdUser = userController.create(user);

        Film result = filmController.addLikeFilm(createdFilm.getId(), createdUser.getId());

        assertNotNull(result.getUserLikes());
        assertTrue(result.getUserLikes().contains(createdUser.getId()));
    }

    @Test
    void addLikeFilm_DuplicateLike_ShouldThrowDuplicatedDataException() {
        // Создаём фильм и пользователя, добавляем лайк
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120);
        Film createdFilm = filmController.create(film);

        User user = new User();
        user.setEmail("user@example.com");
        user.setLogin("userlogin");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        User createdUser = userController.create(user);

        filmController.addLikeFilm(createdFilm.getId(), createdUser.getId());

        // Пытаемся поставить лайк повторно
        DuplicatedDataException exception = assertThrows(
                DuplicatedDataException.class,
                () -> filmController.addLikeFilm(createdFilm.getId(), createdUser.getId())
        );

        assertTrue(exception.getMessage().contains("Вы уже лайкали этот фильм!"));
    }

    @Test
    void deleteLikeFilm_ValidData_ShouldDeleteLike() {
        // Создаём фильм и пользователя, добавляем лайк
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120);
        Film createdFilm = filmController.create(film);

        User user = new User();
        user.setEmail("user@example.com");
        user.setLogin("userlogin");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        User createdUser = userController.create(user);

        filmController.addLikeFilm(createdFilm.getId(), createdUser.getId());

        // Удаляем лайк
        Film result = filmController.deleteLikeFilm(createdFilm.getId(), createdUser.getId());

        assertNotNull(result.getUserLikes());
        assertFalse(result.getUserLikes().contains(createdUser.getId()));
    }

    @Test
    void deleteLikeFilm_NonExistentLike_ShouldThrowNotFoundException() {
        // Создаём фильм и пользователя без лайка
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120);
        Film createdFilm = filmController.create(film);

        User user = new User();
        user.setEmail("user@example.com");
        user.setLogin("userlogin");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        User createdUser = userController.create(user);

        // Пытаемся удалить несуществующий лайк
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> filmController.deleteLikeFilm(createdFilm.getId(), createdUser.getId())
        );

        assertTrue(exception.getMessage().contains("Вы и так не лайкали этот фильм"));
    }

    @Test
    void addLikeFilm_NonExistentFilm_ShouldThrowNotFoundException() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setLogin("userlogin");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        User createdUser = userController.create(user);

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> filmController.addLikeFilm(999L, createdUser.getId())
        );

        assertTrue(exception.getMessage().contains("Фильм с таким id не найден"));
    }

    @Test
    void addLikeFilm_NonExistentUser_ShouldThrowNotFoundException() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120);
        Film createdFilm = filmController.create(film);

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> filmController.addLikeFilm(createdFilm.getId(), 999L)
        );

        assertTrue(exception.getMessage().contains("Пользователь с таким id не найден"));
    }

    @Test
    void listOfPopularFilms_EmptyList_ShouldReturnEmptyCollection() {
        Collection<Film> popularFilms = filmController.listOfpopularFilms(10);

        assertNotNull(popularFilms);
        assertTrue(popularFilms.isEmpty());
    }

    @Test
    void listOfPopularFilms_WithLikes_ShouldReturnSortedByLikes() {
        // Создаём несколько фильмов с разным количеством лайков
        Film film1 = new Film();
        film1.setName("Film 1");
        film1.setDescription("Description 1");
        film1.setReleaseDate(LocalDate.of(2020, 1, 1));
        film1.setDuration(120);
        Film createdFilm1 = filmController.create(film1);

        Film film2 = new Film();
        film2.setName("Film 2");
        film2.setDescription("Description 2");
        film2.setReleaseDate(LocalDate.of(2021, 1, 1));
        film2.setDuration(130);
        Film createdFilm2 = filmController.create(film2);


        Film film3 = new Film();
        film3.setName("Film 3");
        film3.setDescription("Description 3");
        film3.setReleaseDate(LocalDate.of(2022, 1, 1));
        film3.setDuration(140);
        Film createdFilm3 = filmController.create(film3);

        // Создаём пользователей для лайков
        User user1 = new User();
        user1.setEmail("user1@example.com");
        user1.setLogin("user1login");
        user1.setBirthday(LocalDate.of(1990, 1, 1));
        User createdUser1 = userController.create(user1);

        User user2 = new User();
        user2.setEmail("user2@example.com");
        user2.setLogin("user2login");
        user2.setBirthday(LocalDate.of(1995, 5, 15));
        User createdUser2 = userController.create(user2);

        User user3 = new User();
        user3.setEmail("user3@example.com");
        user3.setLogin("user3login");
        user3.setBirthday(LocalDate.of(2000, 3, 20));
        User createdUser3 = userController.create(user3);

        // Ставим лайки: film1 — 3 лайка, film2 — 2 лайка, film3 — 1 лайк
        filmController.addLikeFilm(createdFilm1.getId(), createdUser1.getId());
        filmController.addLikeFilm(createdFilm1.getId(), createdUser2.getId());
        filmController.addLikeFilm(createdFilm1.getId(), createdUser3.getId());

        filmController.addLikeFilm(createdFilm2.getId(), createdUser1.getId());
        filmController.addLikeFilm(createdFilm2.getId(), createdUser2.getId());

        filmController.addLikeFilm(createdFilm3.getId(), createdUser1.getId());

        // Получаем популярные фильмы (топ‑2)
        Collection<Film> popularFilms = filmController.listOfpopularFilms(2);

        assertNotNull(popularFilms);
        assertEquals(2, popularFilms.size());

        List<Film> filmList = new ArrayList<>(popularFilms);
        // Первый фильм должен быть с наибольшим количеством лайков
        assertEquals(createdFilm1.getId(), filmList.get(0).getId());
        assertEquals(3, filmList.get(0).getUserLikes().size());
        // Второй фильм — со вторым по величине количеством лайков
        assertEquals(createdFilm2.getId(), filmList.get(1).getId());
        assertEquals(2, filmList.get(1).getUserLikes().size());
    }


}
