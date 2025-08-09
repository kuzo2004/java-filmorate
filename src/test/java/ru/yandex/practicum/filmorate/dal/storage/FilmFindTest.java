package ru.yandex.practicum.filmorate.dal.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class FilmFindTest extends BaseStorageTest {

    @BeforeEach
    void setUp() {
        cleanAllTables();
    }

    @Test
    @DisplayName("должен находить фильм по id")
    void findFilmByIdTest() {

        insertTestFilm();   // вставили 1 фильм в базу

        Film expectedFilm = Film.builder()
                                .id(TEST_FILM_ID)
                                .name("Матрица")
                                .description("Описание фильма матрица")
                                .releaseDate(LocalDate.of(2000, 3, 22))
                                .duration(100)
                                .mpa(new Mpa(1, null))
                                .genres(List.of(new Genre(1, null)))
                                .build();

        Film filmFromDB = filmStorage.findFilmById(TEST_FILM_ID)
                                     .orElseThrow(() -> new NotFoundException("Фильм с ID " + TEST_FILM_ID + " не найден"));

        assertThat(filmFromDB)
                .usingRecursiveComparison() // сравнивает рекурсивно по всем вложенным полям
                .ignoringFields("mpa.name", "genres.name")
                .isEqualTo(expectedFilm);

        assertThat(filmFromDB.getMpa())
                .extracting(Mpa::getName)   // извлекаем название из объекта Mpa
                .isEqualTo("G");   // проверяем, что оно равно "G"

        assertThat(filmFromDB.getGenres())
                .extracting(Genre::getName) // извлекаем названия жанров
                .containsExactly("Комедия"); // должен быть ровно 1 жанр "Комедия"
    }

    @Test
    @DisplayName("должен возвращать пустой Optional при поиске несуществующего фильма")
    void findNonExistentFilmByIdTest() {

        insertTestFilm();   // вставили 1 фильм в базу c id =1

        Optional<Film> result = filmStorage.findFilmById(9999);
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("должен возвращать все фильмы")
    void getAllFilmsTest() {

        insertTestFilms(10);

        Collection<Film> films = filmStorage.getAllFilms();

        assertThat(films).isNotEmpty();
        assertThat(films).hasSize(10);
    }

    @Test
    @DisplayName("должен возвращать топ популярных фильмов по лайкам")
    void getPopularFilmsTest() {
        // Вставляем 3 фильма
        insertTestFilms(3);

        // Вставляем 3 пользователей
        insertTestUsers(3);

        // Лайки: фильм 2 — 3 лайка, фильм 1 — 2 лайка, фильм 3 — 1 лайк
        insertTestLikes(Map.of(
                1, List.of(1, 2),
                2, List.of(1, 2, 3),
                3, List.of(3)
        ));

        // Запрашиваем топ-2 популярных фильмов
        List<Film> popularFilms = filmStorage.getPopularFilms(2);

        // Проверяем, что вернулось ровно 2 фильма
        assertThat(popularFilms).hasSize(2);

        // Проверяем, что первым идёт фильм 2 (3 лайка), потом фильм 1 (2 лайка)
        assertThat(popularFilms.get(0).getId()).isEqualTo(2);
        assertThat(popularFilms.get(1).getId()).isEqualTo(1);

        // Проверяем, что MPA подгружен
        assertThat(popularFilms)
                .allSatisfy(f -> assertThat(f.getMpa()).isNotNull());

        // Проверяем, что жанры подгружены и отсортированы по id
        assertThat(popularFilms)           // 1. Берём коллекцию фильмов
            .allSatisfy(f ->          // 2. Для КАЖДОГО фильма `f` проверяем условие:
                assertThat(f.getGenres())  // 3. Берём жанры фильма `f`
                .extracting(Genre::getId)  // 4. Извлекаем Id каждого жанра
                 .isSorted()               // 5. Проверяем, что Ids идут в порядке возрастания
        );
    }

    @Test
    @DisplayName("должен проверять существование фильма")
    void existsByIdTest() {

        insertTestFilm();   // вставили 1 фильм в базу c id =1

        assertThat(filmStorage.existsById(TEST_FILM_ID)).isTrue();
        assertThat(filmStorage.existsById(9999)).isFalse();
    }
}
