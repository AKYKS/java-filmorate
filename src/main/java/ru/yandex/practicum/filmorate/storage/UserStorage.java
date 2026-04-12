package ru.yandex.practicum.filmorate.storage;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.filmorate.expection.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.expection.DuplicatedDataException;
import ru.yandex.practicum.filmorate.expection.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public interface UserStorage {
    final Map<Long, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> findAll();

    @PostMapping
    public User create(@RequestBody User user);

    // вспомогательный метод для генерации идентификатора нового поста
    default long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    @PutMapping
    public User update(@RequestBody User newUser);

    default void checkUserData(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            throw new ConditionsNotMetException("Электронная почта не может быть пустой и должна содержать символ \"@\"");
        }
        if (users.values().stream()
                .anyMatch(curUser -> curUser.getEmail().equals(user.getEmail()))) {
            throw new DuplicatedDataException("Этот имейл уже используется");
        }
        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            throw new ConditionsNotMetException("Логин не может быть пустым и содержать пробелы");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        if (user.getBirthday().isAfter(LocalDate.now())) {
            throw new ValidationException("Дата рождения не может быть в будущем.");
        }
    }

    default void clear() {
        users.clear();
    }
}
