package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import jakarta.validation.Valid;
import ru.yandex.practicum.filmorate.model.FilmUpdateDTO;

import java.util.*;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private final Map<Integer, Film> films = new HashMap<>();

    @PostMapping
    public Film addFilm(@Valid @RequestBody Film film) {
        film.setId(getNextId());
        films.put(film.getId(), film);
        log.info("Добавлен фильм: {}", film);
        return film;
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody FilmUpdateDTO updatedFilm) {
        // Проверка существования фильма
        if (!films.containsKey(updatedFilm.getId())) {
            log.error("Фильм с ID {} не найден", updatedFilm.getId());
            throw new ValidationException("Фильм с ID " + updatedFilm.getId() + " не найден");
        }

        Film existingFilm = films.get(updatedFilm.getId());

        // Обновляем только не-null и не пустые поля
        if (updatedFilm.getName() != null && !updatedFilm.getName().isBlank()) {
            existingFilm.setName(updatedFilm.getName());
        }

        if (updatedFilm.getDescription() != null && !updatedFilm.getDescription().isBlank()) {
            existingFilm.setDescription(updatedFilm.getDescription());
        }

        if (updatedFilm.getReleaseDate() != null) {
            existingFilm.setReleaseDate(updatedFilm.getReleaseDate());
        }

        if (updatedFilm.getDuration() != null) {
            existingFilm.setDuration(updatedFilm.getDuration());
        }

        log.info("Обновлён фильм: {}", existingFilm);
        return existingFilm;
    }

    @GetMapping
    public Collection<Film> getAllFilms() {
        return films.values();
    }

    private int getNextId() {
        int currentMaxId = films.keySet()
                                .stream()
                                .mapToInt(id -> id)
                                .max()
                                .orElse(0);
        return ++currentMaxId;
    }
}
