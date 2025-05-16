package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
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

    @GetMapping
    public Collection<User> users(){
        return users.values();
    }

    @PostMapping
    public User addUser(@Valid @RequestBody User user){
        valid(user);
        user.setId(users.size()+1);
        users.put(user.getId(), user);
        return user;
    }

    private void valid(User user){
        if (user.getEmail().isEmpty() || !user.getEmail().contains("@")){
            throw new ValidationException("Электронная почта не может быть пустой и должна содержать символ @");
        }
        if (user.getLogin().isEmpty() || user.getLogin().contains(" ")){
            throw new ValidationException("Логин не может быть пустым и содержать пробелы");
        }
        if(user.getBirthday().isAfter(LocalDate.now())){
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
        if(user.getName().isEmpty()){
            user.setName(user.getLogin());
        }
    }

    @PutMapping
    public User update(@Valid @RequestBody User user){
        valid(user);
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
