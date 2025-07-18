package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationExceptionDuplicate;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.Comparator;
import java.util.Collection;
import java.util.List;

@Slf4j
@Service
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserService userService;

    public FilmService(FilmStorage filmStorage, UserService userService) {
        this.filmStorage = filmStorage;
        this.userService = userService;
    }

    public Film addFilm(Film film) {
        Film addedFilm = filmStorage.addFilm(film);
        log.debug("Фильм добавлен: {}", addedFilm);
        return addedFilm;
    }

    public Film updateFilm(Film film) {
        // Проверяем существование фильма
        Film existingFilm = getFilm(film.getId());
        // Сохраняем существующие лайки при обновлении
        film.setLikes(existingFilm.getLikes());

        Film updatedFilm = filmStorage.updateFilm(film);
        log.debug("Фильм полностью обновлен: {}", updatedFilm);
        return updatedFilm;
    }

    public Film getFilm(int id) {
        Film film = filmStorage.getFilm(id);
        if (film == null) {
            throw new NotFoundException("Фильм с ID " + id + " не найден");
        }
        return film;
    }

    public Collection<Film> getAllFilms() {
        return filmStorage.getAllFilms();
    }

    public void addLike(int filmId, int userId) {
        Film film = getFilm(filmId);

        // Проверить существование пользователя
        userService.getUser(userId);

        // Проверить существование лайка
        if (film.getLikes().contains(userId)) {
            log.debug(String.format("Пользователь %d уже ставил лайк фильму %d", userId, filmId));
            throw new ValidationExceptionDuplicate(
                    String.format("Пользователь %d уже ставил лайк фильму %d", userId, filmId)
            );
        }
        film.getLikes().add(userId);

        filmStorage.updateFilm(film);
        log.debug("Пользователь {} поставил лайк фильму {}", userId, filmId);
    }

    public void removeLike(int filmId, int userId) {
        Film film = getFilm(filmId);

        // Проверить существование пользователя
        userService.getUser(userId);

        if (!film.getLikes().remove(userId)) {
            log.debug("Лайк пользователя {} для фильма {} не найден", userId, filmId);
            return;
        }
        filmStorage.updateFilm(film);
        log.debug("Пользователь {} удалил лайк у фильма {}", userId, filmId);
    }

    public List<Film> getPopularFilms(int count) {
        return filmStorage.getAllFilms().stream()
                          .sorted(Comparator.comparingInt(f -> -f.getLikes().size()))
                          .limit(count)
                          .toList();
    }
}