package ru.yandex.practicum.filmorate.dal.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

public class UserInsertTest extends BaseStorageTest {

    @BeforeEach
    void setUp() {
        cleanAllTables();
    }

    @Test
    @DisplayName("должен добавлять нового пользователя")
    void addUserTest() {
        User expectedUser = User.builder()
                                .id(TEST_USER_ID)
                                .email("new@mail.ru")
                                .login("newLogin")
                                .name("New User")
                                .birthday(LocalDate.of(2000, 3, 22))
                                .build();


        User userFromDB = userStorage.addUser(expectedUser);

        assertThat(userFromDB.getId()).isPositive();

        assertThat(userFromDB)
                .usingRecursiveComparison() // сравнивает рекурсивно по всем вложенным полям
                .isEqualTo(expectedUser);
    }
}

