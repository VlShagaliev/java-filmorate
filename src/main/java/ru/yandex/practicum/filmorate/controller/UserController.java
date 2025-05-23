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
    private final static Logger log = LoggerFactory.getLogger(User.class);

    @GetMapping
    public Collection<User> users(){
        return users.values();
    }

    @PostMapping
    public User addUser(@Valid @RequestBody User user){
        validUser(user);
        user.setId(users.size()+1);
        users.put(user.getId(), user);
        return user;
    }

    private void validUser(User user){
        if (user.getEmail().isEmpty() || !user.getEmail().contains("@")){
            throw new ValidationException("Электронная почта не может быть пустой и должна содержать символ @");
        }
        if (user.getLogin().contains(" ")){
            throw new ValidationException("Логин не может содержать пробелы");
        }
        if(user.getBirthday().isAfter(LocalDate.now())){
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
        if(user.getName() == null){
            user.setName(user.getLogin());
        }
    }

    @PutMapping
    public User update(@Valid @RequestBody User user){
        validUser(user);
        if (user.getId() <= 0){
            throw new ValidationException("Отсутствует id пользователя");
        }
        if (users.containsKey(user.getId())){
            users.put(user.getId(), user);
        } else {
            throw new ValidationException("Пользователь с данным id отсутствует в списке");
        }
        return user;
    }

}
