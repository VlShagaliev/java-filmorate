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
import ru.yandex.practicum.filmorate.dao.mappers.UserEventRowMapper;
import ru.yandex.practicum.filmorate.model.UserEvent;
import ru.yandex.practicum.filmorate.model.UserEventOperation;
import ru.yandex.practicum.filmorate.model.UserEventType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({UserEventDbStorage.class, UserEventRowMapper.class})
class UserEventDbStorageTest {
    final JdbcTemplate jdbcTemplate;
    final UserEventDbStorage userEventDbStorage;
    final UserEventRowMapper userEventRowMapper;
    static String sqlClearTables;
    final String sqlAddTestContent = """
            INSERT INTO users
                (login, name, email, birthday)
            VALUES
                ('login1', 'name1', 'user1@domain.com', '2001-01-01');
            """;
    final String sqlFindAll = """
            SELECT *
            FROM users_events
            """;

    @BeforeAll
    static void setAll() throws IOException {
        Path path = Paths.get("src/test/resources/clear-tables.sql");
        sqlClearTables = Files.readString(path);
    }

    @BeforeEach
    void setUp() {
        jdbcTemplate.update(sqlClearTables);
        jdbcTemplate.update(sqlAddTestContent);
    }

    @Test
    void add() {
        userEventDbStorage.add(1, 0, UserEventType.LIKE, UserEventOperation.ADD);
        List<UserEvent> userEvents = jdbcTemplate.query(sqlFindAll, userEventRowMapper);

        assertThat(userEvents)
                .hasSize(1)
                .first()
                .hasFieldOrPropertyWithValue("userId", 1)
                .hasFieldOrPropertyWithValue("entityId", 0)
                .hasFieldOrPropertyWithValue("eventType", UserEventType.LIKE)
                .hasFieldOrPropertyWithValue("operation", UserEventOperation.ADD);
    }

    @Test
    void getUserEvents() {
        userEventDbStorage.add(1, 0, UserEventType.LIKE, UserEventOperation.ADD);
        userEventDbStorage.add(1, 0, UserEventType.FRIEND, UserEventOperation.REMOVE);
        List<UserEvent> userEvents = jdbcTemplate.query(sqlFindAll, userEventRowMapper);

        assertThat(userEvents)
                .hasSize(2)
                .element(0)
                .hasFieldOrPropertyWithValue("userId", 1)
                .hasFieldOrPropertyWithValue("entityId", 0)
                .hasFieldOrPropertyWithValue("eventType", UserEventType.LIKE)
                .hasFieldOrPropertyWithValue("operation", UserEventOperation.ADD);

        assertThat(userEvents)
                .element(1)
                .hasFieldOrPropertyWithValue("userId", 1)
                .hasFieldOrPropertyWithValue("entityId", 0)
                .hasFieldOrPropertyWithValue("eventType", UserEventType.FRIEND)
                .hasFieldOrPropertyWithValue("operation", UserEventOperation.REMOVE);
    }
}