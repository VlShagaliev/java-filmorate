package ru.yandex.practicum.filmorate.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.service.RatingService;

import java.util.Collection;

@RestController
@RequestMapping("/mpa")
public class RatingController {
    @Autowired
    private RatingService ratingService;

    @GetMapping("/{ratingId}")
    public Rating getRating(@PathVariable("ratingId") int ratingId) {
        return ratingService.getRatingById(ratingId);
    }

    @GetMapping
    public Collection<Rating> getAll() {
        return ratingService.getAll();
    }
}
