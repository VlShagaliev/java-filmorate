package ru.yandex.practicum.filmorate.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.UserEvent;
import ru.yandex.practicum.filmorate.model.UserEventOperation;
import ru.yandex.practicum.filmorate.model.UserEventType;
import ru.yandex.practicum.filmorate.storage.UserEventStorage;

import java.time.Instant;
import java.util.Collection;

@Repository
public class UserEventDbStorage extends BaseDbStorage<UserEvent> implements UserEventStorage {
    private static final String INSERT_QUERY_USERS_EVENTS = """
            INSERT INTO users_events
                (time_stamp, id_user, id_entity, event_type, operation)
            VALUES
                (?, ?, ?, ?, ?)
            """;
    private static final String FIND_ALL_QUERY = """
            SELECT *
            FROM users_events
            WHERE id_user = ?
            """;

    public UserEventDbStorage(JdbcTemplate jdbc, RowMapper<UserEvent> mapper) {
        super(jdbc, mapper);
    }

    public void add(int userId, int entityId, UserEventType type, UserEventOperation operation) {
        Long timestamp = Instant.now().toEpochMilli();
        insert(
                INSERT_QUERY_USERS_EVENTS,
                timestamp,
                userId,
                entityId,
                type.getId(),
                operation.getId()
        );
    }

    public Collection<UserEvent> getUserEvents(int userId) {
        return jdbc.query(FIND_ALL_QUERY, mapper, userId);
    }
}