package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationExceptionDuplicate;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

@Slf4j
@Service
public class UserService {
    private final UserStorage userStorage;

    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public User addUser(User user) {
        // Проверка уникальности email и login
        checkEmailUniqueness(user.getEmail(), null);
        checkLoginUniqueness(user.getLogin(), null);

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
        User existingUser = getUser(user.getId()); // Выбросит NotFoundException, если не найден

        // Проверяем уникальность email и login (кроме текущего пользователя)
        checkEmailUniqueness(user.getEmail(), user.getId());
        checkLoginUniqueness(user.getLogin(), user.getId());

        // Подставляем login, если name пустое
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        // Сохраняем существующих друзей при обновлении
        user.setFriends(existingUser.getFriends());

        User updatedUser = userStorage.updateUser(user);
        log.debug("Пользователь полностью обновлен: {}", updatedUser);
        return updatedUser;
    }

    public User getUser(int id) {
        User user = userStorage.getUser(id);
        if (user == null) {
            throw new NotFoundException("Пользователь с ID " + id + " не найден");
        }
        return user;
    }

    public Collection<User> getAllUsers() {
        return userStorage.getAllUsers();
    }

    public void addFriend(int userId, int friendId) {
        if (userId == friendId) {
            log.debug("Пользователь {} пытается добавить самого себя в друзья", userId);
            throw new ValidationExceptionDuplicate("Нельзя добавить самого себя в друзья");
        }

        User user = getUser(userId);
        User friend = getUser(friendId);

        if (user.getFriends().contains(friendId)) {
            log.debug(String.format("Пользователь %d уже есть в друзьях у пользователя %d", friendId, userId));
            throw new ValidationExceptionDuplicate(
                    String.format("Пользователь %d уже есть в друзьях у пользователя %d", friendId, userId));
        }

        if (friend.getFriends().contains(userId)) {
            log.debug(String.format("Пользователь %d уже есть в друзьях у пользователя %d", userId, friendId));
            throw new ValidationExceptionDuplicate(
                    String.format("Пользователь %d уже есть в друзьях у пользователя %d", userId, friendId));
        }

        user.getFriends().add(friendId);
        friend.getFriends().add(userId);

        userStorage.updateUser(user);
        userStorage.updateUser(friend);
        log.debug("Пользователи {} и  {} добавлены в друзья к друг другу", user, friend);
    }

    public void removeFriend(int userId, int friendId) {
        if (userId == friendId) {
            log.debug("Пользователь {} пытается удалить самого себя из друзей", userId);
            throw new ValidationExceptionDuplicate("Нельзя удалить самого себя из друзей");
        }
        User user = getUser(userId);
        User friend = getUser(friendId);

        if (!user.getFriends().remove(friendId)) {
            log.debug("Дружба не найдена");
            return;
        }

        if (!friend.getFriends().remove(userId)) {
            log.debug("Дружба не найдена");
            return;
        }

        userStorage.updateUser(user);
        userStorage.updateUser(friend);
        log.debug("Пользователи {} и  {} удалены из друзей друг у друга", user, friend);
    }

    public List<User> getFriends(int userId) {
        User user = getUser(userId);
        //Преобразуем Set<Integer> друзей в List<User>
        return user.getFriends().stream()
                   .map(id -> this.getUser(id))
                   .toList();
    }

    public List<User> getCommonFriends(int userId, int otherId) {
        User user = getUser(userId);
        User otherUser = getUser(otherId);
        // копия множества друзей user, чтобы не менять исходные данные.
        Set<Integer> commonFriends = new HashSet<>(user.getFriends());
        // пересечение с множеством друзей otherUser
        commonFriends.retainAll(otherUser.getFriends());
        //Преобразуем Set<Integer> друзей в List<User>
        return commonFriends.stream()
                            .map(id -> this.getUser(id))
                            .toList();
    }

    private void checkEmailUniqueness(String email, Integer currentUserId) {
        boolean emailExists = userStorage.getAllUsers().stream()
                                         .anyMatch(u -> u.getEmail().equals(email)
                                                 && u.getId() != currentUserId);

        if (emailExists) {
            log.error("Email {} уже используется другим пользователем", email);
            throw new ValidationExceptionDuplicate("Email уже используется другим пользователем");
        }
    }

    private void checkLoginUniqueness(String login, Integer currentUserId) {
        boolean loginExists = userStorage.getAllUsers().stream()
                                         .anyMatch(u -> u.getLogin().equals(login)
                                                 && u.getId() != currentUserId);

        if (loginExists) {
            log.error("Логин {} уже используется другим пользователем", login);
            throw new ValidationExceptionDuplicate("Логин уже используется другим пользователем");
        }
    }
}

