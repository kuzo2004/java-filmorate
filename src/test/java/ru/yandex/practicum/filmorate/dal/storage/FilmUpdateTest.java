package ru.yandex.practicum.filmorate.dal.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class FilmUpdateTest extends BaseStorageTest {

    @BeforeEach
    void setUp() {
        cleanAllTables();
    }


    @Test
    @DisplayName("должен обновлять существующий фильм")
    void updateFilmTest() {

        insertTestFilm();   // вставили 1 фильм в базу

        Film previousFilmFromDB = filmStorage.findFilmById(TEST_FILM_ID)
                                             .orElseThrow(() -> new NotFoundException("Фильм не найден"));

        previousFilmFromDB.setName("Обновленное название");
        previousFilmFromDB.setDescription("Обновленное описание");
        previousFilmFromDB.setDuration(150);
        previousFilmFromDB.setMpa(new Mpa(4, null));
        previousFilmFromDB.setGenres(List.of(new Genre(3, null), new Genre(4, null)));

        Film updatedFilmFromDB = filmStorage.updateFilm(previousFilmFromDB);

        assertThat(updatedFilmFromDB.getId()).isEqualTo(TEST_FILM_ID);

        assertThat(updatedFilmFromDB)
                .usingRecursiveComparison() // сравнивает рекурсивно по всем вложенным полям
                .ignoringFields("mpa.name", "genres.name")
                .isEqualTo(previousFilmFromDB);
    }

    @Test
    @DisplayName("должен корректно обновлять фильм, удаляя все жанры")
    void updateFilmTestWithGenres() {

        insertTestFilm();   // вставили 1 фильм в базу

        Film previousFilmFromDB = filmStorage.findFilmById(TEST_FILM_ID)
                                             .orElseThrow(() -> new NotFoundException("Фильм не найден"));

        previousFilmFromDB.setGenres(List.of());

        Film updatedFilmFromDB = filmStorage.updateFilm(previousFilmFromDB);

        assertThat(updatedFilmFromDB.getId()).isEqualTo(TEST_FILM_ID);

        assertThat(updatedFilmFromDB)
                .usingRecursiveComparison() // сравнивает рекурсивно по всем вложенным полям
                .ignoringFields("mpa.name", "genres.name")
                .isEqualTo(previousFilmFromDB);

        assertThat(updatedFilmFromDB.getGenres()).isEmpty();
    }

    @Test
    @DisplayName("должен выбрасывать исключение при обновлении несуществующего фильма")
    void updateNonExistentFilmTest() {

        insertTestFilm();   // вставили 1 фильм в базу

        Film nonExistentFilm = Film.builder()
                                   .id(9999)
                                   .name("Несуществующий фильм")
                                   .build();

        assertThrows(NoSuchElementException.class, () -> filmStorage.updateFilm(nonExistentFilm));
    }
}
