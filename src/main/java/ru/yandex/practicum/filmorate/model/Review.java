package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class Review {
    private final Integer filmId;
    private final Integer userId;
    @JsonProperty(value = "reviewId")
    private Integer id;
    @NotBlank(message = "Содержание отзыва не может быть пустым")
    private String content;
    @NotNull(message = "Отзыв должен содержать тип")
    private Boolean isPositive;
    private int useful;
}