package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.storage.user.UserStorage;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationExceptionDuplicate;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

@Slf4j
@Service
public class UserService {
    private final UserStorage userStorage;

    public UserService(@Qualifier("userDbStorage") UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public User addUser(User user) {

        // Проверяем уникальность email
        if (userStorage.existsByEmail(user.getEmail(), 0)) {
            log.error("Email {} уже используется другим пользователем", user.getEmail());
            throw new ValidationExceptionDuplicate("Email " + user.getEmail() + " уже занят другим пользователем");
        }
        // Проверяем уникальность login
        if (userStorage.existsByLogin(user.getLogin(), 0)) {
            log.error("Логин {} уже используется другим пользователем", user.getLogin());
            throw new ValidationExceptionDuplicate("Логин " + user.getLogin() + " уже занят другим пользователем");
        }

        // Установка name = login, если name пустое
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

        User addedUser = userStorage.addUser(user);
        log.debug("Создан пользователь: {}", addedUser);
        return addedUser;
    }

    public User updateUser(User user) {

        // Проверяем существование пользователя
        validateUserExists(user.getId());

        // Проверяем уникальность email (кроме текущего пользователя)
        if (userStorage.existsByEmail(user.getEmail(), user.getId())) {
            log.error("Email {} уже используется другим пользователем", user.getEmail());
            throw new ValidationExceptionDuplicate("Email " + user.getEmail() + " уже занят другим пользователем");
        }
        // Проверяем уникальность login (кроме текущего пользователя)
        if (userStorage.existsByLogin(user.getLogin(), user.getId())) {
            log.error("Логин {} уже используется другим пользователем", user.getLogin());
            throw new ValidationExceptionDuplicate("Логин " + user.getLogin() + " уже занят другим пользователем");
        }

        // Подставляем login, если name пустое
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

        // Проверяем друзей (если список не null)
        if (user.getFriends() != null) {
            user.getFriends().forEach(this::findUserById); // Проверяем каждый Id
        } else {
            // Если друзей не передали, обнулим список, так как это Post(полный update)
            user.setFriends(new HashSet<>());
        }

        User updatedUser = userStorage.updateUser(user);
        log.debug("Пользователь полностью обновлен: {}", updatedUser);
        return updatedUser;
    }


    public User findUserById(int id) {
        User user = userStorage.findUserById(id)
                               .orElseThrow(() -> new NotFoundException("Пользователь с ID " + id + " не найден"));

        log.debug("Получили пользователя {}", user);
        return user;

    }

    public void validateUserExists(int id) {
        if (!userStorage.existsById(id)) {
            throw new NotFoundException("Пользователь с ID " + id + " не найден");
        }
    }

    public Collection<User> getAllUsers() {
        Collection<User> users = userStorage.getAllUsers();
        log.debug("Получили список пользователей {}", users);
        return users;
    }

    public void addFriend(int userId, int friendId) {
        if (userId == friendId) {
            log.debug("Пользователь {} пытается добавить самого себя в друзья", userId);
            throw new ValidationExceptionDuplicate("Нельзя добавить самого себя в друзья");
        }
        // проверка пользователей
        validateUserExists(userId);
        validateUserExists(friendId);

        User user = findUserById(userId); // подгрузили пользователя с друзьями из БД

        if (user.getFriends().contains(friendId)) {
            log.debug("Пользователь {} пытается добавить {} в друзья дважды", userId, friendId);
            throw new ValidationExceptionDuplicate("Нельзя добавить в друзья дважды");
        }

        user.getFriends().add(friendId);

        // таблица связи пользователь - друг
        userStorage.addFriend(userId, friendId);

        log.debug("Пользователь {} добавил {} в друзья", userId, friendId);

    }

    public void removeFriend(int userId, int friendId) {
        if (userId == friendId) {
            log.debug("Пользователь {} пытается удалить самого себя из друзей", userId);
            throw new ValidationExceptionDuplicate("Нельзя удалить самого себя из друзей");
        }
        // проверка пользователей
        validateUserExists(userId);
        validateUserExists(friendId);

        User user = findUserById(userId);

        if (!user.getFriends().remove(friendId)) {
            log.debug("Дружба не найдена");
            return;
        }

        userStorage.removeFriend(userId, friendId);
        log.debug("Пользователь {} удалил {}  из друзей", userId, friendId);
    }

    public List<User> getFriends(int userId) {
        // проверка пользователя
        validateUserExists(userId);
        List<User> users = userStorage.getFriends(userId);
        log.debug("Для пользователя {} получили список друзей  {}", userId, users);
        return users;
    }

    public List<User> getCommonFriends(int userId, int otherId) {
        // проверка пользователей
        validateUserExists(userId);
        validateUserExists(otherId);

        List<User> users = userStorage.getCommonFriends(userId, otherId);
        log.debug("Получили список {} общих друзей для пользователя {} и {}", users, userId, otherId);
        return users;
    }
}

