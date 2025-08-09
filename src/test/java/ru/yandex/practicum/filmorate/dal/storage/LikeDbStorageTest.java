package ru.yandex.practicum.filmorate.dal.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatNoException;

public class LikeDbStorageTest extends BaseStorageTest {
    @BeforeEach
    void setUp() {
        cleanAllTables();
    }

    @Test
    @DisplayName("должен возвращать false при проверке лайка несуществующей пары фильм-пользователь")
    void isFilmLikedByUserFalseTest() {
        insertTestFilm();
        insertTestUser();

        boolean likesFromDb = likeStorage.isFilmLikedByUser(TEST_FILM_ID, TEST_USER_ID);
        assertThat(likesFromDb).isFalse();
    }

    @Test
    @DisplayName("должен возвращать true, при проверке на существование, после добавления лайка")
    void isFilmLikedByUserTrueTest() {
        insertTestFilm();
        insertTestUser();
        likeStorage.addLike(TEST_FILM_ID, TEST_USER_ID);

        boolean likesFromDb = likeStorage.isFilmLikedByUser(TEST_FILM_ID, TEST_USER_ID);
        assertThat(likesFromDb).isTrue();
    }

    @Test
    @DisplayName("должен успешно добавлять лайк")
    void addLikeSuccessfullyTest() {
        insertTestFilm();
        insertTestUser();

        likeStorage.addLike(TEST_FILM_ID, TEST_USER_ID);

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM likes WHERE film_id = ? AND user_id = ?",
                Integer.class,
                TEST_FILM_ID, TEST_USER_ID
        );
        assertThat(count).isEqualTo(1);
    }


    @Test
    @DisplayName("должен успешно удалять лайк")
    void removeLikeSuccessfully() {
        insertTestFilm();
        insertTestUser();
        likeStorage.addLike(TEST_FILM_ID, TEST_USER_ID);

        likeStorage.removeLike(TEST_FILM_ID, TEST_USER_ID);

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM likes WHERE film_id = ? AND user_id = ?",
                Integer.class,
                TEST_FILM_ID, TEST_USER_ID
        );
        assertThat(count).isEqualTo(0);
    }

    @Test
    @DisplayName("не должен падать при удалении несуществующего лайка")
    void removeLikeIfNonExistentLike() {
        insertTestFilm();
        insertTestUser();

        assertThatNoException().isThrownBy(() -> {
            likeStorage.removeLike(TEST_FILM_ID, TEST_USER_ID);
        });
    }

    @Test
    @DisplayName("должен корректно работать с несколькими лайками")
    void shouldWorkWithMultipleLikes() {
        // Создаем 5 фильмов и 5 пользователей
        insertTestFilms(5);
        insertTestUsers(5);

        // Добавляем лайки: каждый пользователь лайкает все фильмы с id <= своему id
        for (int userId = 1; userId <= 5; userId++) {
            for (int filmId = 1; filmId <= userId; filmId++) {
                likeStorage.addLike(filmId, userId);
            }
        }

        // Проверяем количество лайков для каждого фильма
        for (int filmId = 1; filmId <= 5; filmId++) {
            int expectedLikes = 5 - filmId + 1;
            Integer actualLikes = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM likes WHERE film_id = ?",
                    Integer.class,
                    filmId
            );
            assertThat(actualLikes).isEqualTo(expectedLikes);
        }

        // Удаляем все лайки для фильма с id=3
        for (int userId = 1; userId <= 5; userId++) {
            likeStorage.removeLike(3, userId);
        }

        // Проверяем, что лайки для фильма 3 удалены
        Integer likesForFilm3 = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM likes WHERE film_id = 3",
                Integer.class
        );
        assertThat(likesForFilm3).isEqualTo(0);
    }
}
