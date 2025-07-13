package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.DirectorDbStorage;
import ru.yandex.practicum.filmorate.model.Director;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class DirectorService {
    private final DirectorDbStorage directorDbStorage;

    public Collection<Director> getDirectors() {
        return directorDbStorage.directors();
    }

    public Director getDirectorById(int id) {
        return directorDbStorage.getDirectorById(id);
    }

    public Director addDirector(Director director) {
        return directorDbStorage.addDirector(director);
    }

    public Director updateDirector(Director director) {
        return directorDbStorage.updateDirector(director);
    }

    public void deleteDirectorById(int id) {
        directorDbStorage.deleteDirectorById(id);
    }

}
