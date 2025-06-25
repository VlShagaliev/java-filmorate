package ru.yandex.practicum.filmorate.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.Comparator;

@Service
@RequiredArgsConstructor
public class FilmService {
    @Getter
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public Film addLike(int id, int userId) {
        Film filmId = filmStorage.get(id);
        filmId.getLikeCount().add(userStorage.get(userId));
        return filmId;
    }

    public Film deleteLike(int id, int userId) {
        Film filmId = filmStorage.get(id);
        filmId.getLikeCount().remove(userStorage.get(userId));
        return filmId;
    }

    public Collection<Film> printTenMostPopular(int count) {
        return filmStorage.films().stream()
                .sorted(Comparator.comparingInt((Film film) -> film.getLikeCount().size()).reversed())
                .limit(count)
                .toList();
    }
}
