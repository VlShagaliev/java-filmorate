package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import ru.yandex.practicum.filmorate.exceptions.InternalServerException;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;

import java.sql.PreparedStatement;
import java.sql.Statement;

@RequiredArgsConstructor
public class BaseDbStorage<T> {
    protected static final String CHECK_USED_IN_DB = "SELECT COUNT(*) FROM users WHERE id = ?";
    public static final String CHECK_FILM_IN_DB = "SELECT COUNT(*) FROM films WHERE id = ?";

    protected final JdbcTemplate jdbc;
    protected final RowMapper<T> mapper;

    protected Integer insert(String query, Object... params) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection
                    .prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            for (int idx = 0; idx < params.length; idx++) {
                ps.setObject(idx + 1, params[idx]);
            }
            return ps;
        }, keyHolder);

        Integer id = keyHolder.getKeyAs(Integer.class);
        if (id != null) {
            return id;
        } else {
            throw new InternalServerException("Не удалось сохранить данные");
        }
    }

    protected void updateSql(String query, Object... params) {
        int rowsUpdated = jdbc.update(query, params);
        if (rowsUpdated == 0) {
            throw new InternalServerException("Не удалось обновить данные");
        }
    }

    protected void deleteFromDb(String query, Object... params) {
        jdbc.update(query, params);
    }

    public void checkDbHasId(String requestSql, int id, String message) {
        Integer check = jdbc.queryForObject(requestSql, Integer.class, id);
        if (check == 0 || check == null) {
            throw new NotFoundException(String.format(message, id));
        }
    }

}
