package ru.yandex.practicum.filmorate.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Friends;

import java.util.HashSet;
import java.util.Set;

@Repository
public class FriendDbStorage extends BaseDbStorage<Friends> {
    private static final String GET_FRIENDS_BY_ID = "SELECT _to, status FROM friends WHERE _from = ?";

    public FriendDbStorage(JdbcTemplate jdbc, RowMapper<Friends> mapper) {
        super(jdbc, mapper);
    }

    public Set<Integer> getFriends(int id) {
        return jdbc.query(GET_FRIENDS_BY_ID, rs -> {
            Set<Integer> map = new HashSet<>();
            while (rs.next()) {
                int to = rs.getInt("_to");
                map.add(to);
            }
            return map;
        }, id);
    }
}
