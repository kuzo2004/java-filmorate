package ru.yandex.practicum.filmorate.dal.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class FilmInsertTest extends BaseStorageTest {

    @BeforeEach
    void setUp() {
        cleanAllTables();
    }

    @Test
    @DisplayName("должен добавлять новый фильм")
    void addFilmTest() {

        Film expectedFilm = Film.builder()
                                .id(TEST_FILM_ID)
                                .name("Матрица")
                                .description("Описание фильма матрица")
                                .releaseDate(LocalDate.of(2000, 3, 22))
                                .duration(100)
                                .mpa(new Mpa(1, null))
                                .genres(List.of(new Genre(1, null)))
                                .build();

        Film filmFromDB = filmStorage.addFilm(expectedFilm);

        assertThat(filmFromDB.getId()).isPositive();

        assertThat(filmFromDB)
                .usingRecursiveComparison() // сравнивает рекурсивно по всем вложенным полям
                .ignoringFields("mpa.name", "genres.name")
                .isEqualTo(expectedFilm);

        // mpa.name, genres.name - не проверяем, т.к. они приходят null
    }

    @Test
    @DisplayName("должен добавлять новый фильм с несколькими жанрами")
    void addFilmTestWithTwoGenres() {

        Film expectedFilm = Film.builder()
                                .id(TEST_FILM_ID)
                                .name("Матрица")
                                .description("Описание фильма матрица")
                                .releaseDate(LocalDate.of(2000, 3, 22))
                                .duration(100)
                                .mpa(new Mpa(1, null))
                                .genres(List.of(new Genre(1, null), new Genre(2, null)))
                                .build();

        Film filmFromDB = filmStorage.addFilm(expectedFilm);

        assertThat(filmFromDB.getId()).isPositive();

        assertThat(filmFromDB)
                .usingRecursiveComparison() // сравнивает рекурсивно по всем вложенным полям
                .ignoringFields("mpa.name", "genres.name")
                .isEqualTo(expectedFilm);
    }
}
