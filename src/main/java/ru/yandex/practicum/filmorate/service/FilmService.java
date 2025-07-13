package ru.yandex.practicum.filmorate.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.*;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.UserEventOperation;
import ru.yandex.practicum.filmorate.model.UserEventType;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FilmService {
    @Getter
    private final FilmDbStorage filmDbStorage;
    private final LikesDbStorage likesDbStorage;
    private final GenresDbStorage genresDbStorage;
    private final UserEventDbStorage userEventDbStorage;
    private final Logger log = LoggerFactory.getLogger(FilmService.class);

    public Collection<Film> films() {
        return filmDbStorage.films();
    }

    public Film addFilm(Film film) {
        validateFilm(film);
        Film film1 = filmDbStorage.addFilm(film);
        log.info("Добавлен фильм: {}", film1);
        return film1;
    }

    public Film update(Film film) {
        validateFilm(film);
        filmDbStorage.checkDbHasId(BaseDbStorage.CHECK_FILM_IN_DB, film.getId(), FilmDbStorage.errorMessage);
        Film film1 = filmDbStorage.update(film);
        log.info("Фильм обновлен: {}", film1);
        return film1;
    }

    public void deleteFilm(int id) {
        filmDbStorage.checkDbHasId(BaseDbStorage.CHECK_FILM_IN_DB, id, FilmDbStorage.errorMessage);

        filmDbStorage.deleteFilm(id);
        log.info("Фильм удален: {}", id);
    }

    public Film addLike(int id, int userId) {
        Film film = filmDbStorage.get(id);
        film.setCountLikes(likesDbStorage.addLike(id, userId));
        userEventDbStorage.add(userId, id, UserEventType.LIKE, UserEventOperation.ADD);
        return film;
    }

    public Collection<Film> popularFilms(int count, Integer genreId, Integer year) {

        if (genreId != null) {
            genresDbStorage.checkDbHasId(genreId);
        }

        if (year != null && year < 1895) {
            throw new ValidationException("Год должен быть не ранее 1895");
        }

        return filmDbStorage.mostPopular(count, genreId, year);
    }

    public Collection<Film> getCommonFilms(int userId, int friendId) {
        return filmDbStorage.getCommonFilms(userId, friendId);
    }

    public Film deleteLike(int id, int userId) {
        Film resultFilm = filmDbStorage.deleteLike(id, userId);
        userEventDbStorage.add(userId, id, UserEventType.LIKE, UserEventOperation.REMOVE);
        return resultFilm;
    }

    private void validateFilm(Film film) {
        if (film.getName().isEmpty()) {
            throw new ValidationException("Название не может быть пустым: " + film.getName());
        }
        if (film.getDescription().length() > 200) {
            throw new ValidationException("Максимальная длина описания - 200 символов: " + film.getDescription());
        }
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года: " + film.getReleaseDate());
        }
        if (film.getDuration() <= 0) {
            throw new ValidationException("Продолжительность фильма должна быть положительным числом: " + film.getDuration());
        }
        if (!filmDbStorage.checkRatingHasId(film.getRating().getId())) {
            throw new NotFoundException(String.format("Рейтинг с данным id = %d отсутствует в списке", film.getRating().getId()));
        }
        if ((film.getGenres() != null && film.getGenres().length != 0)) {
            Genre[] newGenres = Arrays.stream(film.getGenres()).distinct().toArray(Genre[]::new);
            if (film.getGenres().length != newGenres.length) {
                film.setGenres(newGenres);
            }
        }
    }

    public Film getFilmById(int id) {
        return filmDbStorage.get(id);
    }

    public Collection<Film> getFilmSorted(int directorId, String typeSort) {
        switch (typeSort) {
            case "year": return filmDbStorage.getFilmsSortedByYear(directorId);
            case "likes": return filmDbStorage.getFilmsSortedByLikes(directorId);
            default: throw new RuntimeException("Неизвестная команда сортировки!");
        }
    }

    public Collection<Film> getFilmByQuery(String query, String by) {
        List<String> searchBy = Arrays.stream(by.split(",")).toList();
        searchBy.stream()
                .filter(option -> !option.equals("title") || !option.equals("director"))
                .findFirst()
                .orElseThrow(() -> new ValidationException("Переданные параметры неверные!"));
        return filmDbStorage.getFilmByQuery(query, by);
    }
}