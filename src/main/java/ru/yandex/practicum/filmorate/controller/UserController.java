package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.Collection;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    public Collection<User> users() {
        return userService.getUserStorage().users();
    }

    @PostMapping
    public User add(@Valid @RequestBody User user) {
        return userService.getUserStorage().add(user);
    }

    @PutMapping
    public User update(@Valid @RequestBody User user) {
        return userService.getUserStorage().update(user);
    }

    @GetMapping("/{id}")
    public User get(@PathVariable("id") int id) {
        return userService.getUserStorage().get(id);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public User addFriends(@PathVariable("id") int id,
                           @PathVariable("friendId") int friendId) {
        return userService.addFriend(id, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public User deleteFriend(@PathVariable("id") int id,
                             @PathVariable("friendId") int friendId) {
        return userService.deleteFriend(id,friendId);
    }

    @GetMapping("/{id}/friends")
    public Collection<User> friends(@PathVariable("id") int id) {
        return userService.userFriends(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public Collection<User> mutualFriends(@PathVariable("id") int id,
                                          @PathVariable("otherId") int otherId) {
        return userService.mutualFriends(id, otherId);
    }
}
