package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.UserEvent;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public Collection<User> users() {
        return userService.users();
    }

    @PostMapping
    public User add(@Valid @RequestBody User user) {
        return userService.add(user);
    }

    @PutMapping
    public User update(@Valid @RequestBody User user) {
        return userService.update(user);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable("id") int id) {
        userService.delete(id);
    }

    @GetMapping("/{id}")
    public User get(@PathVariable("id") int id) {
        return userService.get(id);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public User addFriends(@PathVariable("id") int id,
                           @PathVariable("friendId") int friendId) {
        return userService.addFriend(id, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public User deleteFriend(@PathVariable("id") int id,
                             @PathVariable("friendId") int friendId) {
        return userService.deleteFriend(id, friendId);
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

    @GetMapping("/{id}/feed")
    public Collection<UserEvent> getUserEvents(@PathVariable("id") int id) {
        return userService.getUserEvents(id);
    }

    @GetMapping("/{id}/recommendations")
    public List<Film> getRecommendations(@PathVariable int id) {
        return userService.getRecommendations(id);
    }
}
