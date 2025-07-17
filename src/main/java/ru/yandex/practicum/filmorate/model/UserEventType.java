package ru.yandex.practicum.filmorate.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.stream.Stream;

@Getter
@RequiredArgsConstructor
public enum UserEventType {
    LIKE(1),
    REVIEW(2),
    FRIEND(3);
    private final int id;

    public static UserEventType of(int id) {
        return Stream.of(UserEventType.values())
                .filter(o -> o.getId() == id)
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }
}