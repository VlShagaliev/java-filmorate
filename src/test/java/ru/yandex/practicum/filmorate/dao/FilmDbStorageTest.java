package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.dao.mappers.FilmRowMapper;

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

    @Test
    void deleteFilm() throws IOException {
        final Path path = Paths.get("src/test/resources/clear-tables.sql");
        final String sqlClearTables = Files.readString(path);
        final String sqlAddTestContent = """
                INSERT INTO films
                    (name, description, releaseDate, duration, id_rating)
                VALUES
                    ('name1', 'description1', '2001-01-01', 100, 1);
                INSERT INTO users
                    (login, name, email, birthday)
                VALUES
                    ('login1', 'name1', 'user1@domain.com', '2001-01-01');
                INSERT INTO likes (id_film, id_user) VALUES (1, 1);
                INSERT INTO films_genres (id_film, id_genre) VALUES (1, 1);
                """;
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

        jdbcTemplate.update(sqlClearTables);
        jdbcTemplate.update(sqlAddTestContent);
        filmDbStorage.deleteFilm(id);

        assertThat(jdbcTemplate.queryForList(sqlFilm, id))
                .hasSize(0);
        assertThat(jdbcTemplate.queryForList(sqlLikes, id))
                .hasSize(0);
        assertThat(jdbcTemplate.queryForList(sqlFilmsGenres, id))
                .hasSize(0);
    }
}