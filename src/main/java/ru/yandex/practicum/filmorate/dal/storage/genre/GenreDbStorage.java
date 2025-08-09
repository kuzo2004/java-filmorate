package ru.yandex.practicum.filmorate.dal.storage.genre;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dal.mappers.GenreMapper;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class GenreDbStorage implements GenreStorage {
    private final NamedParameterJdbcTemplate namedJdbcTemplate;

    public GenreDbStorage(NamedParameterJdbcTemplate namedJdbcTemplate) {

        this.namedJdbcTemplate = namedJdbcTemplate;
    }

    @Override
    public List<Genre> getAllGenres() {
        String sql = "SELECT * FROM genres";

        return namedJdbcTemplate.query(sql, new GenreMapper());
    }

    @Override
    public Optional<Genre> getGenreById(int id) {
        String sql = "SELECT * FROM genres WHERE id = :id";

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", id);

        return namedJdbcTemplate.query(sql, params, new GenreMapper())
                                .stream()
                                .findFirst();
    }

    // проверка существования жанра
    @Override
    public boolean existsById(int id) {
        String sql = "SELECT COUNT(*) > 0 FROM genres WHERE id = :id";

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", id);

        return Boolean.TRUE.equals(namedJdbcTemplate.queryForObject(sql, params, Boolean.class));
    }
}
