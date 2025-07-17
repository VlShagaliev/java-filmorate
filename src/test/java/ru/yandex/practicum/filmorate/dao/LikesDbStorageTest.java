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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({LikesDbStorage.class})
class LikesDbStorageTest {
    final JdbcTemplate jdbcTemplate;
    final LikesDbStorage likesDbStorage;
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
                    ('login1', 'name1', 'user1@domain.com', '2001-01-01');
                """;
        jdbcTemplate.update(sqlClearTables);
        jdbcTemplate.update(sqlAddTestContent);
    }

    @Test
    void addLike() {
        String sql = """
                SELECT *
                FROM likes
                """;

        likesDbStorage.addLike(1, 1, 1);
        List<Map<String, Object>> result = jdbcTemplate.queryForList(sql);

        assertThat(result)
                .hasSize(1)
                .first()
                .hasToString("{ID=1, ID_FILM=1, ID_USER=1, MARK=1}");

    }
}