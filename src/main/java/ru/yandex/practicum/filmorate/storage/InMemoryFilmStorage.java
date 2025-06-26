package ru.yandex.practicum.filmorate.storage;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Component
public class InMemoryFilmStorage implements FilmStorage {
    @Getter
    private final Map<Integer, Film> films = new HashMap<>();
    private final Logger log = LoggerFactory.getLogger(InMemoryFilmStorage.class);

    @Override
    public Film addFilm(Film film) {
        film.setId(films.size() + 1);
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Film get(int id) {
        if (films.containsKey(id))
            return films.get(id);
        throw new NotFoundException("Фильм отсутствует в списке");
    }

    @Override
    public Film update(Film film) {
        if (film.getId() <= 0) {
            throw new ValidationException("Отсутствует id фильма: " + film.getId());
        }
        if (films.containsKey(film.getId())) {
            log.info("Данные фильма успешно обновлены: {}", film);
            films.put(film.getId(), film);
        } else {
            log.warn("Фильм с id = {} отсутствует в списке", film.getId());
            throw new NotFoundException(String.format("Фильм с id = %d отсутствует в списке",film.getId()));
        }
        return film;
    }

    @Override
    public Collection<Film> films() {
        return films.values();
    }
}
