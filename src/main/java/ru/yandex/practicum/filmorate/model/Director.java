package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class Director {
    Integer id;
    @NotBlank(message = "Имя должно быть указано")
    String name;
}
