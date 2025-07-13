package ru.yandex.practicum.filmorate.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Rating;

import java.util.Collection;

@Repository
public class RatingDbStorage extends BaseDbStorage<Rating> {
    private static final String CHECK_RATING_IN_DB = "SELECT COUNT(*) FROM ratings WHERE id = ?";
    private final String errorMessage = "Рейтинг с данным id = %d отсутствует в списке";

    public RatingDbStorage(JdbcTemplate jdbc, RowMapper<Rating> mapper) {
        super(jdbc, mapper);
    }

    public Rating getRatingById(int id) {
        checkDbHasId(CHECK_RATING_IN_DB, id, errorMessage);
        return jdbc.queryForObject("SELECT id, name FROM ratings WHERE id = ?", mapper, id);
    }

    public Collection<Rating> getAll() {
        return jdbc.query("SELECT * FROM ratings ORDER BY id ASC", mapper);
    }
}
