package ru.yandex.practicum.filmorate.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.Collection;

@Service
@RequiredArgsConstructor
public class UserService {
    @Getter
    private final UserStorage userStorage;
    private final Logger log = LoggerFactory.getLogger(UserService.class);

    public User add(User user) {
        validateUser(user);
        return userStorage.add(user);
    }

    public User update(User user) {
        validateUser(user);
        return userStorage.update(user);
    }

    public Collection<User> users() {
        return userStorage.users();
    }

    public User get(int id) {
        if (userStorage.get(id) != null)
            return userStorage.get(id);
        throw new NotFoundException("Пользователь с данным id отсутствует в списке");
    }

    public User addFriend(int id, int friendId) {
        if (userStorage.get(id) != null && userStorage.get(friendId) != null) {
            User userId = userStorage.get(id);
            userId.getFriends().add(friendId);
            User userFriendId = userStorage.get(friendId);
            userFriendId.getFriends().add(id);
            return userId;
        }
        throw new NotFoundException("Пользователь с данным id отсутствует в списке");
    }

    public User deleteFriend(int id, int friendId) {
        if (userStorage.get(id) != null && userStorage.get(friendId) != null) {
            User userId = userStorage.get(id);
            userId.getFriends().remove(friendId);
            User userFriendId = userStorage.get(friendId);
            userFriendId.getFriends().remove(id);
            return userId;
        }
        throw new NotFoundException("Пользователь с данным id отсутствует в списке");
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

    private void validateUser(User user) {
        if (user.getEmail().isEmpty() || !user.getEmail().contains("@")) {
            log.warn("Электронная почта не может быть пустой и должна содержать символ @: {}", user.getEmail());
            throw new ValidationException("Электронная почта не может быть пустой и должна содержать символ @");
        }
        if (user.getLogin().contains(" ")) {
            log.warn("Логин не может содержать пробелы: {}", user.getLogin());
            throw new ValidationException("Логин не может содержать пробелы");
        }
        if (user.getBirthday().isAfter(LocalDate.now())) {
            log.warn("Дата рождения не может быть в будущем: {}. Текущее время: {}",
                    user.getBirthday(), LocalDate.now());
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
        if (user.getName() == null) {
            log.info("Имя пустое, присваиваем имя=логин: {}", user.getLogin());
            user.setName(user.getLogin());
        }
    }

}
