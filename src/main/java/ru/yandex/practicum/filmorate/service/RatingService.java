package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.RatingDbStorage;
import ru.yandex.practicum.filmorate.model.Rating;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class RatingService {
    private final RatingDbStorage ratingDbStorage;

    public Rating getRatingById(int id) {
        return ratingDbStorage.getRatingById(id);
    }

    public Collection<Rating> getAll() {
        return ratingDbStorage.getAll();
    }
}
