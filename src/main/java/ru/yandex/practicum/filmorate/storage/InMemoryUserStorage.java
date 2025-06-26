package ru.yandex.practicum.filmorate.storage;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Component
public class InMemoryUserStorage implements UserStorage {

    @Getter
    private final Map<Integer, User> users = new HashMap<>();
    private final Logger log = LoggerFactory.getLogger(InMemoryUserStorage.class);

    @Override
    public User add(User user) {
        user.setId(users.size() + 1);
        users.put(user.getId(), user);
        log.info("Добавлен пользователь: {}", user);
        return user;
    }

    @Override
    public Collection<User> users() {
        return users.values();
    }

    @Override
    public User update(User user) {
        if (user.getId() <= 0) {
            throw new ValidationException("Отсутствует id пользователя");
        }
        if (users.containsKey(user.getId())) {
            log.info("Данные пользователя успешно обновлены: {}", user);
            users.put(user.getId(), user);
        } else {
            throw new NotFoundException(String.format("Пользователь с данным id = %d отсутствует в списке", user.getId()));
        }
        return user;
    }

    @Override
    public User get(int id) {
        if (users.containsKey(id))
            return users.get(id);
        throw new NotFoundException(String.format("Пользователь с данным id = %d отсутствует в списке", id));
    }
}
