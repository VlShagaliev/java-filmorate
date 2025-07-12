package ru.yandex.practicum.filmorate.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.UserDbStorage;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;

@Service
@RequiredArgsConstructor
public class UserService {
    @Getter
    private final UserDbStorage userDbStorage;
    private final Logger log = LoggerFactory.getLogger(UserService.class);

    public User add(User user) {
        validateUser(user);
        return userDbStorage.add(user);
    }

    public User update(User user) {
        validateUser(user);
        return userDbStorage.update(user);
    }

    public void delete(int id) {
        if (userDbStorage.get(id) == null) {
            throw new NotFoundException("Пользователь с данным id отсутствует в списке");
        }
        userDbStorage.delete(id);
    }

    public Collection<User> users() {
        return userDbStorage.users();
    }

    public User get(int id) {
        return userDbStorage.get(id);
    }

    public User addFriend(int id, int friendId) {
        if (userDbStorage.get(id) != null && userDbStorage.get(friendId) != null) {
            return userDbStorage.addFriend(id, friendId);
        }
        throw new NotFoundException("Пользователь с данным id отсутствует в списке");
    }

    public User deleteFriend(int id, int friendId) {
        if (userDbStorage.get(id) != null && userDbStorage.get(friendId) != null) {
            return userDbStorage.deleteFriend(id, friendId);
        }
        throw new NotFoundException("Пользователь с данным id отсутствует в списке");
    }

    public Collection<User> userFriends(int id) {
        return userDbStorage.getFriends(id);
    }

    public Collection<User> mutualFriends(int id, int otherId) {
        return userDbStorage.mutualFriends(id, otherId);
    }

    private void validateUser(User user) {
        if (user.getEmail().isEmpty() || !user.getEmail().contains("@")) {
            throw new ValidationException("Электронная почта не может быть пустой и должна содержать символ @: " + user.getEmail());
        }
        if (user.getLogin().contains(" ")) {
            throw new ValidationException("Логин не может содержать пробелы:" + user.getLogin());
        }
        if (user.getBirthday().isAfter(LocalDate.now())) {
            throw new ValidationException(String.format("Дата рождения не может быть в будущем: %s. Текущее время: %s",
                    user.getBirthday(), LocalDate.now()));
        }
        if (user.getName() == null) {
            log.info("Имя пустое, присваиваем имя=логин: {}", user.getLogin());
            user.setName(user.getLogin());
        }
    }

}
