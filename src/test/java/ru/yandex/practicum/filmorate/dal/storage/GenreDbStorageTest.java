package ru.yandex.practicum.filmorate.dal.storage;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class GenreDbStorageTest extends BaseStorageTest {


    @BeforeEach
    void setUp() {
        // Жанры не очищаем, так как это справочная таблица
        cleanAllTables();
    }

    @Test
    @DisplayName("должен возвращать все жанры")
    void getAllGenresTest() {
        // при запуске Spring создается таблица в БД с данными из data.sql, из нее извлекаем запросом весь справочник
        List<Genre> genresFromDb = genreStorage.getAllGenres();

        // сравниваем список из БД с константным ожидаемым списком EXPECTED_GENRES
        assertThat(genresFromDb)
                .isNotEmpty()
                .hasSize(6)
                .usingRecursiveComparison()
                .isEqualTo(EXPECTED_GENRES);
    }

    @Test
    @DisplayName("должен возвращать жанр по существующему id")
    void getGenreByIdTest() {
        for (Genre expectedGenre : EXPECTED_GENRES) {
            Optional<Genre> genreFromDb = genreStorage.getGenreById(expectedGenre.getId());

            assertThat(genreFromDb)
                    .isPresent()
                    .get()
                    .usingRecursiveComparison()
                    .isEqualTo(expectedGenre);
        }
    }

    @Test
    @DisplayName("должен возвращать пустой Optional для несуществующего id жанра")
    void getGenreByIdIfNotExistsTest() {
        Optional<Genre> genreFromDb = genreStorage.getGenreById(999);

        assertThat(genreFromDb).isEmpty();
    }

    @Test
    @DisplayName("должен возвращать true для существующих id жанров")
    void existsByIdTrueTest() {
        for (Genre genre : EXPECTED_GENRES) {
            assertThat(genreStorage.existsById(genre.getId())).isTrue();
        }
    }

    @Test
    @DisplayName("должен возвращать false для несуществующего id жанра")
    void existsByIdFalseTest() {
        assertThat(genreStorage.existsById(999)).isFalse();
    }
}
