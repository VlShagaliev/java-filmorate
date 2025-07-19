package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class LikesDbStorage {
    private static final String GET_COUNT_LIKES_BY_ID = "SELECT COUNT(id_user) FROM likes WHERE id_film = ?";
    private static final String INSERT_QUERY_LIKES = "INSERT INTO likes(id_film, id_user, mark) VALUES (?, ?, ?)";
    private static final String FIND_SIMILAR_USERS_SQL = """
            SELECT l2.id_user
            FROM likes l1
            JOIN likes l2 ON l1.id_film = l2.id_film AND l1.id_user != l2.id_user
            WHERE l1.id_user = ?
            GROUP BY l2.id_user
            HAVING ABS(AVG(l1.mark) - AVG(l2.mark)) <= 0.5
            ORDER BY COUNT(l2.id_film) DESC, ABS(AVG(l1.mark) - AVG(l2.mark)) ASC
            LIMIT 5
            """;

    private static final String GET_RECOMMENDATIONS_SQL = """
            SELECT f.id
            FROM films f
            JOIN likes l ON f.id = l.id_film
            WHERE l.id_user IN (:similarUserIds)
            AND NOT EXISTS (
                SELECT 1 FROM likes WHERE id_film = f.id AND id_user = :userId
            )
            GROUP BY f.id
            ORDER BY AVG(l.mark) DESC, COUNT(l.id_user) DESC
            LIMIT 10
            """;

    private final JdbcTemplate jdbc;

    public Integer addLike(int filmId, int userId, double mark) {
        jdbc.update(INSERT_QUERY_LIKES, filmId, userId, mark);
        return getLike(filmId);
    }

    public Integer getLike(int filmId) {
        return jdbc.queryForObject(GET_COUNT_LIKES_BY_ID, Integer.class, filmId);
    }

    public void deleteLike(String query, Object... params) {
        jdbc.update(query, params);
    }

    public List<Integer> findUsersWithSimilarLikes(int userId) {
        return jdbc.queryForList(FIND_SIMILAR_USERS_SQL, Integer.class, userId);
    }

    public List<Integer> getRecommendedFilms(int userId, List<Integer> similarUserIds) {
        if (similarUserIds.isEmpty()) {
            return List.of();
        }

        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("similarUserIds", similarUserIds);

        NamedParameterJdbcTemplate namedJdbc = new NamedParameterJdbcTemplate(jdbc);
        return namedJdbc.queryForList(GET_RECOMMENDATIONS_SQL, params, Integer.class);
    }
}
