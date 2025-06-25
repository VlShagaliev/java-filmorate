package ru.yandex.practicum.filmorate.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class UserService {
    @Getter
    private final UserStorage userStorage;

    public User addFriend(int id, int friendId) {
        User userId = userStorage.get(id);
        userId.getFriends().add(friendId);
        User userFriendId = userStorage.get(friendId);
        userFriendId.getFriends().add(id);
        return userId;
    }

    public User deleteFriend(int id, int friendId) {
        User userId = userStorage.get(id);
        userId.getFriends().remove(friendId);
        User userFriendId = userStorage.get(friendId);
        userFriendId.getFriends().remove(id);
        return userId;
    }

    public Collection<User> userFriends(int id) {
        if (userStorage.get(id) != null)
            return userStorage.users().stream()
                .filter(user -> user.getFriends().contains(id))
                .toList();
        throw new NotFoundException("Пользователь с данным id отсутствует в списке");
    }

    public Collection<User> mutualFriends(int id, int otherId) {
        User otherUser = userStorage.get(otherId);
        return userStorage.users().stream()
                .filter(user -> userStorage.get(id).getFriends().stream()
                                    .filter(userId -> otherUser.getFriends().contains(userId))
                                    .toList()
                                    .contains(user.getId()))
                .toList();
    }
}
