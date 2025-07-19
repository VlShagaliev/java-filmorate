package ru.yandex.practicum.filmorate.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.FilmDbStorage;
import ru.yandex.practicum.filmorate.dao.LikesDbStorage;
import ru.yandex.practicum.filmorate.dao.UserDbStorage;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.UserEvent;
import ru.yandex.practicum.filmorate.model.UserEventOperation;
import ru.yandex.practicum.filmorate.model.UserEventType;
import ru.yandex.practicum.filmorate.storage.UserEventStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    @Getter
    private final UserDbStorage userDbStorage;
    private final FilmDbStorage filmDbStorage;
    private final LikesDbStorage likesDbStorage;
    private final UserEventStorage userEventStorage;
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
            User user = userDbStorage.addFriend(id, friendId);
            userEventStorage.add(id, friendId, UserEventType.FRIEND, UserEventOperation.ADD);
            return user;
        }
        throw new NotFoundException("Пользователь с данным id отсутствует в списке");
    }

    public User deleteFriend(int id, int friendId) {
        if (userDbStorage.get(id) != null && userDbStorage.get(friendId) != null) {
            User user = userDbStorage.deleteFriend(id, friendId);
            userEventStorage.add(id, friendId, UserEventType.FRIEND, UserEventOperation.REMOVE);
            return user;
        }
        throw new NotFoundException("Пользователь с данным id отсутствует в списке");
    }

    public Collection<User> userFriends(int id) {
        return userDbStorage.getFriends(id);
    }

    public Collection<User> mutualFriends(int id, int otherId) {
        return userDbStorage.mutualFriends(id, otherId);
    }

    public Collection<UserEvent> getUserEvents(int id) {
        if (userDbStorage.get(id) == null) {
            throw new NotFoundException("Пользователь с данным id отсутствует в списке");
        }
        return userEventStorage.getUserEvents(id);
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
        if (user.getName() == null || user.getName().isBlank()) {
            log.info("Имя пустое, присваиваем имя=логин: {}", user.getLogin());
            user.setName(user.getLogin());
        }
    }

    public List<Film> getRecommendations(int userId) {
        userDbStorage.checkDbHasId(userId);

        List<Integer> similarUserIds = likesDbStorage.findUsersWithSimilarLikes(userId);
        if (similarUserIds.isEmpty()) {
            return List.of();
        }

        return likesDbStorage.getRecommendedFilms(userId, similarUserIds).stream()
                .map(filmDbStorage::get)
                .collect(Collectors.toList());
    }
}
