package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.ValidationException;
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

    @DeleteMapping("/{filmId}")
    public void deleteFilm(@PathVariable("filmId") int filmId) {
        filmService.deleteFilm(filmId);
    }

    @PutMapping(value = {"/{id}/like/{userId}", "/{id}/like/{userId}/{mark}"})
    public Film setLike(
            @PathVariable("id") int id,
            @PathVariable("userId") int userId,
            @PathVariable(name = "mark", required = false) Double mark) {

        if (mark != null && (mark < 1 || mark > 10)) {
            throw new ValidationException("Оценка должна быть от 1 до 10");
        }
        return filmService.addLike(id, userId, mark != null ? mark : 6.0);
    }

    @GetMapping("/popular")
    public Collection<Film> popularFilms(
            @RequestParam(defaultValue = "10") int count,
            @RequestParam(required = false) Integer genreId,
            @RequestParam(required = false) Integer year) {

        return filmService.popularFilms(count, genreId, year);
    }

    @GetMapping("/common")
    public Collection<Film> getCommonFilms(@RequestParam int userId, @RequestParam int friendId) {
        return filmService.getCommonFilms(userId, friendId);
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

    @GetMapping("/director/{id}")
    public Collection<Film> getFilmSorted(@PathVariable("id") int directorId, @RequestParam("sortBy") String typeSort) {
        return filmService.getFilmSorted(directorId, typeSort);
    }

    @GetMapping("/search")
    public Collection<Film> getFilmByQuery(@RequestParam("query") String query, @RequestParam("by") String by) {
        return filmService.getFilmByQuery(query, by);
    }
}