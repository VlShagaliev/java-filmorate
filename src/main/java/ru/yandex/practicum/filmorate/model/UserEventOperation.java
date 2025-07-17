package ru.yandex.practicum.filmorate.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.stream.Stream;

@Getter
@RequiredArgsConstructor
public enum UserEventOperation {
    ADD(1),
    UPDATE(2),
    REMOVE(3);
    private final int id;

    public static UserEventOperation of(int id) {
        return Stream.of(UserEventOperation.values())
                .filter(o -> o.getId() == id)
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }
}