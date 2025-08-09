package ru.yandex.practicum.filmorate.dto;

import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import ru.yandex.practicum.filmorate.annotation.MinReleaseDate;

import java.time.LocalDate;

@Data
public class FilmUpdateDTO {
    @Positive(message = "Id фильма должен быть положительным")
    private int id;

    private String name;

    @Size(max = 200, message = "Описание не должно превышать 200 символов")
    private String description;


    @PastOrPresent(message = "Дата релиза должна быть в прошлом или настоящем")
    @MinReleaseDate
    private LocalDate releaseDate;

    @Positive(message = "Продолжительность фильма должна быть положительной")
    private Integer duration;
}
