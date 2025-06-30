package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class FriendDbStorage {
    private static final String GET_FRIENDS_BY_ID = "SELECT _to, status FROM friends WHERE _from = ?";
    private final JdbcTemplate jdbc;

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
