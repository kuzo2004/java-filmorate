package ru.yandex.practicum.filmorate.dal.storage.like;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class LikeDbStorage implements LikeStorage {

    private final NamedParameterJdbcTemplate namedJdbcTemplate;

    public LikeDbStorage(NamedParameterJdbcTemplate namedJdbcTemplate) {

        this.namedJdbcTemplate = namedJdbcTemplate;
    }

    // проверка существования лайка
    @Override
    public boolean isFilmLikedByUser(int filmId, int userId) {
        String sql = "SELECT COUNT(*) > 0 FROM likes WHERE film_id = :filmId AND user_id = :userId";

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("filmId", filmId)
                .addValue("userId", userId);

        return Boolean.TRUE.equals(namedJdbcTemplate.queryForObject(sql, params, Boolean.class));
    }

    @Override
    public void addLike(int filmId, int userId) {
        String sql = "INSERT INTO likes (film_id, user_id) VALUES (:filmId, :userId)";

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("filmId", filmId)
                .addValue("userId", userId);

        namedJdbcTemplate.update(sql, params);
    }

    @Override
    public void removeLike(int filmId, int userId) {
        String sql = "DELETE FROM likes WHERE film_id =:filmId AND user_id = :userId";

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("filmId", filmId)
                .addValue("userId", userId);

        namedJdbcTemplate.update(sql, params);
    }
}
