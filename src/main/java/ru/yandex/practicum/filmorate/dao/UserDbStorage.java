package ru.yandex.practicum.filmorate.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.mappers.FriendsRowMapper;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.Set;

@Repository
public class UserDbStorage extends BaseDbStorage<User> implements UserStorage {
    private static final String INSERT_QUERY = "INSERT INTO users(name, login, email, birthday) " +
            "VALUES (?, ?, ?, ?)";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM users WHERE id = ?";
    private static final String FIND_ALL_QUERY = "SELECT * FROM users";
    private static final String UPDATE_QUERY = "UPDATE users SET name = ?, login = ?, email = ?, birthday = ? WHERE id = ?";
    private static final String ADD_FRIEND = "INSERT INTO friends(_from, _to,status) " +
            "VALUES (?, ?, ?)";
    public static final String errorMessage = "Пользователь с данным id = %d отсутствует в списке";

    private final FriendDbStorage friendDbStorage;

    public UserDbStorage(JdbcTemplate jdbc, RowMapper<User> mapper) {
        super(jdbc, mapper);
        friendDbStorage = new FriendDbStorage(jdbc, new FriendsRowMapper());
    }

    @Override
    public User add(User user) {
        Integer id = insert(
                INSERT_QUERY,
                user.getName(),
                user.getLogin(),
                user.getEmail(),
                user.getBirthday()
        );
        user.setId(id);
        return user;
    }

    @Override
    public User update(User user) {
        checkDbHasId(CHECK_USER_IN_DB, user.getId(), errorMessage);
        updateSql(
                UPDATE_QUERY,
                user.getName(),
                user.getLogin(),
                user.getEmail(),
                user.getBirthday(),
                user.getId()
        );
        return user;
    }

    @Override
    public Collection<User> users() {
        return jdbc.query(FIND_ALL_QUERY, mapper);
    }

    @Override
    public User get(int id) {
        checkDbHasId(CHECK_USER_IN_DB, id, errorMessage);
        return jdbc.queryForObject(FIND_BY_ID_QUERY, mapper, id);
    }

    public void checkDbHasId(int id) {
        String checkSql = "SELECT COUNT(*) FROM users WHERE id = ?";
        Integer check = jdbc.queryForObject(checkSql, Integer.class, id);
        if (check == 0 || check == null) {
            throw new NotFoundException(String.format("Пользователь с данным id = %d отсутствует в списке", id));
        }
    }

    public User deleteFriend(int id, int friendId) {
        checkDbHasId(CHECK_USER_IN_DB, id, errorMessage);
        checkDbHasId(CHECK_USER_IN_DB, friendId, errorMessage);
        String querySql = "DELETE FROM friends WHERE _from = ? AND _to = ?";
        jdbc.update(querySql, id, friendId);
        return get(id);
    }

    public User addFriend(int id, int friendId) {
        checkDbHasId(CHECK_USER_IN_DB, id, errorMessage);
        checkDbHasId(CHECK_USER_IN_DB, friendId, errorMessage);
        String checkSql = "SELECT COUNT(status) FROM friends WHERE _from = ? AND _to = ?";
        Integer check = jdbc.queryForObject(checkSql, Integer.class, id, friendId);
        boolean status = false;
        if (check != null && check != 0) {
            status = true;
            updateSql("UPDATE friends SET status = ? WHERE _from = ? AND _to = ?", status, id, friendId);
        }
        insert(ADD_FRIEND, id, friendId, status);
        User user = get(id);
        user.getFriends().add(friendId);
        return user;
    }

    public Collection<User> getFriends(int id) {
        checkDbHasId(CHECK_USER_IN_DB, id, errorMessage);
        Set<Integer> friends = friendDbStorage.getFriends(id);
        return friends.stream()
                .map(idFriends -> {
                    User user = get(idFriends);
                    user.setFriends(friendDbStorage.getFriends(idFriends));
                    return user;
                })
                .toList();
    }

    public Collection<User> mutualFriends(int id, int otherId) {
        checkDbHasId(CHECK_USER_IN_DB, id, errorMessage);
        checkDbHasId(CHECK_USER_IN_DB, otherId, errorMessage);

        Set<Integer> friendsId = friendDbStorage.getFriends(id);
        Set<Integer> friendsOtherId = friendDbStorage.getFriends(otherId);
        return friendsId.stream()
                .filter(friendsOtherId::contains)
                .map(idFriends -> {
                    User user = get(idFriends);
                    user.setFriends(friendDbStorage.getFriends(idFriends));
                    return user;
                })
                .toList();
    }
}
