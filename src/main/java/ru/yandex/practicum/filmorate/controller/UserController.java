package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;

import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private final InMemoryUserStorage inMemoryUserStorage;
    private final UserService userService;

    @Autowired
    public UserController(InMemoryUserStorage inMemoryUserStorage, UserService userService) {
        this.inMemoryUserStorage = inMemoryUserStorage;
        this.userService = userService;
    }

    @GetMapping
    public Collection<User> findAll() {
        return inMemoryUserStorage.findAll();
    }

    @GetMapping("/{id}")
    public User findCurrentFilm(@PathVariable long id) {
        return inMemoryUserStorage.findCurrentUser(id);
    }

    @GetMapping("/{userId}/friends")
    public Collection<User> getFriendList(@PathVariable long userId) {
        return userService.getFriendList(userId);
    }

    @GetMapping("/{userId}/friends/common/{otherId}")
    public Collection<User> getMutualFriendList(@PathVariable long userId, @PathVariable long otherId) {
        return userService.getMutualFriendList(userId, otherId);
    }

    @PostMapping
    public User create(@RequestBody User user) {
        return inMemoryUserStorage.create(user);
    }

    @PutMapping
    public User update(@RequestBody User newUser) {
        return inMemoryUserStorage.update(newUser);
    }

    @PutMapping("/{userId}/friends/{id}")
    public Collection<User> addFriend(@PathVariable long userId, @PathVariable long id) {
        return userService.addFriend(userId, id);
    }

    @DeleteMapping("/{userId}/friends/{id}")
    public Collection<User> deleteFriend(@PathVariable long userId, @PathVariable long id) {
        return userService.deleteFriend(userId, id);
    }

    public void clear() {
        inMemoryUserStorage.clear();
    }
}
