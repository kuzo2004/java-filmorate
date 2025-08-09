package ru.yandex.practicum.filmorate.dal.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class UserFriendsTest extends BaseStorageTest {

    @BeforeEach
    void setUp() {
        cleanAllTables();
    }

    @Test
    @DisplayName("должен добавлять друга")
    void addFriendTest() {
        insertTestUsers(2); // Добавляем двух пользователей

        userStorage.addFriend(1, 2);

        List<Integer> friends = jdbcTemplate.queryForList(
                "SELECT friend_id FROM friends WHERE user_id = 1", Integer.class);

        assertThat(friends).containsExactly(2);
    }

    @Test
    @DisplayName("должен удалять друга")
    void removeFriendTest() {
        insertTestUsers(2);
        insertFriends(1, List.of(2));

        userStorage.removeFriend(1, 2);

        List<Integer> friends = jdbcTemplate.queryForList(
                "SELECT friend_id FROM friends WHERE user_id = 1", Integer.class);

        assertThat(friends).isEmpty();
    }

    @Test
    @DisplayName("должен возвращать список друзей")
    void getFriendsTest() {
        insertTestUsers(3);
        insertFriends(1, List.of(2, 3));

        List<User> friends = userStorage.getFriends(1);

        assertThat(friends).hasSize(2);
        assertThat(friends).extracting(User::getId).containsExactlyInAnyOrder(2, 3);
    }

    @Test
    @DisplayName("должен возвращать список общих друзей")
    void getCommonFriendsIdsTest() {
        insertTestUsers(4);
        insertFriends(1, List.of(2, 3)); // У пользователя 1 друзья 2 и 3
        insertFriends(4, List.of(2, 3)); // У пользователя 4 друзья 2 и 3

        List<User> commonFriends = userStorage.getCommonFriends(1, 4);

        assertThat(commonFriends).extracting(User::getId).containsExactlyInAnyOrder(2, 3);
    }
}
