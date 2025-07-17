package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.dao.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.model.Film;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({FilmDbStorage.class, FilmRowMapper.class})
class FilmDbStorageTest {
    private final JdbcTemplate jdbcTemplate;
    private final FilmDbStorage filmDbStorage;
    static String sqlClearTables;

    @BeforeAll
    static void setAll() throws IOException {
        Path path = Paths.get("src/test/resources/clear-tables.sql");
        sqlClearTables = Files.readString(path);
    }

    @BeforeEach
    void setUp() {
        final String sqlAddTestContent = """
                INSERT INTO films
                    (name, description, releaseDate, duration, id_rating)
                VALUES
                    ('name1', 'description1', '2001-01-01', 100, 1);
                INSERT INTO users
                    (login, name, email, birthday)
                VALUES
                    ('login1', 'name1', 'user1@domain.com', '2001-01-01'),
                    ('login2', 'name2', 'user2@domain.com', '2002-02-02');
                INSERT INTO likes
                    (id_film, id_user, mark)
                VALUES
                    (1, 1, 6),
                    (1, 2, 8);
                INSERT INTO films_genres (id_film, id_genre) VALUES (1, 1);
                """;
        jdbcTemplate.update(sqlClearTables);
        jdbcTemplate.update(sqlAddTestContent);
    }

    @Test
    void deleteFilm() throws IOException {
        final String sqlFilm = """
                SELECT *
                FROM films
                WHERE id = ?
                """;
        final String sqlLikes = """
                SELECT *
                FROM likes
                WHERE id_film = ?
                """;
        final String sqlFilmsGenres = """
                SELECT *
                FROM films_genres
                WHERE id_film = ?
                """;
        final int id = 1;

        filmDbStorage.deleteFilm(id);

        assertThat(jdbcTemplate.queryForList(sqlFilm, id))
                .hasSize(0);
        assertThat(jdbcTemplate.queryForList(sqlLikes, id))
                .hasSize(0);
        assertThat(jdbcTemplate.queryForList(sqlFilmsGenres, id))
                .hasSize(0);
    }

    @Test
    void updateMarkShouldBeCorrect() {
        final int rate = filmDbStorage.recalcRate(1);
        final Film film = filmDbStorage.get(1);

        assertThat(rate).isEqualTo(7);
        assertThat(film).hasFieldOrPropertyWithValue("rate", 7);
    }
}