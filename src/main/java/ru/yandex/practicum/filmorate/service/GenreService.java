package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.GenresDbStorage;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class GenreService {
    private final GenresDbStorage genresDbStorage;

    public Genre getGenreById(int id) {
        return genresDbStorage.getById(id);
    }

    public Collection<Genre> getGenres() {
        return genresDbStorage.getGenres();
    }
}
