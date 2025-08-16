package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.storage.like.LikeStorage;
import ru.yandex.practicum.filmorate.exception.ValidationExceptionDuplicate;

@Slf4j
@Service
public class LikeService {
    private final LikeStorage likeStorage;

    public LikeService(LikeStorage likeStorage) {
        this.likeStorage = likeStorage;
    }

    public boolean isFilmLikedByUser(int filmId, int userId) {

        return likeStorage.isFilmLikedByUser(filmId, userId);
    }

    public void addLike(int filmId, int userId) {
        // Проверить существование лайка
        if (isFilmLikedByUser(filmId, userId)) {
            log.debug("Пользователь {} уже ставил лайк фильму {}", userId, filmId);
            throw new ValidationExceptionDuplicate(
                    String.format("Пользователь %d уже ставил лайк фильму %d", userId, filmId)
            );
        }

        likeStorage.addLike(filmId, userId);
    }

    public void removeLike(int filmId, int userId) {
        // Проверить существование лайка
        if (!isFilmLikedByUser(filmId, userId)) {
            log.debug("Лайк пользователя {} для фильма {} не найден", userId, filmId);
            return;
        }

        likeStorage.removeLike(filmId, userId);
    }
}
