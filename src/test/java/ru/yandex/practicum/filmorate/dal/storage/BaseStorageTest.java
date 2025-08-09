package ru.yandex.practicum.filmorate.dal.storage;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.dal.storage.genre.GenreDbStorage;
import ru.yandex.practicum.filmorate.dal.storage.like.LikeDbStorage;
import ru.yandex.practicum.filmorate.dal.storage.mpa.MpaDbStorage;
import ru.yandex.practicum.filmorate.dal.storage.user.UserDbStorage;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@JdbcTest
@AutoConfigureTestDatabase
@Import({FilmDbStorage.class, UserDbStorage.class, LikeDbStorage.class,
        GenreDbStorage.class, MpaDbStorage.class})
public abstract class BaseStorageTest {

    public static final int TEST_FILM_ID = 1;
    public static final int TEST_USER_ID = 1;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @Autowired
    protected FilmDbStorage filmStorage;

    @Autowired
    protected UserDbStorage userStorage;

    @Autowired
    protected LikeDbStorage likeStorage;

    @Autowired
    protected GenreDbStorage genreStorage;

    @Autowired
    protected MpaDbStorage mpaStorage;

    /* ==================== Очистка всех таблиц ==================== */

    /**
     * Гибкое очищение всех таблиц, кроме справочников genres и mpa
     */
    protected void cleanAllTables() {
        // Отключаем проверки внешних ключей (для H2), чтобы удалять таблицы в любом порядке
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE");

        // Таблицы, которые не очищаем
        List<String> skipTables = List.of("genres", "mpa");

        // Получаем список всех таблиц из метаданных
        List<String> tables = jdbcTemplate.queryForList(
                "SELECT table_name FROM information_schema.tables " +
                        "WHERE table_schema='PUBLIC'", String.class);

        // Удаляем все данные из таблиц, кроме исключённых
        tables.stream()
              .filter(t -> !skipTables.contains(t.toLowerCase()))
              .forEach(t -> jdbcTemplate.execute("TRUNCATE TABLE " + t));

        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY TRUE"); // Включаем проверки обратно
    }


    /* ==================== Заполнение списка жанров, которые уже есть в базе ==================== */

    protected static final List<Genre> EXPECTED_GENRES = List.of(
            new Genre(1, "Комедия"),
            new Genre(2, "Драма"),
            new Genre(3, "Мультфильм"),
            new Genre(4, "Триллер"),
            new Genre(5, "Документальный"),
            new Genre(6, "Боевик")
    );

    /* ==================== Заполнение списка рейтингов, которые уже есть в базе ==================== */
    protected static final List<Mpa> EXPECTED_MPAS = List.of(
            new Mpa(1, "G"),
            new Mpa(2, "PG"),
            new Mpa(3, "PG-13"),
            new Mpa(4, "R"),
            new Mpa(5, "NC-17")
    );

    /* ==================== Заполнение фильмов ==================== */
    protected void insertTestFilm() {
        jdbcTemplate.update("""
                    INSERT INTO films (id, name, description, release_date, duration, mpa_id)
                    VALUES (1, 'Матрица', 'Описание фильма матрица', '2000-03-22', 100, 1)
                """);

        jdbcTemplate.update("""
                    INSERT INTO film_genres (film_id, genre_id)
                    VALUES (1, 1)
                """);
    }

    protected void insertTestFilms(int count) {
        jdbcTemplate.batchUpdate(
                "INSERT INTO films (id, name, description, release_date, duration, mpa_id) " +
                        "VALUES (?, ?, ?, ?, ?, ?)",
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        int id = i + 1;
                        ps.setInt(1, id);
                        ps.setString(2, "Фильм " + id);
                        ps.setString(3, "Описание фильма " + id);
                        ps.setDate(4, Date.valueOf(LocalDate.now().minusYears(i)));
                        ps.setInt(5, 90 + i);
                        ps.setInt(6, (i % 5) + 1); // MPA 1..5
                    }

                    @Override
                    public int getBatchSize() {
                        return count;
                    }
                }
        );

        jdbcTemplate.batchUpdate(
                "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)",
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setInt(1, (i / 2) + 1); // 2 жанра на фильм
                        ps.setInt(2, (i % 6) + 1); // Жанры 1..6
                    }

                    @Override
                    public int getBatchSize() {
                        return count * 2;
                    }
                }
        );
    }

    /* ==================== Заполнение пользователей ==================== */

    protected void insertTestUser() {
        jdbcTemplate.update("""
                    INSERT INTO users (id, email, login, name, birthday)
                    VALUES (1, 'test@mail.ru', 'testLogin', 'Test Name', '1990-01-01')
                """);
    }

    protected void insertTestUsers(int count) {
        jdbcTemplate.batchUpdate(
                "INSERT INTO users (id, email, login, name, birthday) VALUES (?, ?, ?, ?, ?)",
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        int id = i + 1;
                        ps.setInt(1, id);
                        ps.setString(2, "user" + id + "@mail.ru");
                        ps.setString(3, "login" + id);
                        ps.setString(4, "Name " + id);
                        ps.setDate(5, Date.valueOf(LocalDate.now().minusYears(20 + i)));
                    }

                    @Override
                    public int getBatchSize() {
                        return count;
                    }
                }
        );
    }

    protected void insertFriends(int userId, List<Integer> friendIds) {
        jdbcTemplate.batchUpdate(
                "INSERT INTO friends (user_id, friend_id) VALUES (?, ?)",
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setInt(1, userId);
                        ps.setInt(2, friendIds.get(i));
                    }

                    @Override
                    public int getBatchSize() {
                        return friendIds.size();
                    }
                }
        );
    }
    /* ==================== Лайки вставить массово  ==================== */

    protected void insertTestLikes(Map<Integer, List<Integer>> filmToUsersLikes) {
        List<Integer> filmIds = new ArrayList<>();
        List<Integer> userIds = new ArrayList<>();

        filmToUsersLikes.forEach((filmId, users) -> {
            users.forEach(userId -> {
                filmIds.add(filmId);
                userIds.add(userId);
            });
        });

        jdbcTemplate.batchUpdate(
                "INSERT INTO likes (film_id, user_id) VALUES (?, ?)",
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setInt(1, filmIds.get(i));
                        ps.setInt(2, userIds.get(i));
                    }

                    @Override
                    public int getBatchSize() {
                        return filmIds.size();
                    }
                }
        );
    }
}

