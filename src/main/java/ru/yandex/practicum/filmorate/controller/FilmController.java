package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private final Logger log = LoggerFactory.getLogger(Film.class);

    @GetMapping
    public Collection<Film> films(){
        return films.values();
    }

    @PostMapping
    public Film addFilm(@Valid @RequestBody Film film){
        validateFilm(film);
        film.setId(films.size() + 1);
        films.put(film.getId(), film);
        log.info("Добавлен фильм: {}", film);
        return film;
    }

    private void validateFilm(Film film){
        if (film.getName().isEmpty()){
            log.warn("Название фильма не может быть пустым: {}", film.getName());
            throw new ValidationException("Название не может быть пустым");
        }
        if (film.getDescription().length() > 200){
            log.warn("Максимальная длина описания - 200 символов: {}", film.getDescription());
            throw new ValidationException("Максимальная длина описания - 200 символов");
        }
        if (film.getReleaseDate().isBefore(LocalDate.of(1895,12,28))){
            log.warn("Дата релиза не может быть раньше 28 декабря 1895 года: {}", film.getReleaseDate());
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }
        if (film.getDuration() <= 0){
            log.warn("Продолжительность фильма должна быть положительным числом: {}", film.getDuration());
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
        }
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film film){
        validateFilm(film);
        if (film.getId() <= 0){
            log.warn("Отсутствует id фильма: {}", film.getId());
            throw new ValidationException("Отсутствует id фильма");
        }
        if (films.containsKey(film.getId())) {
            log.info("Данные фильма успешно обновлены: {}", film);
            films.put(film.getId(), film);
        }
        else {
            log.warn("Фильм с id = {} отсутствует в списке", film.getId());
            throw new ValidationException("Фильм отсутствует в списке");
        }
        return film;
    }
}
