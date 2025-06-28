package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

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
    private final Set<User> likeFrom = new HashSet<>();
    private Set<Genre> genres = new HashSet<>();
    private Set<Rating> ratings = new HashSet<>();
}
