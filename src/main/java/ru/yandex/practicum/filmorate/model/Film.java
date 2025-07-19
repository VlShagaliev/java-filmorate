package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

/**
 * Film.
 */

@Data
public class Film {
    private Integer id;
    @NotBlank(message = "Название фильма должно быть указано")
    private String name;
    @Size(max = 200, message = "Максимальная длина описания - 200 символов")
    @NotBlank
    private String description;
    private LocalDate releaseDate;
    @Positive
    @Min(value = 1, message = "Продолжительность фильма должна быть положительной")
    private int duration;
    private Integer countLikes;
    private Double rate;
    private Genre[] genres;
    @JsonProperty(value = "mpa")
    private Rating rating;
    private Director[] directors;
}
