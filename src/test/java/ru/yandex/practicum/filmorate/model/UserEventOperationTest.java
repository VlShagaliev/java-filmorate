package ru.yandex.practicum.filmorate.model;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserEventOperationTest {

    @Test
    void mustBeUniqueId() {
        final long allSize = UserEventOperation.values().length;
        final long uniqueSize = Arrays.stream(UserEventOperation.values())
                .map(UserEventOperation::getId)
                .distinct()
                .count();

        assertThat(allSize).isEqualTo(uniqueSize);
    }

    @Test
    void notFoundIdMustBeException() {
        assertThatThrownBy(() -> UserEventOperation.of(0))
                .isInstanceOf(IllegalArgumentException.class);
    }
}