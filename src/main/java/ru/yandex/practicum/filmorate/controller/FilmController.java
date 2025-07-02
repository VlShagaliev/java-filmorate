package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Collection;

@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
public class FilmController {

    private final FilmService filmService;

    @GetMapping
    public Collection<Film> films() {
        return filmService.films();
    }

    @PostMapping
    public Film addFilm(@Valid @RequestBody Film film) {
        return filmService.addFilm(film);
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film film) {
        return filmService.update(film);
    }

    @PutMapping("/{id}/like/{userId}")
    public Film setLike(@PathVariable("id") int id,
                        @PathVariable("userId") int userId) {
        return filmService.addLike(id, userId);
    }

    @GetMapping("/popular")
    public Collection<Film> popularFilms(@RequestParam("count") int count) {
        return filmService.popularFilms(count);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public Film deleteLike(@PathVariable("id") int id,
                           @PathVariable("userId") int userId) {
        return filmService.deleteLike(id, userId);
    }

    @GetMapping("/{filmId}")
    public Film getFilmById(@PathVariable("filmId") int filmId) {
        return filmService.getFilmById(filmId);
    }
}
