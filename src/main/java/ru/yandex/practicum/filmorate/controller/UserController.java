package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    public Collection<User> findAll() {
        return userService.findAll();
    }

    @GetMapping("/{id}")
    public User findCurrentFilm(@PathVariable long id) {
        return userService.findCurrentUser(id);
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
        return userService.create(user);
    }

    @PutMapping
    public User update(@RequestBody User newUser) {
        return userService.update(newUser);
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
        userService.clear();
    }
}
