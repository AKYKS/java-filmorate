package ru.yandex.practicum.filmorate.storage;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.filmorate.model.User;

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

    default void clear() {
        users.clear();
    }
}
