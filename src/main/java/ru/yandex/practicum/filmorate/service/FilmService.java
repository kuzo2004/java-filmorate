package ru.yandex.practicum.filmorate.service;

import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.storage.FilmStorage;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserService userService;
    private final MpaService mpaService;
    private final GenreService genreService;
    private final LikeService likeService;

    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage,
                       UserService userService,
                       MpaService mpaService,
                       GenreService genreService,
                       LikeService likeService
    ) {
        this.filmStorage = filmStorage;
        this.userService = userService;
        this.mpaService = mpaService;
        this.genreService = genreService;
        this.likeService = likeService;
    }

    public Film addFilm(Film film) {
        // 1. Проверяем существование MPA
        Mpa mpa = mpaService.findMpaById(film.getMpa().getId()); // выбросит NotFoundException
        film.setMpa(mpa); // от клиента приходит только id, а мы можем вернуть клиенту с именем mpa

        // 2. Обрабатываем жанры (если они есть)
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            // Убираем дубликаты жанров и сверяемся со справочником по наличию и проставляем имена жанров
            List<Genre> uniqueGenres = film.getGenres().stream()
                                           .distinct()
                                           .map(genre -> genreService.findGenreById(genre.getId()))
                                           .toList();

            // Устанавливаем жанры обратно в объект film, но уже проверенные и с именами.
            film.setGenres(uniqueGenres);
        }

        // 3. Сохраняем фильм вместе с жанрами
        Film addedFilm = filmStorage.addFilm(film);

        log.debug("Фильм добавлен: {}", addedFilm);
        return addedFilm;
    }

    public Film updateFilm(Film film) {
        // 1. Проверяем существование фильма
        validateFilmExists(film.getId());

        // 2. Проверяем существование MPA
        Mpa mpa = mpaService.findMpaById(film.getMpa().getId()); // выбросит NotFoundException
        film.setMpa(mpa);

        // 3. Проверяем жанры
        if (film.getGenres() != null) {
            List<Genre> uniqueGenres = film.getGenres().stream()
                                           .distinct()
                                           .map(genre -> genreService.findGenreById(genre.getId()))
                                           .collect(Collectors.toList());

            // Устанавливаем жанры обратно в объект film, но уже проверенные и с именами.
            film.setGenres(uniqueGenres);
        } else {
            // Если жанры не передали, обнулим список, так как это Post(полный update)
            film.setGenres(new ArrayList<>());
        }

        // 4. Обновление таблицы films
        Film updatedFilm = filmStorage.updateFilm(film);

        log.debug("Фильм полностью обновлен: {}", updatedFilm);
        return updatedFilm;
    }

    public Film findFilmById(int id) {
        return filmStorage.findFilmById(id)
                          .orElseThrow(() -> new NotFoundException("Фильм с ID " + id + " не найден"));
    }

    public Collection<Film> getAllFilms() {

        return filmStorage.getAllFilms();
    }

    public void addLike(int filmId, int userId) {
        // Проверить существование фильма
        validateFilmExists(filmId);

        // Проверить существование пользователя
        userService.validateUserExists(userId);

        likeService.addLike(filmId, userId);
        log.debug("Пользователь {} поставил лайк фильму {}", userId, filmId);

    }

    public void removeLike(int filmId, int userId) {
        // Проверяем существование фильма
        validateFilmExists(filmId);

        // Проверить существование пользователя
        userService.validateUserExists(userId);

        likeService.removeLike(filmId, userId);
        log.debug("Пользователь {} удалил лайк у фильма {}", userId, filmId);
    }

    public List<Film> getPopularFilms(int count) {
        if (count <= 0) {
            throw new ValidationException("Число выводимых фильмов должно быть положительным.");
        }
        return filmStorage.getPopularFilms(count);
    }

    public void validateFilmExists(int id) {
        if (!filmStorage.existsById(id)) {
            throw new NotFoundException("Фильм с ID " + id + " не найден");
        }
    }
}