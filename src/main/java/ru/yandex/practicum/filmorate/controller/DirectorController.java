package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.service.DirectorService;

import java.util.Collection;

@RestController
@RequestMapping("/directors")
@RequiredArgsConstructor
public class DirectorController {
    private final DirectorService directorService;

    @GetMapping
    public Collection<Director> getDirectors() {
        return directorService.getDirectors();
    }

    @GetMapping("/{id}")
    public Director getDirectorById(@PathVariable("id") int id) {
        return directorService.getDirectorById(id);
    }

    @PostMapping
    public Director addDirector(@Valid @RequestBody Director director) {
        return directorService.addDirector(director);
    }

    @PutMapping
    public Director updateDirector(@Valid @RequestBody Director director) {
        return directorService.updateDirector(director);
    }

    @DeleteMapping("/{id}")
    public void deleteDirectorById(@PathVariable("id") int id) {
        directorService.deleteDirectorById(id);
    }


}
