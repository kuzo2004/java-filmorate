package ru.yandex.practicum.filmorate.dal.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class MpaDbStorageTest extends BaseStorageTest {


    @BeforeEach
    void setUp() {
        // Таблицу mpa не очищаем, так как это справочная таблица
        cleanAllTables();
    }

    @Test
    @DisplayName("должен возвращать все рейтинги MPA")
    void getAllMpa_shouldReturnAllMpaRatings() {
        // при запуске Spring создается таблица в БД с данными из data.sql, из нее извлекаем запросом весь справочник
        List<Mpa> mpasFromDb = mpaStorage.getAllMpa();

        // сравниваем список из БД с константным ожидаемым списком EXPECTED_MPAS
        assertThat(mpasFromDb)
                .isNotEmpty()
                .hasSize(5)
                .usingRecursiveComparison()
                .isEqualTo(EXPECTED_MPAS);
    }

    @Test
    @DisplayName("должен возвращать рейтинг MPA по существующему id")
    void findMpaById_shouldReturnMpaForExistingId() {
        for (Mpa expectedMpa : EXPECTED_MPAS) {
            Optional<Mpa> mpaFromDb = mpaStorage.findMpaById(expectedMpa.getId());

            assertThat(mpaFromDb)
                    .isPresent()
                    .get()
                    .usingRecursiveComparison()
                    .isEqualTo(expectedMpa);
        }
    }

    @Test
    @DisplayName("должен возвращать пустой Optional для несуществующего id рейтинга MPA")
    void findMpaByIdIfNonExistId() {
        Optional<Mpa> mpaFromDb = mpaStorage.findMpaById(999);

        assertThat(mpaFromDb).isEmpty();
    }

    @Test
    @DisplayName("должен возвращать true для существующих id рейтингов MPA")
    void existsByIdTrueTest() {
        for (Mpa mpa : EXPECTED_MPAS) {
            assertThat(mpaStorage.existsById(mpa.getId())).isTrue();
        }
    }

    @Test
    @DisplayName("должен возвращать false для несуществующего id рейтинга MPA")
    void existsByIdFalseTest() {
        assertThat(mpaStorage.existsById(999)).isFalse();
    }


    @Test
    @DisplayName("должен возвращать корректные названия для всех рейтингов MPA")
    void shouldReturnCorrectNamesForAllMpaRatings() {
        List<Mpa> mpasFromBd = mpaStorage.getAllMpa();

        assertThat(mpasFromBd)
                .extracting(Mpa::getName)
                .containsExactly("G", "PG", "PG-13", "R", "NC-17");
    }
}
