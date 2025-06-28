package ru.yandex.practicum.filmorate.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Comparator;

@Service
@RequiredArgsConstructor
public class FilmService {
    @Getter
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final Logger log = LoggerFactory.getLogger(FilmService.class);

    public Collection<Film> films() {
        return filmStorage.films();
    }

    public Film addFilm(Film film) {
        validateFilm(film);
        Film film1 = filmStorage.addFilm(film);
        log.info("Добавлен фильм: {}", film1);
        return film1;
    }

    public Film update(Film film) {
        validateFilm(film);
        Film film1 = filmStorage.update(film);
        log.info("Фильм обновлен: {}", film1);
        return film1;
    }

    public Film addLike(int id, int userId) {
        Film filmId = filmStorage.get(id);
        filmId.getLikeFrom().add(userStorage.get(userId));
        return filmId;
    }

    public Film deleteLike(int id, int userId) {
        Film filmId = filmStorage.get(id);
        filmId.getLikeFrom().remove(userStorage.get(userId));
        return filmId;
    }

    public Collection<Film> printTenMostPopular(int count) {
        return filmStorage.films().stream()
                .sorted(Comparator.comparingInt((Film film) -> film.getLikeFrom().size()).reversed())
                .limit(count)
                .toList();
    }

    private void validateFilm(Film film) {
        if (film.getName().isEmpty()) {
            throw new ValidationException("Название не может быть пустым: " + film.getName());
        }
        if (film.getDescription().length() > 200) {
            throw new ValidationException("Максимальная длина описания - 200 символов: " + film.getDescription());
        }
        if (film.getReleaseDate().isBefore(LocalDate.of(1895,12,28))) {
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года: " + film.getReleaseDate());
        }
        if (film.getDuration() <= 0) {
            throw new ValidationException("Продолжительность фильма должна быть положительным числом: " + film.getDuration());
        }
    }
}
