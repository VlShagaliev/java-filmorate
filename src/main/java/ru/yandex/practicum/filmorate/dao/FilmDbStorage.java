package ru.yandex.practicum.filmorate.dao;

import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.mappers.DirectorRowMapper;
import ru.yandex.practicum.filmorate.dao.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.dao.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Repository
public class FilmDbStorage extends BaseDbStorage<Film> implements FilmStorage {
    private static final String INSERT_QUERY_FILMS = "INSERT INTO films(name, description, releaseDate, duration, id_rating) " +
            "VALUES (?, ?, ?, ?, ?)";
    private static final String INSERT_QUERY_FILMS_GENRE = "MERGE INTO films_genres(id_film, id_genre) KEY(id_film, id_genre) VALUES (?, ?)";
    private static final String INSERT_QUERY_FILMS_DIRECTOR = "MERGE INTO film_director(id_film, id_director) KEY(id_film, id_director) VALUES (?, ?)";
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
    private static final String FIND_COMMON_QUERY = """
        SELECT f.id, f.name, f.description, f.releaseDate, f.duration, f.id_rating,
            r.name AS rating_name, COUNT(l3.id_user) AS likes_count
        FROM films f
        JOIN likes l1 ON f.id = l1.id_film AND l1.id_user = ?
        JOIN likes l2 ON f.id = l2.id_film AND l2.id_user = ?
        LEFT JOIN likes l3 ON f.id = l3.id_film
        LEFT JOIN ratings r ON r.id = f.id_rating
        GROUP BY f.id, f.name, f.description, f.releaseDate, f.duration, f.id_rating, r.name
        ORDER BY likes_count DESC
    """;
    public static final String errorMessage = "Фильм с данным id = %d отсутствует в списке";

    private final LikesDbStorage likesDbStorage;
    private final GenresDbStorage genresDbStorage;
    private final UserDbStorage userDbStorage;
    private final DirectorDbStorage directorDbStorage;

    public FilmDbStorage(JdbcTemplate jdbc, RowMapper<Film> mapper) {
        super(jdbc, mapper);
        likesDbStorage = new LikesDbStorage(jdbc);
        genresDbStorage = new GenresDbStorage(jdbc, new GenreRowMapper());
        userDbStorage = new UserDbStorage(jdbc, new UserRowMapper());
        directorDbStorage = new DirectorDbStorage(jdbc, new DirectorRowMapper());
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
        if (film.getDirectors() != null)
            film = directorsUpdate(film);
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
        deleteFromDb("DELETE FROM films_genres WHERE id_film = ?", film.getId());
        if (film.getGenres() != null) {
            film = genresUpdate(film);
        }
        deleteFromDb("DELETE FROM film_director WHERE id_film = ?", film.getId());
        if (film.getDirectors() != null) {
            film = directorsUpdate(film);
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

    private Film directorsUpdate(Film film) {
        Director[] directors = film.getDirectors();
        jdbc.batchUpdate(INSERT_QUERY_FILMS_DIRECTOR, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setInt(1, film.getId());
                ps.setInt(2, directors[i].getId());
            }

            @Override
            public int getBatchSize() {
                return directors.length;
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
        checkDbHasId(CHECK_FILM_IN_DB, id, errorMessage);
        Film film = jdbc.queryForObject(FIND_BY_ID_QUERY, mapper, id);
        film.setCountLikes(likesDbStorage.getLike(id));
        List<Genre> genreList = genresDbStorage.getGenre(id);
        film.setGenres(genreList.toArray(new Genre[0]));
        List<Director> directorList = directorDbStorage.getDirectorByIdFilm(id);
        film.setDirectors(directorList.toArray(new Director[0]));
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

        return pullGenresAndDirector(films);
    }

    public Collection<Film> getCommonFilms(int userId, int friendId) {
        checkDbHasId(CHECK_USER_IN_DB, userId, "Пользователь с данным id = %d отсутствует в списке");
        checkDbHasId(CHECK_USER_IN_DB, friendId, "Пользователь с данным id = %d отсутствует в списке");
        Collection<Film> films = jdbc.query(FIND_COMMON_QUERY, mapper, userId, friendId);

        films.forEach(film -> {
            film.setGenres(genresDbStorage.getGenre(film.getId()).toArray(new Genre[0]));
            film.setDirectors(directorDbStorage.getDirectorByIdFilm(film.getId()).toArray(new Director[0]));
            film.setCountLikes(likesDbStorage.getLike(film.getId()));
        });

        return films;
    }

    public Collection<Film> getCommonFilms(int userId, int friendId) {
        checkDbHasId(CHECK_USER_IN_DB, userId, "Пользователь с данным id = %d отсутствует в списке");
        checkDbHasId(CHECK_USER_IN_DB, friendId, "Пользователь с данным id = %d отсутствует в списке");
        Collection<Film> films = jdbc.query(FIND_COMMON_QUERY, mapper, userId, friendId);

        films.forEach(film -> {
            film.setGenres(genresDbStorage.getGenre(film.getId()).toArray(new Genre[0]));
            film.setDirectors(directorDbStorage.getDirectorByIdFilm(film.getId()).toArray(new Director[0]));
            film.setCountLikes(likesDbStorage.getLike(film.getId()));
        });

        return films;
    }

    public Film deleteLike(int filmId, int userId) {
        checkDbHasId(CHECK_FILM_IN_DB, filmId, errorMessage);
        userDbStorage.checkDbHasId(CHECK_USER_IN_DB, userId, "Пользователь с данным id = %d отсутствует в списке");
        String deleteSql = "DELETE FROM likes WHERE id_film = ? AND id_user = ?";
        likesDbStorage.deleteLike(deleteSql, filmId, userId);
        return get(filmId);
    }

    private final String querySqlSort = "SELECT f.*, r.name AS rating_name, COUNT(l.id_user) AS likes_count " +
            "FROM films as f " +
            "LEFT JOIN likes AS l ON f.id = l.id_film " +
            "LEFT JOIN ratings AS r ON r.id = f.id_rating " +
            "LEFT JOIN film_director AS fd ON f.id = fd.id_film " +
            "WHERE fd.id_director = ? " +
            "GROUP BY f.id, f.name, f.description, f.releaseDate, f.duration, f.id_rating " +
            "ORDER BY ";

    public Collection<Film> getFilmsSortedByYear(int directorId) {
        String releaseDate = "f.releaseDate ASC";
        Collection<Film> collection = jdbc.query(querySqlSort + releaseDate, mapper, directorId);
        return pullGenresAndDirector(collection);
    }

    public Collection<Film> getFilmsSortedByLikes(int directorId) {
        String likesCount = "likes_count DESC";
        Collection<Film> collection = jdbc.query(querySqlSort + likesCount, mapper, directorId);
        return pullGenresAndDirector(collection);
    }

    public Collection<Film> getFilmByQuery(String query, String by) {
        String sqlHead = """
                SELECT f.*,
                	r.name AS rating_name,
                	COUNT(l.id_user) AS likes_count
                FROM films AS f
                LEFT JOIN ratings AS r
                	ON r.id = f.id_rating
                LEFT JOIN likes AS l
                	ON l.id_film = f.id
                """;
        String sqlJoinDirector = """
                LEFT JOIN film_director AS fd
                	ON fd.id_film = f.id
                LEFT JOIN director AS d
                	ON d.id = fd.id_director
                """;
        String sqlBottom = "GROUP BY f.id, f.name, f.description, f.releaseDate, f.duration, f.id_rating " +
                "ORDER BY likes_count DESC";
        List<String> searchBy = Arrays.stream(by.split(",")).toList();
        StringBuilder sqlQuery = new StringBuilder(sqlHead);
        if (searchBy.contains("director")) {
            sqlQuery.append(sqlJoinDirector);
        }
        sqlQuery.append(System.lineSeparator()).append("WHERE");
        if (searchBy.contains("title")) {
            sqlQuery.append(" f.name LIKE '%").append(query).append("%' ");
        }
        if (searchBy.containsAll(List.of("title", "director"))) {
            sqlQuery.append(" OR");
        }
        if (searchBy.contains("director")) {
            sqlQuery.append(" d.name LIKE '%").append(query).append("%' ");
        }
        sqlQuery.append(System.lineSeparator()).append(sqlBottom);
        Collection<Film> films = jdbc.query(sqlQuery.toString(), mapper);
        return pullGenresAndDirector(films);
    }

    private Collection<Film> pullGenresAndDirector(Collection<Film> collection) {
        for (Film film : collection) {
            Film film1 = get(film.getId());
            film.setGenres(film1.getGenres());
            film.setDirectors(film1.getDirectors());
        }
        return collection;
    }
}
