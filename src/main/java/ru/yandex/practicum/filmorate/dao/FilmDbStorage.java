package ru.yandex.practicum.filmorate.dao;

import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.dao.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

@Repository
public class FilmDbStorage extends BaseDbStorage<Film> implements FilmStorage {
    private static final String INSERT_QUERY_FILMS = "INSERT INTO films(name, description, releaseDate, duration, id_rating) " +
            "VALUES (?, ?, ?, ?, ?)";
    private static final String INSERT_QUERY_FILMS_GENRE = "MERGE INTO films_genres(id_film, id_genre) KEY(id_film, id_genre) VALUES (?, ?)";
    private static final String FIND_ALL_QUERY = "SELECT f.*, r.name AS rating_name, COUNT(l.id_user) AS likes_count FROM films as f " +
            "LEFT JOIN likes AS l ON f.id = l.id_film " +
            "LEFT JOIN ratings AS r ON r.id = f.id_rating " +
            "GROUP BY f.id, f.name, f.description, f.releaseDate, f.duration, f.id_rating " +
            "ORDER BY f.id";
    private static final String UPDATE_QUERY_FILMS = "UPDATE films SET";
    private static final String FIND_BY_ID_QUERY = "SELECT f.*, r.name AS rating_name, COUNT(l.id_user) AS likes_count FROM films as f " +
            "LEFT JOIN likes AS l ON f.id = l.id_film " +
            "LEFT JOIN ratings AS r ON r.id = f.id_rating " +
            "WHERE f.id = ? " +
            "GROUP BY f.id, f.name, f.description, f.releaseDate, f.duration, f.id_rating ";


    private final LikesDbStorage likesDbStorage;
    private final GenresDbStorage genresDbStorage;
    private final UserDbStorage userDbStorage;

    public FilmDbStorage(JdbcTemplate jdbc, RowMapper<Film> mapper) {
        super(jdbc, mapper);
        likesDbStorage = new LikesDbStorage(jdbc);
        genresDbStorage = new GenresDbStorage(jdbc, new GenreRowMapper());
        userDbStorage = new UserDbStorage(jdbc, new UserRowMapper());
    }

    @Override
    public Film addFilm(Film film) {
        Integer id = insert(
                INSERT_QUERY_FILMS,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getRating().getId()
        );
        film.setId(id);
        if (film.getGenres() != null)
            film = genresUpdate(film);
        String nameRating = jdbc.queryForObject("SELECT name FROM ratings WHERE id = ?", String.class, film.getRating().getId());
        film.getRating().setName(nameRating);
        return film;
    }

    @Override
    public Film update(Film film) {
        StringBuilder requestBuilder = new StringBuilder(UPDATE_QUERY_FILMS);
        if (film.getName() != null)
            requestBuilder.append(" name = '").append(film.getName()).append("',");
        if (film.getDescription() != null)
            requestBuilder.append(" description = '").append(film.getDescription()).append("',");
        if (film.getReleaseDate() != null)
            requestBuilder.append(" releaseDate = '").append(film.getReleaseDate()).append("',");
        requestBuilder.delete(requestBuilder.length() - 1, requestBuilder.length());
        requestBuilder.append(" WHERE id = ").append(film.getId());
        updateSql(requestBuilder.toString());
        updateSql("DELETE FROM films_genres WHERE id_film = ?", film.getId());
        if (film.getGenres() != null) {
            film = genresUpdate(film);
        }
        if (film.getRating() != null) {
            String nameRating = jdbc.queryForObject("SELECT name FROM ratings WHERE id = ?", String.class, film.getRating().getId());
            film.getRating().setName(nameRating);
        }
        return film;
    }

    public void deleteFilm(int id) {
        String sql = "DELETE FROM films WHERE id = ?";
        updateSql(sql, id);
    }

    private Film genresUpdate(Film film) {
        Genre[] genres = film.getGenres();
        jdbc.batchUpdate(INSERT_QUERY_FILMS_GENRE, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                checkGenresHasId(genres[i].getId());
                ps.setInt(1, film.getId());
                ps.setInt(2, genres[i].getId());
                film.getGenres()[i].setName(jdbc.queryForObject("SELECT name FROM genres WHERE id = ?", String.class, genres[i].getId()));
            }

            @Override
            public int getBatchSize() {
                return genres.length;
            }
        });
        return film;
    }

    @Override
    public Collection<Film> films() {
        return jdbc.query(FIND_ALL_QUERY, mapper);
    }

    @Override
    public Film get(int id) {
        checkDbHasId(CHECK_FILM_IN_DB, id);
        Film film = jdbc.queryForObject(FIND_BY_ID_QUERY, mapper, id);
        film.setCountLikes(likesDbStorage.getLike(id));
        List<Genre> genreList = genresDbStorage.getGenre(id);
        film.setGenres(genreList.toArray(new Genre[0]));
        /*String nameRating = jdbc.queryForObject("SELECT r.name FROM ratings r " +
                "LEFT JOIN films AS f ON r.id = f.id_rating " +
                "WHERE f.id = ?", String.class, film.getId());
        film.getRating().setName(nameRating);*/
        return film;
    }

    private void checkGenresHasId(int id) {
        String checkSql = "SELECT COUNT(*) FROM genres WHERE id = ?";
        Integer check = jdbc.queryForObject(checkSql, Integer.class, id);
        if (check != 1) {
            throw new NotFoundException(String.format("Жанр с данным id = %d отсутствует в списке", id));
        }
    }

    public boolean checkRatingHasId(int id) {
        String checkSql = "SELECT COUNT(*) FROM ratings WHERE id = ?";
        Integer check = jdbc.queryForObject(checkSql, Integer.class, id);
        return check == 1;
    }

    public Collection<Film> mostPopular(int count, Integer genreId, Integer year) {
        StringBuilder sql = new StringBuilder("""
        SELECT f.*, r.name AS rating_name, COUNT(l.id_user) AS likes_count
        FROM films f
        LEFT JOIN likes l ON f.id = l.id_film
        LEFT JOIN ratings r ON r.id = f.id_rating
        """);

        if (genreId != null) {
            sql.append(" JOIN films_genres fg ON f.id = fg.id_film AND fg.id_genre = ").append(genreId);
        }

        if (year != null) {
            sql.append(" WHERE EXTRACT(YEAR FROM f.releaseDate) = ").append(year);
        }

        sql.append("""
        GROUP BY f.id, r.name
        ORDER BY likes_count DESC
        LIMIT
        """).append(count);

        Collection<Film> films = jdbc.query(sql.toString(), mapper);

        films.forEach(film -> {
            List<Genre> genres = genresDbStorage.getGenre(film.getId());
            film.setGenres(genres.toArray(new Genre[0]));
        });

        return films;
    }

    public Film deleteLike(int filmId, int userId) {
        checkDbHasId(CHECK_FILM_IN_DB, filmId);
        userDbStorage.checkDbHasId(CHECK_USED_IN_DB, userId);
        String deleteSql = "DELETE FROM likes WHERE id_film = ? AND id_user = ?";
        likesDbStorage.deleteLike(deleteSql, filmId, userId);
        return get(filmId);
    }
}
