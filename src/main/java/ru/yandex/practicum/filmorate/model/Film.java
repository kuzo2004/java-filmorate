package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import ru.yandex.practicum.filmorate.annotation.MinReleaseDate;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Builder
@Data
@AllArgsConstructor
public class Film {
    private int id;

    @NotBlank(message = "Название фильма не может быть пустым")
    private String name;

    @Size(max = 200, message = "Описание не должно превышать 200 символов")
    private String description;

    @NotNull(message = "Дата релиза обязательна")
    @PastOrPresent(message = "Дата релиза должна быть в прошлом или настоящем")
    @MinReleaseDate
    private LocalDate releaseDate;

    @NotNull(message = "Продолжительность фильма не должна быть пустой")
    @Positive(message = "Продолжительность фильма должна быть положительной")
    private Integer duration;

    @NotNull(message = "Рейтинг фильма не должна быть пустой")
    private Mpa mpa;

    private List<Genre> genres;  //пройти тесты Postman получилось только через List

    public Film() {
        this.genres = new ArrayList<>();
    }
}
