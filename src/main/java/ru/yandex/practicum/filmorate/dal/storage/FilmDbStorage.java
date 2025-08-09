package ru.yandex.practicum.filmorate.dal.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dal.mappers.FilmMapper;
import ru.yandex.practicum.filmorate.dal.mappers.GenreMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Component
@Qualifier("filmDbStorage")
public class FilmDbStorage implements FilmStorage {

    private final NamedParameterJdbcTemplate namedJdbcTemplate;

    public FilmDbStorage(NamedParameterJdbcTemplate namedJdbcTemplate) {

        this.namedJdbcTemplate = namedJdbcTemplate;
    }

    @Override
    public Film addFilm(Film film) {
        String sql = """
                INSERT INTO films (name, description, release_date, duration, mpa_id) 
                VALUES (:name, :description, :releaseDate, :duration, :mpaId)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("name", film.getName())
                .addValue("description", film.getDescription())
                .addValue("releaseDate", Date.valueOf(film.getReleaseDate()))
                .addValue("duration", film.getDuration())
                .addValue("mpaId", film.getMpa().getId());

        namedJdbcTemplate.update(
                sql,
                params,
                keyHolder,
                new String[]{"id"} // Имя колонки для возврата сгенерированного ID
        );

        film.setId(Objects.requireNonNull(keyHolder.getKey()).intValue());

        // Добавляем связи в film_genres
        addFilmGenresRelations(film);

        return film;
    }

    @Override
    public Film updateFilm(Film film) {

        Film existingFilm = findFilmById(film.getId()).get();

        String sql = """
                UPDATE films 
                SET name = :name, 
                description = :description, 
                release_date = :releaseDate, 
                duration = :duration, 
                mpa_id = :mpaId 
                WHERE id = :id
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("name", film.getName())
                .addValue("description", film.getDescription())
                .addValue("releaseDate", Date.valueOf(film.getReleaseDate()))
                .addValue("duration", film.getDuration())
                .addValue("mpaId", film.getMpa().getId())
                .addValue("id", film.getId());

        namedJdbcTemplate.update(sql, params);

        // Удалили строки из таблицы film_genres, ассоциированные со старой версией объект (по id)
        delFilmGenresRelations(existingFilm);

        //  Добавляем вновь пришедшие связи в film_genres
        addFilmGenresRelations(film);

        return film;
    }


    public Optional<Film> findFilmById(int id) {
        String sql = """
                SELECT f.*, m.name AS mpa_name 
                FROM films f 
                JOIN mpa m ON f.mpa_id = m.id 
                WHERE f.id = :id
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", id);

        return namedJdbcTemplate.query(sql, params, new FilmMapper())
                                .stream()
                                .findFirst()
                                .map(this::loadGenresIntoFilm);   //  жанры вставляем
    }


    // проверка существования фильма
    @Override
    public boolean existsById(int filmId) {
        String sql = "SELECT COUNT(*) > 0 FROM films WHERE id = :id";

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", filmId);

        return Boolean.TRUE.equals(namedJdbcTemplate.queryForObject(sql, params, Boolean.class));
    }

    @Override
    public Collection<Film> getAllFilms() {
        // 1. Получаем все фильмы с информацией о MPA, в том числе имя mpa вытащили из справочника
        String filmsSql = """
                SELECT f.*, m.name AS mpa_name 
                FROM films f 
                JOIN mpa m ON f.mpa_id = m.id
                """;
        List<Film> films = namedJdbcTemplate.query(filmsSql, new FilmMapper());

        if (films.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. Получаем все жанры для всех фильмов и мэтчим их с именами жанров из справочника жанров
        String genresSql = """
                SELECT fg.film_id, g.id AS genre_id, g.name AS genre_name
                FROM film_genres fg
                JOIN genres g ON fg.genre_id = g.id
                """;

        // мапа - каждый фильм со своим списком жанров
        Map<Integer, List<Genre>> filmGenresMap = new HashMap<>();

        namedJdbcTemplate.query(genresSql,
                rs -> {
                    int filmId = rs.getInt("film_id");  // Получаем id фильма из текущей строки
                    Genre genre = new GenreMapper().mapRow(rs, rs.getRow()); // Преобразуем строку в объект Genre

                    // все жанры сохраняем по порядку id (требование Postman-тестов) поэтому ArrayList
                    filmGenresMap.computeIfAbsent(filmId, k -> new ArrayList<>()) // если нет ключа filmId, вставляем его
                                 .add(genre); // теперь ключ есть, добавить жанр
                });

        // Связывание фильмов с жанрами
        films.forEach(film -> {
            film.setGenres(filmGenresMap.getOrDefault(film.getId(), new ArrayList<>())); // вставляем из мапы, иначе new
        });
        return films;
    }

    @Override
    public List<Film> getPopularFilms(int count) {
        String sql = """
                SELECT f.*, m.name AS mpa_name, COUNT(lk.user_id) AS likes_count
                FROM films f
                JOIN mpa m ON f.mpa_id = m.id
                LEFT JOIN likes lk ON f.id = lk.film_id
                GROUP BY f.id
                ORDER BY likes_count DESC, f.id
                LIMIT :count
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("count", count);

        List<Film> films = namedJdbcTemplate.query(sql, params, new FilmMapper());

        if (films.isEmpty()) {
            return Collections.emptyList();
        }

        // Получаем ID всех найденных фильмов
        List<Integer> filmIds = films.stream()
                                     .map(Film::getId)
                                     .toList();


        // 2. Получаем все жанры для фильмов с использованием GenreMapper
        String genresSql = """
                SELECT fg.film_id, g.id AS genre_id, g.name AS genre_name
                FROM film_genres fg
                JOIN genres g ON fg.genre_id = g.id
                WHERE fg.film_id IN (:filmIds)
                """;

        MapSqlParameterSource paramsGenre = new MapSqlParameterSource()
                .addValue("filmIds", filmIds);

        Map<Integer, List<Genre>> filmGenresMap = new HashMap<>();

        namedJdbcTemplate.query(genresSql,
                paramsGenre,
                rs -> {
                    int filmId = rs.getInt("film_id");
                    Genre genre = new GenreMapper().mapRow(rs, rs.getRow());
                    filmGenresMap.computeIfAbsent(filmId, k -> new ArrayList<>())
                                 .add(genre);
                });

        // Устанавливаем жанры для каждого фильма (с сортировкой по id)
        films.forEach(film -> {
            List<Genre> genres = filmGenresMap.getOrDefault(film.getId(), Collections.emptyList());
            genres.sort(Comparator.comparingInt(Genre::getId));
            film.setGenres(genres);
        });
        return films;
    }

    // удаление из таблицы связи фильм-жанры
    private void delFilmGenresRelations(Film film) {
        String sql = """
                DELETE FROM film_genres 
                WHERE film_id = :id
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", film.getId());

        namedJdbcTemplate.update(sql, params);
    }

    // вставка в таблицу связи фильм-жанры
    private void addFilmGenresRelations(Film film) {
        // проверка на существование жанров уже сделана на этапе service

        List<Genre> genresList = film.getGenres();

        String sql = """
                INSERT INTO film_genres (film_id, genre_id) 
                VALUES (:filmId, :genreId)
                """;
        // Создаем массив параметров для batchUpdate
        MapSqlParameterSource[] batchParams = genresList.stream()
                                                        .map(genre -> new MapSqlParameterSource()
                                                                .addValue("filmId", film.getId())
                                                                .addValue("genreId", genre.getId()))
                                                        .toArray(MapSqlParameterSource[]::new);

        // Пакетная вставка
        namedJdbcTemplate.batchUpdate(sql, batchParams);
    }


    // выгрузка из таблицы фильм-жанры всех записей без фильтров
    private List<Genre> getFilmGenresRelations(Film film) {
        String sql = """
                SELECT g.id AS genre_id, g.name AS genre_name 
                FROM genres g 
                JOIN film_genres fg 
                ON g.id = fg.genre_id 
                WHERE fg.film_id = :filmId 
                ORDER BY g.id
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("filmId", film.getId());

        return namedJdbcTemplate.query(sql, params, new GenreMapper());
    }

    // загрузка в фильм его жанров из таблицы связи (с id и именем жанра).
    private Film loadGenresIntoFilm(Film film) {

        // вытащим все жанры фильма из таблицы film_genres и дополним названием из справочника жанров
        List<Genre> genres = getFilmGenresRelations(film);


        // вставим в фильм список жанров
        film.setGenres(genres);

        return film;
    }
}
