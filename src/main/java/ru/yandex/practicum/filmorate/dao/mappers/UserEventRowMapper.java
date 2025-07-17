package ru.yandex.practicum.filmorate.dao.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.UserEvent;
import ru.yandex.practicum.filmorate.model.UserEventOperation;
import ru.yandex.practicum.filmorate.model.UserEventType;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class UserEventRowMapper implements RowMapper<UserEvent> {
    @Override
    public UserEvent mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        UserEvent userEvent = new UserEvent();
        userEvent.setId(resultSet.getInt("id"));
        userEvent.setTimestamp(resultSet.getLong("time_stamp"));
        userEvent.setUserId(resultSet.getInt("id_user"));
        userEvent.setEntityId(resultSet.getInt("id_entity"));
        userEvent.setEventType(UserEventType.of(resultSet.getInt("event_type")));
        userEvent.setOperation(UserEventOperation.of(resultSet.getInt("operation")));
        return userEvent;
    }
}