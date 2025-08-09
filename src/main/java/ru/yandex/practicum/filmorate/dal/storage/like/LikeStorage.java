package ru.yandex.practicum.filmorate.dal.storage.like;

public interface LikeStorage {
    boolean isFilmLikedByUser(int filmId, int userId);

    void addLike(int filmId, int userId);

    void removeLike(int filmId, int userId);
}
