package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDate;

/**
 * Film.
 */
@Data
public class Film {
    private Integer id;
    private String name;
    @NotBlank
    private String description;
    private LocalDate releaseDate;
    @Positive
    private int duration;
}
