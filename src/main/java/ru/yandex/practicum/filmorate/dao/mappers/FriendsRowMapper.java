package ru.yandex.practicum.filmorate.dao.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Friends;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class FriendsRowMapper implements RowMapper<Friends> {
    @Override
    public Friends mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        Friends friends = new Friends();
        friends.setTo(resultSet.getInt("_to"));
        friends.setStatus(resultSet.getBoolean("status"));
        return friends;
    }
}
