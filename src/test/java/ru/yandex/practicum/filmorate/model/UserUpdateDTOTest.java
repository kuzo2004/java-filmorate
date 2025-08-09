package ru.yandex.practicum.filmorate.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.dto.UserUpdateDTO;

import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class UserUpdateDTOTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void whenAllFieldsValid_thenValidationPasses() {
        UserUpdateDTO dto = new UserUpdateDTO();
        dto.setId(1);
        dto.setEmail("valid@example.com");
        dto.setLogin("validLogin");
        dto.setName("Valid Name");
        dto.setBirthday(LocalDate.now().minusYears(1));

        Set<ConstraintViolation<UserUpdateDTO>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty(), "Не должно быть нарушений при корректных данных");
    }

    @Test
    void whenIdNotPositive_thenValidationFails() {
        UserUpdateDTO dto = new UserUpdateDTO();
        dto.setId(0); // Некорректный ID
        dto.setEmail("valid@example.com");
        dto.setLogin("validLogin");
        dto.setName("Valid Name");
        dto.setBirthday(LocalDate.now().minusYears(1));

        Set<ConstraintViolation<UserUpdateDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertEquals("Id пользователя должен быть положительным",
                violations.iterator().next().getMessage());
    }

    @Test
    void whenIdIsNull_thenValidationFails() {
        UserUpdateDTO dto = new UserUpdateDTO();
        // Id null
        dto.setEmail("valid@example.com");
        dto.setLogin("validLogin");
        dto.setName("Valid Name");
        dto.setBirthday(LocalDate.now().minusYears(1));

        Set<ConstraintViolation<UserUpdateDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertEquals("Id пользователя должен быть положительным",
                violations.iterator().next().getMessage());
    }

    @Test
    void whenEmailInvalid_thenValidationFails() {
        UserUpdateDTO dto = new UserUpdateDTO();
        dto.setId(1);
        dto.setEmail("invalid-email"); // Некорректный email
        dto.setLogin("validLogin");
        dto.setName("Valid Name");
        dto.setBirthday(LocalDate.now().minusYears(1));

        Set<ConstraintViolation<UserUpdateDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertEquals("Email должен быть корректным",
                violations.iterator().next().getMessage());
    }

    @Test
    void whenEmailNull_thenValidationPasses() {
        UserUpdateDTO dto = new UserUpdateDTO();
        dto.setId(1);
        // Email может быть null (нет @NotBlank)
        dto.setLogin("validLogin");
        dto.setName("Valid Name");
        dto.setBirthday(LocalDate.now().minusYears(1));

        Set<ConstraintViolation<UserUpdateDTO>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty(), "Email может быть null, не должно быть нарушений");
    }

    @Test
    void whenLoginContainsSpaces_thenValidationFails() {
        UserUpdateDTO dto = new UserUpdateDTO();
        dto.setId(1);
        dto.setEmail("valid@example.com");
        dto.setLogin("invalid login"); // Логин с пробелами
        dto.setName("Valid Name");
        dto.setBirthday(LocalDate.now().minusYears(20));

        Set<ConstraintViolation<UserUpdateDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertEquals("Логин не должен содержать пробелы",
                violations.iterator().next().getMessage());
    }

    @Test
    void whenLoginNull_thenValidationPasses() {
        UserUpdateDTO dto = new UserUpdateDTO();
        dto.setId(1);
        dto.setEmail("valid@example.com");
        // Логин может быть null (нет @NotBlank)
        dto.setName("Valid Name");
        dto.setBirthday(LocalDate.now().minusYears(20));

        Set<ConstraintViolation<UserUpdateDTO>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty(), "Логин может быть null, не должно быть нарушений");
    }

    @Test
    void whenBirthdayInFuture_thenValidationFails() {
        UserUpdateDTO dto = new UserUpdateDTO();
        dto.setId(1);
        dto.setEmail("valid@example.com");
        dto.setLogin("validLogin");
        dto.setName("Valid Name");
        dto.setBirthday(LocalDate.now().plusDays(1)); // Дата в будущем

        Set<ConstraintViolation<UserUpdateDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertEquals("Дата рождения не может быть в будущем",
                violations.iterator().next().getMessage());
    }

    @Test
    void whenBirthdayNull_thenValidationPasses() {
        UserUpdateDTO dto = new UserUpdateDTO();
        dto.setId(1);
        dto.setEmail("valid@example.com");
        dto.setLogin("validLogin");
        dto.setName("Valid Name");
        // Дата может быть null

        Set<ConstraintViolation<UserUpdateDTO>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty(), "Дата рождения может быть null, не должно быть нарушений");
    }

    @Test
    void whenNameNull_thenValidationPasses() {
        UserUpdateDTO dto = new UserUpdateDTO();
        dto.setId(1);
        dto.setEmail("valid@example.com");
        dto.setLogin("validLogin");
        // Имя может быть null
        dto.setBirthday(LocalDate.now().minusYears(1));

        Set<ConstraintViolation<UserUpdateDTO>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty(), "Имя может быть null, не должно быть нарушений");
    }

    @Test
    void whenMultipleViolations_thenValidationFails() {
        UserUpdateDTO dto = new UserUpdateDTO();
        // Id null
        dto.setEmail("invalid-email"); // Некорректный email
        dto.setLogin("invalid login"); // Логин с пробелами
        dto.setName("Valid Name");
        dto.setBirthday(LocalDate.now().plusDays(1)); // Дата в будущем

        Set<String> violations = validator.validate(dto).stream()
                                          .map(v -> v.getMessage().trim()) // Убираем пробелы для сравнения
                                          .collect(Collectors.toSet());

        assertEquals(4, violations.size());
        assertTrue(violations.contains("Id пользователя должен быть положительным"));
        assertTrue(violations.contains("Email должен быть корректным"));
        assertTrue(violations.contains("Логин не должен содержать пробелы"));
        assertTrue(violations.contains("Дата рождения не может быть в будущем"));
    }
}