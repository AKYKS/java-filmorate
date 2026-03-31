package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class FilmorateApplicationTests {

	private FilmController filmController;
	private UserController userController;

	@BeforeEach
	void setUp() {
		filmController = new FilmController();
		userController = new UserController();
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

}
