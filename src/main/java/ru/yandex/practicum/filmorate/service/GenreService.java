package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.storage.genre.GenreStorage;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;

@Service
public class GenreService {
    private final GenreStorage genreStorage;

    public GenreService(GenreStorage genreStorage) {

        this.genreStorage = genreStorage;
    }

    public List<Genre> getAllGenres() {

        return genreStorage.getAllGenres();
    }

    public Genre findGenreById(int id) {
        return genreStorage.getGenreById(id)
                           .orElseThrow(() -> new NotFoundException("Жанр с ID " + id + " не найден"));
    }
}
