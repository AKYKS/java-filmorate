package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.expection.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.expection.DuplicatedDataException;
import ru.yandex.practicum.filmorate.expection.NotFoundException;
import ru.yandex.practicum.filmorate.expection.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private final InMemoryUserStorage inMemoryUserStorage;

    public Collection<User> findAll() {
        return inMemoryUserStorage.findAll();
    }

    public User findCurrentUser(long id) {
        return inMemoryUserStorage.findCurrentUser(id);
    }

    public User create(User user) {
        // проверяем выполнение необходимых условий
        checkUserData(user);
        return inMemoryUserStorage.create(user);
    }

    public User update(User newUser) {
        // проверяем необходимые условия
        if (newUser.getId() == null) {
            throw new ConditionsNotMetException("Id должен быть указан");
        }
        if (inMemoryUserStorage.findCurrentUser(newUser.getId()) != null) {
            return inMemoryUserStorage.update(newUser);
        }
        throw new NotFoundException("Пользователь с id = " + newUser.getId() + " не найден");
    }

    public Collection<User> addFriend(long userId, long id) {
        isPresentUser(userId);
        isPresentUser(id);

        User user = inMemoryUserStorage.findCurrentUser(userId);
        Set<Long> userFriends = user.getFriends();

        User anotherUser = inMemoryUserStorage.findCurrentUser(id);
        Set<Long> anotherUserFriends = anotherUser.getFriends();

        if (userFriends != null) {
            if (userFriends.contains(id)) {
                throw new DuplicatedDataException("Пользователь с этим id уже у вас в друзьях");
            }
        } else {
            userFriends = new HashSet<>();
        }
        if (anotherUserFriends == null) {
            anotherUserFriends = new HashSet<>();
        }
        userFriends.add(id);
        user.setFriends(userFriends);

        anotherUserFriends.add(userId);
        anotherUser.setFriends(anotherUserFriends);

        List<User> result = new ArrayList<>();

        result.add(user);
        result.add(anotherUser);

        log.info("Друг успешно добавлен!: {}", id);
        return result;
    }

    public Collection<User> deleteFriend(long userId, long id) {
        isPresentUser(userId);
        isPresentUser(id);

        User user = inMemoryUserStorage.findCurrentUser(userId);
        Set<Long> userFriends = user.getFriends();

        User anotherUser = inMemoryUserStorage.findCurrentUser(id);
        Set<Long> anotherUserFriends = anotherUser.getFriends();

        if (userFriends == null) {
            userFriends = new HashSet<>();
            if (anotherUserFriends == null) {
                anotherUserFriends = new HashSet<>();
            }
        }

        userFriends.remove(id);
        user.setFriends(userFriends);

        anotherUserFriends.remove(userId);
        anotherUser.setFriends(anotherUserFriends);

        List<User> result = new ArrayList<>();

        result.add(user);
        result.add(anotherUser);

        return result;
    }

    public List<User> getFriendList(long userId) {
        isPresentUser(userId);

        User user = inMemoryUserStorage.findCurrentUser(userId);
        Set<Long> userFriends = user.getFriends();

        if (userFriends == null) {
            userFriends = new HashSet<>();
        }
        ;

        return friendIdtoList(userFriends);
    }

    public Collection<User> getMutualFriendList(long userId, long id) {
        isPresentUser(userId);
        isPresentUser(id);

        User user = inMemoryUserStorage.findCurrentUser(userId);
        User otherUser = inMemoryUserStorage.findCurrentUser(id);

        Collection<User> commonFriends = user.getFriends().stream()
                .filter(otherUser.getFriends()::contains)
                .map(inMemoryUserStorage::findCurrentUser)
                .toList();
        return commonFriends;
    }

    public void clear() {
        inMemoryUserStorage.clear();
    }

    public void isPresentUser(long id) {
        if (inMemoryUserStorage.findCurrentUser(id) == null) {
            log.warn("Пользователь с таким id {} не найден", id);
            throw new NotFoundException("Пользователь с таким id не найден");
        }
    }

    private void isPresentUserFriends(Set<Long> userFriends) {
        if (userFriends == null) {
            throw new NotFoundException("Друзья отсутствуют");
        }
    }

    private List<User> friendIdtoList(Set<Long> friendsId) {
        return friendsId.stream()
                .map(friendId -> inMemoryUserStorage.findCurrentUser(friendId))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private void checkUserData(User user) {
        final Map<Long, User> users = inMemoryUserStorage.findAll().stream()
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
