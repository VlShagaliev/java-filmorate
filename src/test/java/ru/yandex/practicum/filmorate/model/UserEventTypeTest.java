package ru.yandex.practicum.filmorate.model;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserEventTypeTest {

    @Test
    void mustBeUniqueId() {
        final long allSize = UserEventType.values().length;
        final long uniqueSize = Arrays.stream(UserEventType.values())
                .map(UserEventType::getId)
                .distinct()
                .count();

        assertThat(allSize).isEqualTo(uniqueSize);
    }

    @Test
    void notFoundIdMustBeException() {
        assertThatThrownBy(() -> UserEventType.of(0))
                .isInstanceOf(IllegalArgumentException.class);
    }
}