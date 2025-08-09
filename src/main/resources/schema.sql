CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    login VARCHAR(50) NOT NULL,
    name VARCHAR(100),
    birthday DATE
);
CREATE TABLE IF NOT EXISTS mpa (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL
);
CREATE TABLE IF NOT EXISTS genres (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL
);
CREATE TABLE IF NOT EXISTS films (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(200),
    release_date DATE NOT NULL,
    duration INT NOT NULL,
    mpa_id INT REFERENCES mpa(id)
);
-- Таблица лайков (многие ко многим между films и users)
CREATE TABLE IF NOT EXISTS likes (
    film_id INT REFERENCES films(id),
    user_id INT REFERENCES users(id)
);
-- Таблица друзей (двусторонняя связь пользователей)
CREATE TABLE IF NOT EXISTS friends (
    user_id INT REFERENCES users(id),
    friend_id INT REFERENCES users(id)
);
-- Таблица жанров фильмов (многие ко многим)
CREATE TABLE IF NOT EXISTS film_genres (
    film_id INT REFERENCES films(id),
    genre_id INT REFERENCES genres(id)
);



