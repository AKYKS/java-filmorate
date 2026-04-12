package ru.yandex.practicum.filmorate.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.expection.DuplicatedDataException;
import ru.yandex.practicum.filmorate.expection.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {
    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private final InMemoryUserStorage inMemoryUserStorage;

    @Autowired
    public UserService(InMemoryUserStorage inMemoryUserStorage) {
        this.inMemoryUserStorage = inMemoryUserStorage;
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
            if (anotherUserFriends == null) {
                anotherUserFriends = new HashSet<>();
            }
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

        if (userFriends != null) {
            if (!userFriends.contains(id)) {
                throw new NotFoundException("Пользователя с этим id нет в друзьях");
            }
        } else {
            userFriends = new HashSet<>();
            if (anotherUserFriends == null) {
                anotherUserFriends = new HashSet<>();
            }
            //throw new NotFoundException("Друзья отсутствуют");
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

    public List<User> getMutualFriendList(long userId, long id) {
        isPresentUser(userId);
        isPresentUser(id);

        User user = inMemoryUserStorage.findCurrentUser(userId);
        Set<Long> userFriends = user.getFriends();

        User anotherUser = inMemoryUserStorage.findCurrentUser(id);
        Set<Long> anotherUserFriends = anotherUser.getFriends();

        isPresentUserFriends(userFriends);
        isPresentUserFriends(anotherUserFriends);

        Set<Long> mutualFriendIds = new HashSet<>(userFriends);

        mutualFriendIds.retainAll(anotherUserFriends);

        return friendIdtoList(mutualFriendIds);
    }

    public void isPresentUser(long id) {
        if (inMemoryUserStorage.findCurrentUser(id) == null) {
            log.warn("Пользователь с таким id {} не найден", id);
            throw new NotFoundException("Пользователь с таким id не найден");
        }
    }

    public void isPresentUserFriends(Set<Long> userFriends) {
        if (userFriends == null) {
            throw new NotFoundException("Друзья отсутствуют");
        }
    }

    public List<User> friendIdtoList(Set<Long> friendsId) {
        return friendsId.stream()
                .map(friendId -> inMemoryUserStorage.findCurrentUser(friendId))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
