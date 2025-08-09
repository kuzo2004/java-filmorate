package ru.yandex.practicum.filmorate.dal.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class UserUpdateTest extends BaseStorageTest {

    @BeforeEach
    void setUp() {
        cleanAllTables();
    }

    @Test
    @DisplayName("должен обновлять пользователя")
    void updateUserTest() {
        insertTestUser();

        User updatedUser = new User();
        updatedUser.setId(TEST_USER_ID);
        updatedUser.setEmail("updated@mail.ru");
        updatedUser.setLogin("updatedLogin");
        updatedUser.setName("Updated Name");
        updatedUser.setBirthday(LocalDate.of(1995, 5, 5));

        User userFromDB = userStorage.updateUser(updatedUser);

        assertThat(userFromDB)
                .usingRecursiveComparison()
                .isEqualTo(updatedUser);
    }
}

