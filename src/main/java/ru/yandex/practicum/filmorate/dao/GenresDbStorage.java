package ru.yandex.practicum.filmorate.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;
import java.util.List;

@Repository
public class GenresDbStorage extends BaseDbStorage<Genre> {
    private static final String GET_GENRES_BY_FILM_ID = "SELECT g.id, g.name FROM genres AS g " +
            "LEFT JOIN films_genres AS fg ON g.id = fg.id_genre " +
            "WHERE fg.id_film = ? ORDER BY g.id";

    public GenresDbStorage(JdbcTemplate jdbc, RowMapper<Genre> mapper) {
        super(jdbc, mapper);
    }

    public List<Genre> getGenre(int filmId) {
        return jdbc.query(GET_GENRES_BY_FILM_ID, mapper, filmId);
    }

    public Genre getById(int genreId) {
        checkDbHasId(genreId);
        return jdbc.queryForObject("SELECT * FROM genres WHERE id = ?", mapper, genreId);
    }

    public void checkDbHasId(int id) {
        String checkSql = "SELECT COUNT(*) FROM genres WHERE id = ?";
        Integer check = jdbc.queryForObject(checkSql, Integer.class, id);
        if (check == 0 || check == null) {
            throw new NotFoundException(String.format("Жанр с данным id = %d отсутствует в списке", id));
        }
    }

    public Collection<Genre> getGenres() {
        return jdbc.query("SELECT * FROM genres ORDER BY ID ASC", mapper);
    }

}
