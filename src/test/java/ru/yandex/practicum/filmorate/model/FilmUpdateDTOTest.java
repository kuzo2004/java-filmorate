package ru.yandex.practicum.filmorate.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

class FilmUpdateDTOTest {
    private static Validator validator;

    @BeforeAll
    static void setup() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void whenIdIsNull_thenValidationFails() {
        FilmUpdateDTO dto = new FilmUpdateDTO();
        dto.setName("Home Alone");
        dto.setReleaseDate(LocalDate.now());
        dto.setDuration(120);

        Set<ConstraintViolation<FilmUpdateDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertEquals("Id фильма должен быть положительным",
                violations.iterator().next().getMessage());
    }

    // Тест на отсутствие ошибок при валидных данных
    @Test
    void whenFilmUpdateDTOIsValid_thenNoValidationErrors() {
        FilmUpdateDTO dto = new FilmUpdateDTO();
        dto.setId(1);
        dto.setName("Home Alone");
        dto.setDescription("A".repeat(200));
        dto.setReleaseDate(LocalDate.of(2025, 1, 1));
        dto.setDuration(120);

        Set<ConstraintViolation<FilmUpdateDTO>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    // Тест на пустое имя (должно проходить, так как имя не обязательно)
    @Test
    void whenNameIsBlank_thenValidationPasses() {
        FilmUpdateDTO dto = new FilmUpdateDTO();
        dto.setId(1);
        dto.setName("");
        dto.setReleaseDate(LocalDate.now());
        dto.setDuration(120);

        Set<ConstraintViolation<FilmUpdateDTO>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    // Тест на null имя (должно проходить)
    @Test
    void whenNameIsNull_thenValidationPasses() {
        FilmUpdateDTO dto = new FilmUpdateDTO();
        dto.setId(1);
        dto.setReleaseDate(LocalDate.now());
        dto.setDuration(120);

        Set<ConstraintViolation<FilmUpdateDTO>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    // Тест на слишком длинное описание
    @Test
    void whenDescriptionExceeds200Chars_thenValidationFails() {
        FilmUpdateDTO dto = new FilmUpdateDTO();
        dto.setId(1);
        dto.setDescription("A".repeat(201));
        dto.setReleaseDate(LocalDate.now());
        dto.setDuration(120);

        Set<ConstraintViolation<FilmUpdateDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertEquals("Описание не должно превышать 200 символов",
                violations.iterator().next().getMessage());
    }

    // Тест на null дату релиза (должно проходить, так как поле не обязательно)
    @Test
    void whenReleaseDateIsNull_thenValidationPasses() {
        FilmUpdateDTO dto = new FilmUpdateDTO();
        dto.setId(1);
        dto.setDuration(120);

        Set<ConstraintViolation<FilmUpdateDTO>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    // Тест на дату релиза до 28.12.1895 года
    @Test
    void whenReleaseDateBefore28_12_1895_thenValidationFails() {
        FilmUpdateDTO dto = new FilmUpdateDTO();
        dto.setId(1);
        dto.setReleaseDate(LocalDate.of(1895, 1, 1));
        dto.setDuration(120);

        Set<ConstraintViolation<FilmUpdateDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertEquals("Дата релиза не может быть раньше 28 декабря 1895 года",
                violations.iterator().next().getMessage());
    }

    // Тест на будущую дату релиза
    @Test
    void whenReleaseDateInFuture_thenValidationFails() {
        FilmUpdateDTO dto = new FilmUpdateDTO();
        dto.setId(1);
        dto.setReleaseDate(LocalDate.now().plusDays(1));
        dto.setDuration(120);

        Set<ConstraintViolation<FilmUpdateDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertEquals("Дата релиза должна быть в прошлом или настоящем",
                violations.iterator().next().getMessage());
    }

    // Тест на null продолжительность (должно проходить)
    @Test
    void whenDurationIsNull_thenValidationPasses() {
        FilmUpdateDTO dto = new FilmUpdateDTO();
        dto.setId(1);
        dto.setReleaseDate(LocalDate.now());

        Set<ConstraintViolation<FilmUpdateDTO>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    // Тест на отрицательную продолжительность
    @Test
    void whenDurationIsNegative_thenValidationFails() {
        FilmUpdateDTO dto = new FilmUpdateDTO();
        dto.setId(1);
        dto.setReleaseDate(LocalDate.now());
        dto.setDuration(-1);

        Set<ConstraintViolation<FilmUpdateDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertEquals("Продолжительность фильма должна быть положительной",
                violations.iterator().next().getMessage());
    }

    // Тест на нулевую продолжительность
    @Test
    void whenDurationIsZero_thenValidationFails() {
        FilmUpdateDTO dto = new FilmUpdateDTO();
        dto.setId(1);
        dto.setReleaseDate(LocalDate.now());
        dto.setDuration(0);

        Set<ConstraintViolation<FilmUpdateDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertEquals("Продолжительность фильма должна быть положительной",
                violations.iterator().next().getMessage());
    }

    // Тест на несколько ошибок одновременно
    @Test
    void whenMultipleViolations_thenAllAreReported() {
        FilmUpdateDTO dto = new FilmUpdateDTO();
        // Не устанавливаем id
        dto.setDescription("A".repeat(201)); // Слишком длинное описание
        dto.setReleaseDate(LocalDate.now().plusDays(1)); // Будущая дата
        dto.setDuration(-1); // Отрицательная продолжительность

        Set<ConstraintViolation<FilmUpdateDTO>> violations = validator.validate(dto);
        assertEquals(4, violations.size()); // 4 нарушения


        List<String> errorMessages = violations.stream()
                                               .map(ConstraintViolation::getMessage)
                                               .toList();

        assertTrue(errorMessages.contains("Id фильма должен быть положительным"));
        assertTrue(errorMessages.contains("Описание не должно превышать 200 символов"));
        assertTrue(errorMessages.contains("Дата релиза должна быть в прошлом или настоящем"));
        assertTrue(errorMessages.contains("Продолжительность фильма должна быть положительной"));
    }
}