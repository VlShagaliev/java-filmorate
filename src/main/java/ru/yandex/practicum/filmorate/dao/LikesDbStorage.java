package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class LikesDbStorage {
    private static final String GET_COUNT_LIKES_BY_ID = "SELECT COUNT(id_user) FROM likes WHERE id_film = ?";
    private static final String INSERT_QUERY_LIKES = "INSERT INTO likes(id_film, id_user) VALUES (?, ?)";
    private final JdbcTemplate jdbc;

    public Integer addLike(int filmId, int userId) {
        jdbc.update(INSERT_QUERY_LIKES, filmId, userId);
        return getLike(filmId);
    }

    public Integer getLike(int filmId) {
        return jdbc.queryForObject(GET_COUNT_LIKES_BY_ID, Integer.class, filmId);
    }

    public void deleteLike(String query, Object... params) {
        jdbc.update(query, params);
    }
}
