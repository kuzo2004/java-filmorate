package ru.yandex.practicum.filmorate.dal.storage.mpa;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dal.mappers.MpaMapper;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class MpaDbStorage implements MpaStorage {

    private final NamedParameterJdbcTemplate namedJdbcTemplate;

    public MpaDbStorage(NamedParameterJdbcTemplate namedJdbcTemplate) {

        this.namedJdbcTemplate = namedJdbcTemplate;
    }

    @Override
    public List<Mpa> getAllMpa() {
        String sql = "SELECT * FROM mpa";
        return namedJdbcTemplate.query(sql, new MpaMapper());
    }

    @Override
    public Optional<Mpa> findMpaById(int id) {
        String sql = " SELECT * FROM mpa WHERE id = :id";

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", id);

        return namedJdbcTemplate.query(sql, params, new MpaMapper())
                                .stream()
                                .findFirst();
    }

    // проверка существования жанра
    @Override
    public boolean existsById(int id) {
        String sql = "SELECT COUNT(*) > 0 FROM mpa WHERE id = :id";

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", id);

        return Boolean.TRUE.equals(namedJdbcTemplate.queryForObject(sql, params, Boolean.class));
    }
}
