package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Rating;

import java.util.Collection;

@Repository
@RequiredArgsConstructor
public class RatingDbStorage {
    private final JdbcTemplate jdbc;
    private final RowMapper<Rating> mapper;

    public void checkDbHasId(int id) {
        String checkSql = "SELECT COUNT(*) FROM ratings WHERE id = ?";
        Integer check = jdbc.queryForObject(checkSql, Integer.class, id);
        if (check == 0 || check == null) {
            throw new NotFoundException(String.format("Рейтинг с данным id = %d отсутствует в списке", id));
        }
    }

    public Rating getRatingById(int id) {
        checkDbHasId(id);
        return jdbc.queryForObject("SELECT id, name FROM ratings WHERE id = ?", mapper, id);
    }

    public Collection<Rating> getAll() {
        return jdbc.query("SELECT * FROM ratings ORDER BY id ASC", mapper);
    }
}
