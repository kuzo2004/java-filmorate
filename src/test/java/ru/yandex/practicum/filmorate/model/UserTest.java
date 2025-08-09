package ru.yandex.practicum.filmorate.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {
    private static Validator validator;

    @BeforeAll
    static void setup() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void whenAllFieldsCorrect_thenValidationPasses() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("validLogin");
        user.setName("Valid Name");
        user.setBirthday(LocalDate.now().minusYears(1));

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertTrue(violations.isEmpty(), "Не должно быть нарушений при корректных данных");
    }

    @Test
    void whenEmailNull_thenValidationFails() {
        User user = new User();
        // null email
        user.setLogin("validLogin");
        user.setName("Valid Name");
        user.setBirthday(LocalDate.now().minusYears(1));

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());
        assertEquals("Email не может быть пустым",
                violations.iterator().next().getMessage());
    }

    @Test
    void whenEmailBlank_thenValidationFails() {
        User user = new User();
        user.setEmail(""); // Пустой email
        user.setLogin("validLogin");
        user.setName("Valid Name");
        user.setBirthday(LocalDate.now().minusYears(1));

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());
        assertEquals("Email не может быть пустым",
                violations.iterator().next().getMessage());
    }

    @Test
    void whenEmailInvalid_thenValidationFails() {
        User user = new User();
        user.setEmail("invalid-email"); // Некорректный email
        user.setLogin("validLogin");
        user.setName("Valid Name");
        user.setBirthday(LocalDate.now().minusYears(1));

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());
        assertEquals("Email должен быть корректным",
                violations.iterator().next().getMessage());
    }

    @Test
    void whenLoginNull_thenValidationFails() {
        User user = new User();
        user.setEmail("test@example.com");
        // null логин
        user.setName("Valid Name");
        user.setBirthday(LocalDate.now().minusYears(1));

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());
        assertEquals("Логин не может быть пустым",
                violations.iterator().next().getMessage());
    }

    @Test
    void whenLoginBlank_thenValidationFails() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin(""); // Пустой логин
        user.setName("Valid Name");
        user.setBirthday(LocalDate.now().minusYears(1));

        Set<String> violations = validator.validate(user).stream()
                                          .map(ConstraintViolation::getMessage)
                                          .collect(Collectors.toSet());
        assertFalse(violations.isEmpty());
        assertTrue(violations.contains("Логин не может быть пустым"));
        assertTrue(violations.contains("Логин не должен содержать пробелы"));
    }

    @Test
    void whenLoginContainsSpaces_thenValidationFails() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("invalid login"); // Логин с пробелами
        user.setName("Valid Name");
        user.setBirthday(LocalDate.now().minusYears(1));

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());
        assertEquals("Логин не должен содержать пробелы",
                violations.iterator().next().getMessage());
    }

    @Test
    void whenBirthdayInFuture_thenValidationFails() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("validLogin");
        user.setName("Valid Name");
        user.setBirthday(LocalDate.now().plusDays(1)); // Дата в будущем

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());
        assertEquals("Дата рождения не может быть в будущем",
                violations.iterator().next().getMessage());
    }

    @Test
    void whenMultipleViolations_thenValidationFails() {
        User user = new User();
        user.setEmail("invalid-email"); // Некорректный email
        user.setLogin(""); // Пустой логин
        user.setName("Valid Name");
        user.setBirthday(LocalDate.now().plusDays(1)); // Дата в будущем

        Set<String> violations = validator.validate(user).stream()
                                          .map(ConstraintViolation::getMessage)
                                          .collect(Collectors.toSet());

        assertEquals(4, violations.size());
        assertTrue(violations.contains("Email должен быть корректным"));
        assertTrue(violations.contains("Логин не может быть пустым"));
        assertTrue(violations.contains("Логин не должен содержать пробелы"));
        assertTrue(violations.contains("Дата рождения не может быть в будущем"));
    }

    @Test
    void whenNameNull_thenValidationPasses() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("validLogin");
        // Имя может быть null
        user.setBirthday(LocalDate.now().minusYears(1));

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertTrue(violations.isEmpty(), "Имя может быть null, не должно быть нарушений");
    }

    @Test
    void whenNameBlank_thenValidationPasses() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("validLogin");
        user.setName(""); // Пустое имя допустимо
        user.setBirthday(LocalDate.now().minusYears(1));

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertTrue(violations.isEmpty(), "Имя может быть пустое, не должно быть нарушений");
    }
}