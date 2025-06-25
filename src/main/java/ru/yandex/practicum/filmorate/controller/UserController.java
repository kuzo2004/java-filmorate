package ru.yandex.practicum.filmorate.controller;


import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.exception.ValidationExceptionDuplicate;
import ru.yandex.practicum.filmorate.model.User;
import jakarta.validation.Valid;
import ru.yandex.practicum.filmorate.model.UserUpdateDTO;

import java.util.*;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private final Map<Integer, User> users = new HashMap<>();

    @PostMapping
    public User createUser(@Valid @RequestBody User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

        checkEmailUniqueness(user.getEmail(), user.getId());
        checkLoginUniqueness(user.getLogin(), user.getId());

        user.setId(getNextId());
        users.put(user.getId(), user);
        log.info("Создан пользователь: {}", user);
        return user;
    }

    @PutMapping
    public User updateUser(@Valid @RequestBody UserUpdateDTO updatedUser) {
        if (!users.containsKey(updatedUser.getId())) {
            log.error("Пользователь с ID {} не найден", updatedUser.getId());
            throw new ValidationException("Пользователь с ID " + updatedUser.getId() + " не найден");
        }

        User existingUser = users.get(updatedUser.getId());

        // Проверка уникальности email (если передан)
        if (updatedUser.getEmail() != null && !updatedUser.getEmail().isEmpty()) {
            checkEmailUniqueness(updatedUser.getEmail(), updatedUser.getId());
            existingUser.setEmail(updatedUser.getEmail());
        }

        // Проверка уникальности login (если передан)
        if (updatedUser.getLogin() != null && !updatedUser.getLogin().isEmpty()) {
            checkLoginUniqueness(updatedUser.getLogin(), updatedUser.getId());
            existingUser.setLogin(updatedUser.getLogin());
        }

        // Обновление name (если передан и не пустой)
        if (updatedUser.getName() != null && !updatedUser.getName().isEmpty()) {
            existingUser.setName(updatedUser.getName());
        }

        // Обновление birthday (если передан)
        if (updatedUser.getBirthday() != null) {
            existingUser.setBirthday(updatedUser.getBirthday());
        }

        log.info("Обновлён пользователь: {}", existingUser);
        return existingUser;
    }


    @GetMapping
    public Collection<User> getAllUsers() {
        return users.values();
    }


    private int getNextId() {
        int currentMaxId = users.keySet()
                                .stream()
                                .mapToInt(id -> id)
                                .max()
                                .orElse(0);
        return ++currentMaxId;
    }

    private void checkEmailUniqueness(String email, Integer currentUserId) {
        boolean emailExists = users.values().stream()
                                   .anyMatch(u -> u.getEmail().equals(email) && u.getId() != currentUserId);

        if (emailExists) {
            log.error("Email {} уже используется другим пользователем", email);
            throw new ValidationExceptionDuplicate("Email уже используется другим пользователем");
        }
    }

    private void checkLoginUniqueness(String login, Integer currentUserId) {
        boolean loginExists = users.values().stream()
                                   .anyMatch(u -> u.getLogin().equals(login)
                                           && u.getId() != currentUserId);

        if (loginExists) {
            log.error("Логин {} уже используется другим пользователем", login);
            throw new ValidationExceptionDuplicate("Логин уже используется другим пользователем");
        }
    }
}

