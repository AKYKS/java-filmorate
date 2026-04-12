package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.expection.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.expection.DuplicatedDataException;
import ru.yandex.practicum.filmorate.expection.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

@Component
public class InMemoryUserStorage implements UserStorage {

    @Override
    public Collection<User> findAll() {
        return users.values();
    }

    public User findCurrentUser(long id) {
        return users.get(id);
    }

    @Override
    public User create(User user) {
        // проверяем выполнение необходимых условий
        checkUserData(user);
        // формируем дополнительные данные
        user.setId(getNextId());
        // сохраняем новую публикацию в памяти приложения
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User update(User newUser) {
        // проверяем необходимые условия
        if (newUser.getId() == null) {
            throw new ConditionsNotMetException("Id должен быть указан");
        }
        if (users.containsKey(newUser.getId())) {
            if (users.values().stream()
                    .anyMatch(curUser -> curUser.getEmail().equals(newUser.getEmail()))) {
                throw new DuplicatedDataException("Этот имейл уже используется");
            }
            User oldUser = users.get(newUser.getId());
            if (newUser.getName() != null) {
                oldUser.setName(newUser.getName());
            }
            if (newUser.getLogin() != null) {
                oldUser.setLogin(newUser.getLogin());
            }
            if (newUser.getEmail() != null) {
                oldUser.setEmail(newUser.getEmail());
            }
            if (newUser.getBirthday() != null) {
                oldUser.setBirthday(newUser.getBirthday());
            }
            // если публикация найдена и все условия соблюдены, обновляем её содержимое
            return oldUser;
        }
        throw new NotFoundException("Пользователь с id = " + newUser.getId() + " не найден");
    }
}
