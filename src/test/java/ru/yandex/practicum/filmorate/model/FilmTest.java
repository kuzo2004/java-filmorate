package ru.yandex.practicum.filmorate.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class FilmTest {
    private static Validator validator;

    @BeforeAll
    static void setup() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    // Тест на пустое название
    @Test
    void whenNameIsBlank_thenValidationFails() {
        Film film = new Film();
        film.setName("");
        film.setReleaseDate(LocalDate.now());
        film.setDuration(120);
        film.setMpa(new Mpa(1, null));

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty());
        assertEquals("Название фильма не может быть пустым",
                violations.iterator().next().getMessage());
    }

    // Тест на null название
    @Test
    void whenNameIsNull_thenValidationFails() {
        Film film = new Film();

        film.setReleaseDate(LocalDate.now());
        film.setMpa(new Mpa(1, null));
        film.setDuration(120);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty());
        assertEquals("Название фильма не может быть пустым",
                violations.iterator().next().getMessage());
    }

    // Тест на слишком длинное описание
    @Test
    void whenDescriptionExceeds200Chars_thenValidationFails() {
        Film film = new Film();
        film.setName("Home Alone");
        film.setDescription("A".repeat(201));
        film.setMpa(new Mpa(1, null));
        film.setReleaseDate(LocalDate.now());
        film.setDuration(120);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty());
        assertEquals("Описание не должно превышать 200 символов",
                violations.iterator().next().getMessage());
    }

    // Тест на null дату релиза
    @Test
    void whenReleaseDateIsNull_thenValidationFails() {
        Film film = new Film();
        film.setName("Home Alone");
        film.setMpa(new Mpa(1, null));
        //film.setReleaseDate(null);
        film.setDuration(120);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty());
        assertEquals("Дата релиза обязательна",
                violations.iterator().next().getMessage());
    }

    // Тест на дату релиза до 18/12/1895 года
    @Test
    void whenReleaseDateBefore12_18_1895_thenValidationFails() {
        Film film = new Film();
        film.setName("Home Alone");
        film.setMpa(new Mpa(1, null));

        film.setReleaseDate(LocalDate.of(1895, 1, 1));
        film.setDuration(120);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty());
        assertEquals("Дата релиза не может быть раньше 28 декабря 1895 года",
                violations.iterator().next().getMessage());
    }

    // Тест на будущую дату релиза
    @Test
    void whenReleaseDateInFuture_thenValidationFails() {
        Film film = new Film();
        film.setName("Home Alone");
        film.setReleaseDate(LocalDate.now().plusDays(1));
        film.setDuration(120);
        film.setMpa(new Mpa(1, null));

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty());
        assertEquals("Дата релиза должна быть в прошлом или настоящем",
                violations.iterator().next().getMessage());
    }

    // Тест на null продолжительность
    @Test
    void whenDurationIsNull_thenValidationFails() {
        Film film = new Film();
        film.setName("Home Alone");
        film.setReleaseDate(LocalDate.now());
        film.setMpa(new Mpa(1, null));
        //film.setDuration(null);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty());
        assertEquals("Продолжительность фильма не должна быть пустой",
                violations.iterator().next().getMessage());
    }

    // Тест на отрицательную продолжительность
    @Test
    void whenDurationIsNegative_thenValidationFails() {
        Film film = new Film();
        film.setName("Home Alone");
        film.setReleaseDate(LocalDate.now());
        film.setMpa(new Mpa(1, null));
        film.setDuration(-1);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty());
        assertEquals("Продолжительность фильма должна быть положительной",
                violations.iterator().next().getMessage());
    }

    // Тест на нулевую продолжительность
    @Test
    void whenDurationIsZero_thenValidationFails() {
        Film film = new Film();
        film.setName("Home Alone");
        film.setReleaseDate(LocalDate.now());
        film.setMpa(new Mpa(1, null));
        film.setDuration(0);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty());
        assertEquals("Продолжительность фильма должна быть положительной",
                violations.iterator().next().getMessage());
    }

    // Тест на корректный фильм
    @Test
    void whenFilmIsValid_thenNoValidationErrors() {
        Film film = new Film();
        film.setName("Home Alone");
        film.setDescription("A".repeat(200));
        film.setReleaseDate(LocalDate.of(2025, 1, 1));
        film.setMpa(new Mpa(1, null));
        film.setDuration(120);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertTrue(violations.isEmpty());
    }

    // Тест на более чем одну ошибку одновременно
    void whenDurationIsNullAndNameIsNull_thenValidationFails() {
        Film film = new Film();

        film.setDescription("A".repeat(200));
        film.setReleaseDate(LocalDate.of(2025, 1, 1));


        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertTrue(violations.isEmpty());
        assertEquals(2, violations.size());
        // Проверяем конкретные сообщения об ошибках
        List<String> errorMessages = violations.stream()
                                               .map(ConstraintViolation::getMessage)
                                               .toList();

        assertTrue(errorMessages.contains("Название фильма не может быть пустым"));
        assertTrue(errorMessages.contains("Продолжительность фильма не должна быть пустой"));
    }
}

