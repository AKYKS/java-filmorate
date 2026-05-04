package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.expection.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserStorage userStorage;

    public UserService(@Qualifier("userDbStorage") UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public User getUserById(Long id) {
        if (userStorage.getUserById(id) == null) {
            throw new DatabaseException("Такого юзера нет в списке!");
        }
        return userStorage.getUserById(id);
    }

    public User makeFriendship(long id, long friendId) {
        if (userStorage.getUserById(id) == null) {
            throw new NotFoundException("Такого юзера нет в списке!");
        }
        if (userStorage.getUserById(friendId) == null) {
            throw new NotFoundException("Невозможно добавить в друзья несуществующего юзера!");
        }
        userStorage.createFriendship(id, friendId);
        return userStorage.getUserById(id);
    }

    public User deleteFriendship(long id, long friendId) {
        if (userStorage.getUserById(id) == null) {
            throw new NotFoundException("Такого юзера нет в списке!");
        }
        if (userStorage.getUserById(friendId) == null) {
            throw new NotFoundException("Удаляемого из друзья юзера нет в списке!");
        }
        userStorage.deleteFriendship(id, friendId);
        return userStorage.getUserById(id);
    }

    public Collection<User> listOfFriends(long id) {
        if (userStorage.getUserById(id) == null) {
            throw new DatabaseException("Такого юзера нет в списке!");
        }
        if (userStorage.getUserById(id).getFriends() == null) {
            throw new DatabaseException("Список друзей пуст!");
        }
        return userStorage.listOfFriends(id);
    }

    public Collection<User> listOfCommonFriends(Long id, Long otherId) {
        if (userStorage.getUserById(id) == null || userStorage.getUserById(otherId) == null) {
            throw new NotFoundException("Одного из юзеров нет в списке!");
        }
        if (userStorage.getUserById(id).getFriends() == null || userStorage.getUserById(otherId).getFriends() == null) {
            throw new NotFoundException("Один из списков друзей пуст!");
        }
        return userStorage.listOfCommonFriends(id, otherId);
    }

    public Collection<User> getAllUsers() {
        return userStorage.getAllUsers();
    }

    public User createUser(User user) {
        checkUserData(user);
        return userStorage.createUser(user);
    }

    public User updateUser(User user) {
        return userStorage.updateUser(user);
    }

    private void checkUserData(User user) {
        final Map<Long, User> users = userStorage.getAllUsers().stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

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
}
