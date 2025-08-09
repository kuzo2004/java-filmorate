package ru.yandex.practicum.filmorate.dal.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class UserFindTest extends BaseStorageTest {

    @BeforeEach
    void setUp() {
        cleanAllTables();
    }

    @Test
    @DisplayName("должен находить пользователя по id")
    void findUserByIdTest() {
        insertTestUser();

        User expectedUser = new User();
        expectedUser.setId(TEST_USER_ID);
        expectedUser.setEmail("test@mail.ru");
        expectedUser.setLogin("testLogin");
        expectedUser.setName("Test Name");
        expectedUser.setBirthday(LocalDate.of(1990, 1, 1));
        expectedUser.setFriends(new HashSet<>());

        User userFromDB = userStorage.findUserById(TEST_USER_ID)
                                     .orElseThrow(() -> new NotFoundException("Пользователь с ID " + TEST_USER_ID + " не найден"));

        assertThat(userFromDB)
                .usingRecursiveComparison()
                .isEqualTo(expectedUser);
    }

    @Test
    @DisplayName("должен возвращать пустой Optional при поиске несуществующего пользователя")
    void findNonExistentUserByIdTest() {
        insertTestUser();

        Optional<User> result = userStorage.findUserById(9999);
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("должен возвращать всех пользователей")
    void getAllUsersTest() {
        insertTestUsers(5);

        Collection<User> users = userStorage.getAllUsers();

        assertThat(users).isNotEmpty();
        assertThat(users).hasSize(5);
    }

    @Test
    @DisplayName("должен проверять существование пользователя")
    void existsByIdTest() {
        insertTestUser();

        assertThat(userStorage.existsById(TEST_USER_ID)).isTrue();
        assertThat(userStorage.existsById(9999)).isFalse();
    }

    @Test
    @DisplayName("должен проверять существование пользователя по email")
    void existsByEmailTest() {
        insertTestUser();

        // есть в БД
        assertThat(userStorage.existsByEmail("test@mail.ru", null)).isTrue();
        // нет в БД, кроме как у текущего пользователя
        assertThat(userStorage.existsByEmail("test@mail.ru", TEST_USER_ID)).isFalse();
        // нет такого адреса в БД
        assertThat(userStorage.existsByEmail("nonexistent@mail.ru", null)).isFalse();
    }

    @Test
    @DisplayName("должен проверять существование пользователя по login")
    void existsByLoginTest() {
        insertTestUser();

        // есть в БД
        assertThat(userStorage.existsByLogin("testLogin", null)).isTrue();
        // нет в БД, кроме как у текущего пользователя
        assertThat(userStorage.existsByLogin("testLogin", TEST_USER_ID)).isFalse();
        // нет такого логина в БД
        assertThat(userStorage.existsByLogin("nonexistentLogin", null)).isFalse();
    }
}
