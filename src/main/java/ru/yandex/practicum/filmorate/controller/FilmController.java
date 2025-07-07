package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import jakarta.validation.Valid;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.*;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private final FilmService filmService;

    @Autowired
    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Film addFilm(@Valid @RequestBody Film film) {
        return filmService.addFilm(film);
    }

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public Film updateFilm(@Valid @RequestBody Film updatedFilm) {
        return filmService.updateFilm(updatedFilm);
    }

    @GetMapping("/{id}")
    public Film getFilm(@PathVariable int id) {
        return filmService.getFilm(id);
    }

    @GetMapping
    public Collection<Film> getAllFilms() {
        return filmService.getAllFilms();
    }

    // Пользователь ставит лайк фильму
    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable int id,
                        @PathVariable int userId) {
        filmService.addLike(id, userId);
    }

    // Пользователь удаляет лайк
    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable int id,
                           @PathVariable int userId) {
        filmService.removeLike(id, userId);
    }

    // Возвращается список из первых count фильмов по количеству лайков
    @GetMapping("/popular")
    public List<Film> getPopularFilms(
            @RequestParam(defaultValue = "10") int count) {
        return filmService.getPopularFilms(count);
    }
}
