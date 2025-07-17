package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.dao.mappers.UserRowMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({UserDbStorage.class, UserRowMapper.class})
class UserDbStorageTest {
    private final JdbcTemplate jdbcTemplate;
    private final UserDbStorage userDbStorage;

    @Test
    void delete() throws IOException {
        final Path path = Paths.get("src/test/resources/clear-tables.sql");
        final String sqlClearTables = Files.readString(path);
        final String sqlAddTestContent = """
                INSERT INTO users
                    (login, name, email, birthday)
                VALUES
                    ('login1', 'name1', 'user1@domain.com', '2001-01-01'),
                    ('login2', 'name2', 'user2@domain.com', '2002-02-02');
                INSERT INTO films
                    (name, description, releaseDate, duration, id_rating)
                VALUES
                    ('name1', 'description1', '2001-01-01', 100, 1);
                INSERT INTO likes (id_film, id_user) VALUES (1, 1);
                INSERT INTO friends (_from, _to) VALUES (1, 2);
                """;
        final String sqlUser = """
                SELECT *
                FROM users
                WHERE id = ?
                """;
        final String sqlLikes = """
                SELECT *
                FROM likes
                WHERE id_user = ?
                """;
        final String sqlFriends = """
                SELECT *
                FROM friends
                WHERE _from = ?
                    OR _to = ?
                """;
        final int id = 1;

        jdbcTemplate.update(sqlClearTables);
        jdbcTemplate.update(sqlAddTestContent);
        userDbStorage.delete(id);

        assertThat(jdbcTemplate.queryForList(sqlUser, id))
                .hasSize(0);
        assertThat(jdbcTemplate.queryForList(sqlLikes, id))
                .hasSize(0);
        assertThat(jdbcTemplate.queryForList(sqlFriends, id, id))
                .hasSize(0);
    }
}