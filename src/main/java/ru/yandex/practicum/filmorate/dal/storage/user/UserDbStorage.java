package ru.yandex.practicum.filmorate.dal.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dal.mappers.UserMapper;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.Date;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Component
@Qualifier("userDbStorage")
public class UserDbStorage implements UserStorage {

    private final NamedParameterJdbcTemplate namedJdbcTemplate;

    public UserDbStorage(NamedParameterJdbcTemplate namedJdbcTemplate) {

        this.namedJdbcTemplate = namedJdbcTemplate;
    }


    @Override
    public User addUser(User user) {
        String sql = """
                INSERT INTO users (email, login, name, birthday) 
                VALUES (:email, :login, :name, :birthday)
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("email", user.getEmail())
                .addValue("login", user.getLogin())
                .addValue("name", user.getName())
                .addValue("birthday", Date.valueOf(user.getBirthday()));

        namedJdbcTemplate.update(
                sql,
                params,
                keyHolder,
                new String[]{"id"} // Имя колонки для возврата сгенерированного ID
        );

        user.setId(Objects.requireNonNull(keyHolder.getKey()).intValue());
        return user;
    }

    @Override
    public User updateUser(User user) {
        String sql = """
                UPDATE users 
                SET email = :email, login = :login, name = :name, birthday = :birthday 
                WHERE id = :id
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("email", user.getEmail())
                .addValue("login", user.getLogin())
                .addValue("name", user.getName())
                .addValue("birthday", Date.valueOf(user.getBirthday()))
                .addValue("id", user.getId());

        namedJdbcTemplate.update(sql, params);

        // Удалили строки из таблицы friends, ассоциированные со старой версией объект (по id)
        delUserFriendsRelations(user);

        //  Добавляем вновь пришедшие связи в friends
        addUserFriendsRelations(user);

        return user;
    }

    @Override
    public Optional<User> findUserById(int id) {
        String sql = """
                SELECT * 
                FROM users 
                WHERE id = :id
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", id);

        return namedJdbcTemplate.query(sql, params, new UserMapper())
                                .stream()
                                .findFirst()
                                .map(this::loadFriendsIdsIntoUser);
    }

    @Override
    public Collection<User> getAllUsers() {
        String sql = "SELECT * FROM users";
        return namedJdbcTemplate.query(sql, new UserMapper());
    }

    // проверка существования пользователя
    @Override
    public boolean existsById(int userId) {
        String sql = "SELECT COUNT(*) > 0 FROM users WHERE id = :id";

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", userId);

        return Boolean.TRUE.equals(namedJdbcTemplate.queryForObject(sql, params, Boolean.class));
    }

    // проверка совпадения адреса почты в БД (кроме как у текущего пользователя, если пользователь есть)
    @Override
    public boolean existsByEmail(String email, Integer excludeUserId) {
        String sql = excludeUserId != null
                ? "SELECT COUNT(*) > 0 FROM users WHERE email = :email AND id <> :excludeUserId"
                : "SELECT COUNT(*) > 0 FROM users WHERE email = :email";

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("email", email);
        if (excludeUserId != null) {
            params.addValue("excludeUserId", excludeUserId);
        }

        return Boolean.TRUE.equals(namedJdbcTemplate.queryForObject(sql, params, Boolean.class));
    }

    // проверка совпадения логина в БД (кроме как у текущего пользователя, если пользователь есть)
    @Override
    public boolean existsByLogin(String login, Integer excludeUserId) {
        String sql = excludeUserId != null
                ? "SELECT COUNT(*) > 0 FROM users WHERE login = :login AND id <> :excludeUserId"
                : "SELECT COUNT(*) > 0 FROM users WHERE login = :login";

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("login", login);
        if (excludeUserId != null) {
            params.addValue("excludeUserId", excludeUserId);
        }

        return Boolean.TRUE.equals(namedJdbcTemplate.queryForObject(sql, params, Boolean.class));
    }

    @Override
    public void addFriend(int userId, int friendId) {
        String sql = """
                INSERT INTO friends (user_id, friend_id) 
                VALUES (:userId, :friendId)
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("friendId", friendId);

        namedJdbcTemplate.update(sql, params);
    }

    @Override
    public void removeFriend(int userId, int friendId) {
        String sql = """
                DELETE FROM friends 
                WHERE user_id = :userId AND friend_id = :friendId
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("friendId", friendId);

        namedJdbcTemplate.update(sql, params);
    }

    @Override
    // список всех друзей (объектов user) пользователя (без свойства friends, ленивая загрузка)
    public List<User> getFriends(int userId) {
        String sql = """
                SELECT u.* 
                FROM users u 
                JOIN friends f ON u.id = f.friend_id 
                WHERE f.user_id = :userId
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId);

        return namedJdbcTemplate.query(sql, params, new UserMapper());
    }

    @Override
    // список всех друзей (объектов user) пользователя (без свойства friends, ленивая загрузка)
    public List<User> getCommonFriends(int userId, int otherId) {
        List<Integer> commonFriendsIds = getCommonFriendsIds(userId, otherId);

        if (commonFriendsIds.isEmpty()) {
            return List.of();
        }

        return getUsersFromListIds(commonFriendsIds);
    }

    // список пользователей на основе их ids
    private List<User> getUsersFromListIds(List<Integer> ids) {

        String sql = """
                SELECT * 
                FROM users
                WHERE id IN (:ids) 
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("ids", ids);

        return namedJdbcTemplate.query(sql, params, new UserMapper());
    }

    // список ids общих друзей между пользователем и другим пользователем
    private List<Integer> getCommonFriendsIds(int userId, int otherId) {
        String sql = """
                SELECT f1.friend_id 
                FROM users u 
                JOIN friends f1 ON u.id = f1.user_id 
                JOIN friends f2 ON f1.friend_id = f2.friend_id 
                WHERE f1.user_id = :userId AND f2.user_id = :otherId
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("otherId", otherId);

        return namedJdbcTemplate.queryForList(sql, params, Integer.class);
    }

    // выборка из таблицы friends всех id друзей, связанных с пользователем и обновление их у пользователя
    private User loadFriendsIdsIntoUser(User user) {
        String sql = """
                SELECT friend_id 
                FROM friends 
                WHERE user_id = :userId
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", user.getId());

        List<Integer> friendsIds = namedJdbcTemplate.queryForList(
                sql,
                params,
                Integer.class
        );

        user.setFriends(new HashSet<>(friendsIds));
        return user;
    }

    private void delUserFriendsRelations(User user) {
        String sql = """
                DELETE FROM friends 
                WHERE user_id = :userId 
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", user.getId());


        namedJdbcTemplate.update(sql, params);
    }

    private void addUserFriendsRelations(User user) {

        Set<Integer> friends = user.getFriends();
        String sql = """
                INSERT INTO friends (user_id, friend_id) 
                VALUES (:userId, :friendId)
                """;

        MapSqlParameterSource[] batchParams = friends.stream()
                                                     .map(id -> new MapSqlParameterSource()
                                                             .addValue("userId", user.getId())
                                                             .addValue("friendId", id))
                                                     .toArray(MapSqlParameterSource[]::new);

        namedJdbcTemplate.batchUpdate(sql, batchParams);
    }
}
