package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/films")
public class FilmController {
    private final Map<Integer, Film> films = new HashMap<>();

    @GetMapping
    public Collection<Film> films(){
        return films.values();
    }

    @PostMapping
    public Film addFilm(@Valid @RequestBody Film film){
        vaildFilm(film);
        film.setId(films.size()+1);
        films.put(film.getId(), film);
        return film;
    }

    private void vaildFilm(Film film){
        if (film.getName().isEmpty()){
            throw new ValidationException("Название не может быть пустым");
        }
        if (film.getDescription().length() > 200){
            throw new ValidationException("Максимальная длина описания - 200 символов");
        }
        if (film.getReleaseDate().isBefore(LocalDate.of(1895,12,28))){
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }
        if (film.getDuration() <= 0){
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
        }
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film film){
        vaildFilm(film);
        if (film.getId() <= 0){
            throw new ValidationException("Отсутствует id фильма");
        }
        if (films.containsKey(film.getId()))
            films.put(film.getId(), film);
        else {
            throw new ValidationException("Фильм отсутствует в списке");
        }
        return film;
    }


}
