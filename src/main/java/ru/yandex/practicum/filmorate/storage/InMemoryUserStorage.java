package ru.yandex.practicum.filmorate.storage;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Component
public class InMemoryUserStorage implements UserStorage {

    @Getter
    private final Map<Integer, User> users = new HashMap<>();
    private final Logger log = LoggerFactory.getLogger(User.class);

    @Override
    public User add(User user) {
        validateUser(user);
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
        validateUser(user);
        if (user.getId() <= 0) {
            log.warn("При обновлении отсутствует id пользователя: {}", user.getId());
            throw new ValidationException("Отсутствует id пользователя");
        }
        if (users.containsKey(user.getId())) {
            log.info("Данные пользователя успешно обновлены: {}", user);
            users.put(user.getId(), user);
        } else {
            log.warn("Пользователь с id = {} отсутствует в списке", user.getId());
            throw new NotFoundException("Пользователь с данным id отсутствует в списке");
        }
        return user;
    }

    @Override
    public User get(int id) {
        if (users.containsKey(id))
            return users.get(id);
        throw new NotFoundException("Пользователь отсутствует в списке");
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
