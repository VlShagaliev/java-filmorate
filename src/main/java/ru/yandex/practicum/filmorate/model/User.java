package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * User.
 */

@Data
public class User {
    private Integer id;
    @Email
    private String email;
    private String login;
    private String name;
    @NotNull
    private LocalDate birthday;
    private Set<Integer> friends = new HashSet<>();
}
