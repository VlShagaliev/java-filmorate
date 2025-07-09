package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.FilmDbStorage;
import ru.yandex.practicum.filmorate.dao.LikesDbStorage;
import ru.yandex.practicum.filmorate.dao.UserDbStorage;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendationService {
    private final LikesDbStorage likesDbStorage;
    private final FilmDbStorage filmDbStorage;
    private final UserDbStorage userDbStorage;

    public List<Film> getRecommendations(int userId) {

        userDbStorage.checkDbHasId(userId);

        List<Integer> similarUserIds = likesDbStorage.findUsersWithSimilarLikes(userId);

        if (similarUserIds.isEmpty()) {
            return List.of();
        }

        List<Integer> recommendedFilmIds = likesDbStorage.getRecommendedFilms(userId, similarUserIds);

        return recommendedFilmIds.stream()
                .map(filmDbStorage::get)
                .collect(Collectors.toList());
    }
}