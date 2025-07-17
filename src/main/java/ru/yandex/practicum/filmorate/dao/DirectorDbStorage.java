package ru.yandex.practicum.filmorate.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Director;

import java.util.Collection;
import java.util.List;

@Repository
public class DirectorDbStorage extends BaseDbStorage<Director> {
    private static final String INSERT_QUERY_DIRECTOR = "INSERT INTO director(name) " +
            "VALUES (?)";

    private static final String GET_DIRECTOR_BY_FILM_ID = "SELECT d.id, d.name FROM director AS d " +
            "LEFT JOIN film_director AS fd ON fd.id_director = d.id " +
            "WHERE fd.id_film = ? ORDER BY d.id";

    private static final String GET_DIRECTOR_BY_ID = "SELECT * " +
            "FROM director AS d " +
            "WHERE d.id = ?";

    private static final String UPDATE_QUERY = "UPDATE director " +
            "SET name = ? " +
            "WHERE id = ?";

    private static final String FIND_ALL_QUERY = "SELECT * " +
            "FROM director";

    public static final String CHECK_DIRECTOR_IN_DB = "SELECT COUNT(*) " +
            "FROM director " +
            "WHERE id = ?";

    private static final String DELETE_DIRECTOR_BY_ID = "DELETE FROM director " +
            "WHERE id = ?";

    public static final String errorMessage = "Режиссер с данным id = %d отсутствует в списке";

    public DirectorDbStorage(JdbcTemplate jdbc, RowMapper<Director> mapper) {
        super(jdbc, mapper);
    }

    public List<Director> getDirectorByIdFilm(int filmId) {
        return jdbc.query(GET_DIRECTOR_BY_FILM_ID, mapper, filmId);
    }

    public Collection<Director> directors() {
        return jdbc.query(FIND_ALL_QUERY, mapper);
    }

    public Director getDirectorById(int id) {
        checkDbHasId(CHECK_DIRECTOR_IN_DB, id, errorMessage);
        return jdbc.queryForObject(GET_DIRECTOR_BY_ID, mapper, id);
    }

    public Director addDirector(Director director) {
        Integer id = insert(INSERT_QUERY_DIRECTOR,
                director.getName()
        );
        director.setId(id);
        return director;
    }

    public Director updateDirector(Director director) {
        checkDbHasId(CHECK_DIRECTOR_IN_DB, director.getId(), errorMessage);
        updateSql(UPDATE_QUERY, director.getName(), director.getId());
        return director;
    }

    public void deleteDirectorById(int id) {
        checkDbHasId(CHECK_DIRECTOR_IN_DB, id, errorMessage);
        updateSql(DELETE_DIRECTOR_BY_ID, id);
    }
}
