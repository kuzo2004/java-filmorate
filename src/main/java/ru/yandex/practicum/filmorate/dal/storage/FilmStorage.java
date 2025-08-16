package ru.yandex.practicum.filmorate.dal.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface FilmStorage {
    Film addFilm(Film film);

    Film updateFilm(Film film);

    Optional<Film> findFilmById(int id);

    boolean existsById(int filmId);

    Collection<Film> getAllFilms();

    List<Film> getPopularFilms(int count);
}

