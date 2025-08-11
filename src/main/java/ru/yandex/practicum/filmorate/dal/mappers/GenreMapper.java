package ru.yandex.practicum.filmorate.dal.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class GenreMapper implements RowMapper<Genre> {
    private final String idColumn;
    private final String nameColumn;

    // Конструктор по умолчанию — для запросов напрямую к таблице genres
    public GenreMapper() {
        this("id", "name");
    }

    // Конструктор с настраиваемыми именами колонок (для JOIN-ов)
    public GenreMapper(String idColumn, String nameColumn) {
        this.idColumn = idColumn;
        this.nameColumn = nameColumn;
    }

    @Override
    public Genre mapRow(ResultSet rs, int rowNum) throws SQLException {
        Genre genre = new Genre();
        genre.setId(rs.getInt(idColumn));
        genre.setName(rs.getString(nameColumn));
        return genre;
    }
}