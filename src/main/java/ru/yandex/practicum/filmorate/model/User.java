package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class User {
    private int id;
    @NotBlank
    @NotNull
    @Email
    private String email;
    @NotNull
    @NotBlank
    private String login;
    @NotNull
    private String name;
    @NotNull
    private LocalDate birthday;
}
