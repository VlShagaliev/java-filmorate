package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.InternalServerException;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Collection;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {
    private static final String INSERT_QUERY_FILMS = "INSERT INTO films(name, description, releaseDate, duration, id_rating) " +
            "VALUES (?, ?, ?, ?, ?)";
    private static final String INSERT_QUERY_FILMS_GENRE = "MERGE INTO films_genres(id_film, id_genre) KEY(id_film, id_genre) VALUES (?, ?)";
    private static final String FIND_ALL_QUERY = "SELECT * FROM films";
    private static final String UPDATE_QUERY_FILMS_NAME = "UPDATE films SET name = ? WHERE id = ?";
    private static final String UPDATE_QUERY_FILMS_DESCRIPTION = "UPDATE films SET description = ? WHERE id = ?";
    private static final String UPDATE_QUERY_FILMS_RELEASE_DATE = "UPDATE films SET releaseDate = ? WHERE id = ?";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM films WHERE id = ?";
    private static final String UPDATE_QUERY_GENRES = "UPDATE films_genres SET id_film = ?, id_genre = ? WHERE id = ?";

    private final JdbcTemplate jdbc;
    private final RowMapper<Film> mapper;
    private final LikesDbStorage likesDbStorage;
    private final GenresDbStorage genresDbStorage;
    private final UserDbStorage userDbStorage;

    private Integer insert(String query, Object... params) {
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

    private void updateSql(String query, Object... params) {
        int rowsUpdated = jdbc.update(query, params);
        if (rowsUpdated == 0) {
            throw new InternalServerException("Не удалось обновить данные");
        }
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
            for (Genre genre : film.getGenres()) {
                checkGenresHasId(genre.getId());
                String nameGenre = jdbc.queryForObject("SELECT name FROM genres WHERE id = ?", String.class, genre.getId());
                genre.setName(nameGenre);
                insert(
                        INSERT_QUERY_FILMS_GENRE,
                        film.getId(),
                        genre.getId()
                );
            }
        String nameRating = jdbc.queryForObject("SELECT name FROM ratings WHERE id = ?", String.class, film.getRating().getId());
        film.getRating().setName(nameRating);
        return film;
    }

    @Override
    public Film update(Film film) {
        if (film.getName() != null)
            updateSql(UPDATE_QUERY_FILMS_NAME,
                    film.getName(),
                    film.getId()
            );
        if (film.getDescription() != null)
            updateSql(UPDATE_QUERY_FILMS_DESCRIPTION,
                    film.getDescription(),
                    film.getId()
            );
        if (film.getReleaseDate() != null)
            updateSql(UPDATE_QUERY_FILMS_RELEASE_DATE,
                    film.getReleaseDate(),
                    film.getId()
            );
        updateSql("DELETE FROM films_genres WHERE id_film = ?", film.getId());
        if (film.getGenres() != null) {
            for (Genre genre : film.getGenres()) {
                checkGenresHasId(genre.getId());
                String nameGenre = jdbc.queryForObject("SELECT name FROM genres WHERE id = ?", String.class, genre.getId());
                genre.setName(nameGenre);
                updateSql(
                        UPDATE_QUERY_GENRES,
                        film.getId(),
                        genre.getId()
                );
            }
        }
        if (film.getRating() != null) {
            String nameRating = jdbc.queryForObject("SELECT name FROM ratings WHERE id = ?", String.class, film.getRating().getId());
            film.getRating().setName(nameRating);
        }
        return film;
    }

    @Override
    public Collection<Film> films() {
        return jdbc.query(FIND_ALL_QUERY, mapper);
    }

    @Override
    public Film get(int id) {
        checkDbHasId(id);
        Film film = jdbc.queryForObject(FIND_BY_ID_QUERY, mapper, id);
        film.setCountLikes(likesDbStorage.getLike(id));
        List<Genre> genreList = genresDbStorage.getGenre(id);
        film.setGenres(genreList.toArray(new Genre[0]));
        String nameRating = jdbc.queryForObject("SELECT r.name FROM ratings r " +
                "LEFT JOIN films AS f ON r.id = f.id_rating " +
                "WHERE f.id = ?", String.class, film.getId());
        film.getRating().setName(nameRating);
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

    public void checkDbHasId(int id) {
        String checkSql = "SELECT COUNT(*) FROM films WHERE id = ?";
        Integer check = jdbc.queryForObject(checkSql, Integer.class, id);
        if (check == 0 || check == null) {
            throw new NotFoundException(String.format("Фильм с данным id = %d отсутствует в списке", id));
        }
    }

    public Collection<Film> mostPopular(int count) {
        String popularSql = "SELECT f.id, f.name, f.description, f.releaseDate, f.duration, f.id_rating, COUNT(l.id_user) AS likes_count FROM films as f " +
                "LEFT JOIN likes AS l ON f.id = l.id_film " +
                "GROUP BY f.id, f.name, f.description, f.releaseDate, f.duration, f.id_rating " +
                "ORDER BY likes_count DESC " +
                "LIMIT " + count;
        Collection<Film> collection = jdbc.query(popularSql, mapper);
        for (Film film : collection) {
            film = get(film.getId());
        }
        return collection;
    }

    public Film deleteLike(int filmId, int userId) {
        checkDbHasId(filmId);
        userDbStorage.checkDbHasId(userId);
        String deleteSql = "DELETE FROM likes WHERE id_film = ? AND id_user = ?";
        likesDbStorage.deleteLike(deleteSql, filmId, userId);
        return get(filmId);
    }
}
