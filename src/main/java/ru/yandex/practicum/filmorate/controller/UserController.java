package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {
    private final Map<Integer, User> users = new HashMap<>();
    private final Logger log = LoggerFactory.getLogger(User.class);

    @GetMapping
    public Collection<User> users() {
        return users.values();
    }

    @PostMapping
    public User addUser(@Valid @RequestBody User user) {
        validateUser(user);
        user.setId(users.size() + 1);
        users.put(user.getId(), user);
        log.info("Добавлен пользователь: {}", user);
        return user;
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
        if(user.getBirthday().isAfter(LocalDate.now())) {
            log.warn("Дата рождения не может быть в будущем: {}. Текущее время: {}",
                    user.getBirthday(), LocalDate.now());
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
        if(user.getName() == null) {
            log.info("Имя пустое, присваиваем имя=логин: {}", user.getLogin());
            user.setName(user.getLogin());
        }
    }

    @PutMapping
    public User update(@Valid @RequestBody User user) {
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
            throw new ValidationException("Пользователь с данным id отсутствует в списке");
        }
        return user;
    }

}
